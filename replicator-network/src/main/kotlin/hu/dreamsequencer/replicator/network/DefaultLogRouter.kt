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

import com.google.inject.assistedinject.Assisted
import hu.dreamsequencer.replicator.json.api.JsonMapper
import hu.dreamsequencer.replicator.json.api.JsonReadException
import hu.dreamsequencer.replicator.json.api.JsonWriteException
import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogRouter
import hu.dreamsequencer.replicator.model.ReplicatorMessage
import rx.Observable
import rx.lang.kotlin.observable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import kotlin.concurrent.thread

internal class DefaultLogRouter
@Inject constructor(private val jsonMapper: JsonMapper,
                    @Assisted private val localNode: ReplicatorNode,
                    @Assisted private val logDispatcher: LogDispatcher) : LogRouter {

    companion object {
        private val BUFFER_SIZE = 1024
    }

    private val socket = DatagramSocket(localNode.port)
    private val subscription = createReplicatorMessageObservable().subscribe { logDispatcher.receive(it) }

    override fun send(remoteNode: ReplicatorNode, message: ReplicatorMessage) {
        try {
            socket.send(writeMessage(message, remoteNode))
        }
        catch (jsonWriteException: JsonWriteException) {
            error(jsonWriteException)
        }
    }

    override fun close() = subscription.unsubscribe()

    private fun writeMessage(message: ReplicatorMessage, remoteNode: ReplicatorNode): DatagramPacket {
        val messageAsByteArray = jsonMapper.write(message).toByteArray()

        return DatagramPacket(messageAsByteArray, messageAsByteArray.size,
                InetAddress.getByName(remoteNode.url), remoteNode.port)
    }

    private fun createReplicatorMessageObservable(): Observable<ReplicatorMessage> {
        return observable<ReplicatorMessage> { subscriber ->
            thread {
                while (true) {
                    try {
                        if (subscriber.isUnsubscribed) {
                            closeSocket()
                            break
                        }
                        subscriber.onNext(waitForSocketAndReadMessage())
                    }
                    catch (jsonReadException: JsonReadException) {
                        error(jsonReadException)
                        subscriber.onError(jsonReadException)
                    }
                }
            }
        }
    }

    private fun closeSocket() = socket.close()

    private fun waitForSocketAndReadMessage(): ReplicatorMessage {
        val packet = DatagramPacket(ByteArray(BUFFER_SIZE), BUFFER_SIZE)

        socket.receive(packet)

        return jsonMapper.read(String(packet.data.copyOfRange(0, packet.length)), ReplicatorMessage::class)
    }
}
