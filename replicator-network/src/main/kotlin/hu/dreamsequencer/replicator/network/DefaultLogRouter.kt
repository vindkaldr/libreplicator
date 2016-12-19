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

package hu.dreamsequencer.replicator.network

import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogRouter
import hu.dreamsequencer.replicator.json.api.JsonMapper
import hu.dreamsequencer.replicator.json.api.JsonReadException
import hu.dreamsequencer.replicator.json.api.JsonWriteException
import hu.dreamsequencer.replicator.model.ReplicatorMessage
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

internal class DefaultLogRouter
@Inject constructor(private val jsonMapper: JsonMapper,
                    localNode: ReplicatorNode,
                    private val logDispatcher: LogDispatcher) : LogRouter {

    private val logger = LoggerFactory.getLogger(DefaultLogRouter::class.java)

    private val BUFFER_SIZE_IN_BYTES = 1024
    private val SOCKET_TIMEOUT_IN_MILLIS = 1000

    private val socket = DatagramSocket(localNode.port)
    private val messageSubscription = createMessageObservable().subscribe { logDispatcher.receive(it) }

    override fun send(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
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

    override fun close() = messageSubscription.unsubscribe()

    private fun createPacket(remoteNode: ReplicatorNode, message: ReplicatorMessage): DatagramPacket {
        val messageAsByteArray = jsonMapper.write(message).toByteArray()

        return DatagramPacket(messageAsByteArray, messageAsByteArray.size,
                InetAddress.getByName(remoteNode.url), remoteNode.port)
    }

    private fun createMessageObservable(): Observable<ReplicatorMessage> {
        return observable<ReplicatorMessage> { subscriber ->
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
