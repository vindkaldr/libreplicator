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
import rx.Observable
import rx.lang.kotlin.observable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.concurrent.thread

class DefaultLogRouter
@Inject constructor(private val jsonMapper: JsonMapper,
                    private val localNode: ReplicatorNode,
                    private val logDispatcher: LogDispatcher) : LogRouter {

    private val logger = LoggerFactory.getLogger(DefaultLogRouter::class.java)

    private val BUFFER_SIZE_IN_BYTES = 1024 * 1024
    private val SOCKET_TIMEOUT_IN_MILLIS = 1000

    private var socket = createSocket()
    private var messageSubscription = createMessageSubscription()

    override fun send(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        if (isRouterClosed()) {
            return
        }
        try {
            socket.send(createPacket(remoteNode, message))
        }
        catch (unknownHostException: UnknownHostException) {
            logger.error(unknownHostException.message, unknownHostException)
        }
        catch (jsonWriteException: JsonWriteException) {
            logger.error(jsonWriteException.message, jsonWriteException)
        }
    }

    override fun open() {
        if (isRouterClosed()) {
            socket = createSocket()
            messageSubscription = createMessageSubscription()
        }
    }

    override fun close() = messageSubscription.unsubscribe()

    private fun isRouterClosed() = socket.isClosed and messageSubscription.isUnsubscribed
    private fun createSocket() = DatagramSocket(localNode.port)
    private fun createMessageSubscription() = createMessageObservable().subscribe { logDispatcher.receive(it) }

    private fun createPacket(remoteNode: ReplicatorNode, message: ReplicatorMessage): DatagramPacket {
        val messageAsByteArray = jsonMapper.write(message).toByteArray()

        return DatagramPacket(messageAsByteArray, messageAsByteArray.size,
                InetAddress.getByName(remoteNode.url), remoteNode.port)
    }

    private fun createMessageObservable() = observable<ReplicatorMessage> { subscriber ->
        thread {
            setSocketTimeout(SOCKET_TIMEOUT_IN_MILLIS)
            while (true) {
                try {
                    if (subscriber.isUnsubscribed) {
                        closeSocket()
                        break
                    }
                    subscriber.onNext(readMessageFromSocket())
                }
                catch (socketTimeoutException: SocketTimeoutException) {
                    // Ignore it. I have no better solution to have timeout on the socket.
                }
                catch (jsonReadException: JsonReadException) {
                    logger.error(jsonReadException.message, jsonReadException)
                    subscriber.onError(jsonReadException)
                }
            }
        }
    }

    private fun setSocketTimeout(timeout: Int) {
        socket.soTimeout = timeout
    }

    private fun closeSocket() = socket.close()

    private fun readMessageFromSocket(): ReplicatorMessage {
        val packet = DatagramPacket(ByteArray(BUFFER_SIZE_IN_BYTES), BUFFER_SIZE_IN_BYTES)

        socket.receive(packet)

        return jsonMapper.read(String(packet.data.copyOfRange(0, packet.length)), ReplicatorMessage::class)
    }
}
