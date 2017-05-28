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

import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.libreplicator.api.Observer
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.crypto.api.MessageCipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.network.api.LogRouter
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DefaultLogRouter @Inject constructor(
        private val jsonMapper: JsonMapper,
        private val messageCipher: MessageCipher,
        private val localNode: ReplicatorNode) : LogRouter {

    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultLogRouter::class.java)
        private val SYNC_PATH = "/sync"
    }

    private var hasSubscription = false

    override fun send(remoteNode: ReplicatorNode, message: ReplicatorMessage) = synchronized(this) {
        try {
            serializeAndSendMessage(remoteNode, message)
        }
        catch (e: HttpHostConnectException) {
            logger.info("Failed to connect remote node!")
        }
        catch (e: ConnectTimeoutException) {
            logger.info("Failed to connect remote node!")
        }
    }

    private fun serializeAndSendMessage(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        val httpClient = createHttpClient()
        val httpPostRequest = createHttpPostRequest(remoteNode, SYNC_PATH, message)

        httpClient.use { httpClient.execute(httpPostRequest) }
    }

    private fun createHttpClient(): CloseableHttpClient = HttpClients.createDefault()

    private fun createHttpPostRequest(remoteNode: ReplicatorNode, path: String, message: ReplicatorMessage): HttpPost {
        val httpPost = HttpPost(remoteNode.toUri(path))
        httpPost.entity = StringEntity(messageCipher.encrypt(jsonMapper.write(message)))
        return httpPost
    }

    override fun subscribe(messageObserver: Observer<ReplicatorMessage>): Subscription = synchronized(this) {
        val server = createServer(localNode)
        server.handler = ReplicatorMessageHandler(jsonMapper, messageCipher, messageObserver)
        server.startAndWaitUntilStarted()

        hasSubscription = true

        return object : Subscription {
            override fun unsubscribe() = synchronized(this) {
                server.stopAndWaitUntilStarted()
                hasSubscription = false
            }
        }
    }

    private fun createServer(localNode: ReplicatorNode): Server = Server(localNode.port)

    override fun hasSubscription(): Boolean = hasSubscription

    private class ReplicatorMessageHandler(
            private val jsonMapper: JsonMapper,
            private val messageCipher: MessageCipher,
            private val messageObserver: Observer<ReplicatorMessage>) : AbstractHandler() {

        override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
            if (baseRequest.isPostRequest() && baseRequest.isRequestedPath(SYNC_PATH)) {
                tryDeserializeAndObserveMessage(baseRequest)
                baseRequest.markHandled()
            }
        }

        private fun tryDeserializeAndObserveMessage(baseRequest: Request?) {
            try {
                messageObserver.observe(deserializeMessage(messageCipher.decrypt(baseRequest.getMessage())))
            }
            catch (e: JsonReadException) {
                logger.warn("Failed to deserialize message!")
            }
        }

        private fun deserializeMessage(message: String): ReplicatorMessage =
                jsonMapper.read(message, ReplicatorMessage::class)
    }
}
