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

import org.libreplicator.api.ReplicatorNode
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.api.LogRouter
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.json.api.JsonWriteException
import org.libreplicator.model.ReplicatorMessage
import org.slf4j.LoggerFactory
import rx.lang.kotlin.observable
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.concurrent.thread

class DefaultLogRouter
@Inject constructor(private val jsonMapper: JsonMapper,
                    private val localNode: ReplicatorNode,
                    private val logDispatcher: LogDispatcher) : LogRouter {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultLogRouter::class.java)
        private val BUFFER_SIZE_IN_BYTES = 1024 * 1024
    }

    private var socket = createSocket()
    private var messageSubscription = subscribeToMessages()

    override fun send(remoteNode: ReplicatorNode, message: ReplicatorMessage) = synchronized(this) {
        if (isRouterClosed()) {
            return
        }
        try {
            socket.send(createPacketToNodeFromMessage(remoteNode, message))
        }
        catch (unknownHostException: UnknownHostException) {
            logger.error(unknownHostException.message, unknownHostException)
        }
        catch (jsonWriteException: JsonWriteException) {
            logger.error(jsonWriteException.message, jsonWriteException)
        }
    }

    override fun open() = synchronized(this) {
        if (isRouterClosed()) {
            socket = createSocket()
            messageSubscription = subscribeToMessages()
        }
    }

    override fun close() = synchronized(this) {
        if (isRouterClosed()) {
            return
        }
        unsubscribeFromMessages()
        closeSocket()
        waitUntilRouterIsClosed()
    }

    private fun isRouterClosed() = socket.isClosed and messageSubscription.isUnsubscribed

    private fun createPacketToNodeFromMessage(remoteNode: ReplicatorNode, message: ReplicatorMessage): DatagramPacket {
        val messageAsByteArray = jsonMapper.write(message).toByteArray()

        return DatagramPacket(messageAsByteArray, messageAsByteArray.size,
                InetAddress.getByName(remoteNode.url), remoteNode.port)
    }

    private fun createSocket() = DatagramSocket(localNode.port)
    private fun subscribeToMessages() = createMessageObservable().subscribe { logDispatcher.receive(it) }

    private fun unsubscribeFromMessages() = messageSubscription.unsubscribe()
    private fun closeSocket() = socket.close()

    private fun waitUntilRouterIsClosed() {
        while (!isRouterClosed()) {
            sleep(1000)
        }
    }

    private fun createMessageObservable() = observable<ReplicatorMessage> { subscriber ->
        thread {
            while (true) {
                try {
                    if (subscriber.isUnsubscribed) {
                        break
                    }
                    subscriber.onNext(readMessageFromSocket())
                }
                catch (jsonReadException: JsonReadException) {
                    logger.error(jsonReadException.message, jsonReadException)
                    subscriber.onError(jsonReadException)
                }
                catch (socketException: SocketException) {
                    if (!socket.isClosed) {
                        logger.error(socketException.message, socketException)
                        socket.close()
                    }
                    subscriber.unsubscribe()
                }
            }
        }
    }

    private fun readMessageFromSocket(): ReplicatorMessage {
        val packet = DatagramPacket(ByteArray(BUFFER_SIZE_IN_BYTES), BUFFER_SIZE_IN_BYTES)
        socket.receive(packet)
        return jsonMapper.read(String(packet.data.copyOfRange(0, packet.length)), ReplicatorMessage::class)
    }
}
