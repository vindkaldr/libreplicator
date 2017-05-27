/*
 *     Copyright (C) 2016  Mihaly Szabo <szmihaly91@gmail.com/>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplicator.network

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.logging.log4j.core.util.IOUtils
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.libreplicator.api.Observer
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.network.api.LogRouter
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DefaultLogRouter @Inject constructor(
        private val jsonMapper: JsonMapper,
        private val localNode: ReplicatorNode) : LogRouter {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultLogRouter::class.java)
    }

    private var subscribedTo = false

    override fun send(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        val remoteUri = URIBuilder()
                .setScheme("http")
                .setHost(remoteNode.url)
                .setPort(remoteNode.port)
                .setPath("/sync")
                .build()

        val httpRequestConfig = RequestConfig.custom()
                .setConnectTimeout(1)
                .build()

        val httpPost = HttpPost(remoteUri)
        httpPost.entity = StringEntity(jsonMapper.write(message))!!
        httpPost.config = httpRequestConfig

        val httpClient = HttpClients.createDefault()

        try {
            httpClient.use { httpClient.execute(httpPost) }
        }
        catch (e: HttpHostConnectException) {
            logger.info("Failed to connect remote node!")
        }
        catch (e: ConnectTimeoutException) {
            logger.info("Failed to connect remote node!")
        }
    }

    override fun subscribe(messageObserver: Observer<ReplicatorMessage>): Subscription = synchronized(this) {
        val server = Server(localNode.port)
        server.handler = MyHandler(messageObserver)
        server.start()

        while (!server.isStarted) {
            sleep(250)
        }

        subscribedTo = true

        return object : Subscription {
            override fun unsubscribe() = synchronized(this) {
                server.stop()
                while (!server.isStopped) {
                    sleep(250)
                }
                subscribedTo = false
            }
        }
    }

    override fun hasSubscription(): Boolean = subscribedTo

    inner class MyHandler(private val messageObserver: Observer<ReplicatorMessage>): AbstractHandler() {
        override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
            if (baseRequest?.pathInfo == "/sync") {
                val messageAsString = IOUtils.toString(baseRequest?.getReader())
                messageObserver.observe(jsonMapper.read(messageAsString, ReplicatorMessage::class))
                baseRequest?.setHandled(true)
            }
        }

    }
}
