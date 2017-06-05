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

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Observer
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.network.api.MessageRouter
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DefaultMessageRouter @Inject constructor(
        private val replicatorClientProvider: Provider<ReplicatorClient>,
        private val jsonMapper: JsonMapper,
        private val cipher: Cipher,
        private val localNode: ReplicatorNode) : MessageRouter {

    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultMessageRouter::class.java)
        private val SYNC_PATH = "/sync"
    }

    private lateinit var replicatorClient: ReplicatorClient
    private var hasSubscription = false

    override fun routeMessage(remoteNode: ReplicatorNode, message: ReplicatorMessage) = synchronized(this) {
        if (!hasSubscription) {
            throw NotSubscribedException()
        }
        replicatorClient.synchronizeWithNode(remoteNode, message)
    }

    override fun subscribe(messageObserver: Observer<ReplicatorMessage>): Subscription = synchronized(this) {
        val server = createServer(localNode)
        server.handler = ReplicatorMessageHandler(jsonMapper, cipher, messageObserver)
        server.startAndWaitUntilStarted()

        replicatorClient = replicatorClientProvider.get()
        hasSubscription = true

        return object : Subscription {
            override fun unsubscribe() = synchronized(this) {
                server.stopAndWaitUntilStarted()

                replicatorClient.close()
                hasSubscription = false
            }
        }
    }

    private fun createServer(localNode: ReplicatorNode): Server = Server(localNode.port)

    override fun hasSubscription(): Boolean = hasSubscription

    private class ReplicatorMessageHandler(
            private val jsonMapper: JsonMapper,
            private val cipher: Cipher,
            private val messageObserver: Observer<ReplicatorMessage>) : AbstractHandler() {

        override fun handle(target: String?, baseRequest: Request?, request: HttpServletRequest?, response: HttpServletResponse?) {
            if (baseRequest.isPostRequest() && baseRequest.isRequestedPath(SYNC_PATH)) {
                tryDeserializeAndObserveMessage(baseRequest)
                response?.status = HttpServletResponse.SC_NO_CONTENT
                baseRequest.markHandled()
            }
        }

        private fun tryDeserializeAndObserveMessage(baseRequest: Request?) {
            try {
                messageObserver.observe(deserializeMessage(cipher.decrypt(baseRequest.getMessage())))
            }
            catch (e: JsonReadException) {
                logger.warn("Failed to deserialize message!")
            }
        }

        private fun deserializeMessage(message: String): ReplicatorMessage =
                jsonMapper.read(message, ReplicatorMessage::class)
    }
}
