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

package org.libreplicator.network.channel

import org.libreplicator.api.AlreadySubscribedException
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Observer
import org.libreplicator.api.Subscription
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException
import kotlin.concurrent.thread

open class InsecureChannel(private val localUrl: String, private val localPort: Int) {
    private companion object {
        private val BUFFER_SIZE_IN_BYTES = 1024 * 1024
        private val LISTENING_CHECK_INTERVAL = 250L
    }

    private var hasSubscription = false
    private var listening = false

    private lateinit var socket: DatagramSocket

    fun send(remoteUrl: String, remotePort: Int, message: String) {
        val messageAsByteArray = message.toByteArray()
        val remoteAddress = InetSocketAddress(remoteUrl, remotePort)

        val packet = DatagramPacket(messageAsByteArray, messageAsByteArray.size, remoteAddress)
        socket.send(packet)
    }

    fun subscribe(observer: Observer<String>): Subscription = synchronized(this) {
        if (hasSubscription) {
            throw AlreadySubscribedException()
        }
        hasSubscription = true
        socket = DatagramSocket(InetSocketAddress(localUrl, localPort))

        startReceivingThread(observer)
        waitUntilListeningStarted()

        return object : Subscription {
            override fun unsubscribe() {
                if (!hasSubscription) {
                    throw NotSubscribedException()
                }
                hasSubscription = false
                socket.close()
                waitUntilListeningFinished()
            }
        }
    }

    private fun startReceivingThread(observer: Observer<String>) = thread {
        while (true) {
            listening = true
            try {
                val packet = DatagramPacket(ByteArray(BUFFER_SIZE_IN_BYTES), BUFFER_SIZE_IN_BYTES)
                socket.receive(packet)

                val messageAsByteArray = packet.data.copyOf(packet.length)
                val message = String(messageAsByteArray)
                observer.observe(message)
            }
            catch (socketException: SocketException) {
                if (hasSubscription) {
                    hasSubscription = false
                    socket.close()
                }
                break
            }
        }
        listening = false
    }

    fun hasSubscription() = hasSubscription

    private fun waitUntilListeningStarted() {
        while (!listening) {
            sleep(LISTENING_CHECK_INTERVAL)
        }
    }

    private fun waitUntilListeningFinished() {
        while (listening) {
            sleep(LISTENING_CHECK_INTERVAL)
        }
    }
}
