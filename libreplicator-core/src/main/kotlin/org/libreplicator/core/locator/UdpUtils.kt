/*
 *     Copyright (C) 2016  Mihály Szabó
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

package org.libreplicator.core.locator

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketException

fun createMulticastSocket(multicastAddress: InetAddress, multicastPort: Int): MulticastSocket {
    val multicastSocket = MulticastSocket(multicastPort)
    multicastSocket.joinGroup(multicastAddress)
    return multicastSocket
}

fun receiveMessage(multicastSocket: DatagramSocket?, bufferSizeInBytes: Int): String {
    return try {
        String(extractData(multicastSocket, bufferSizeInBytes) ?: byteArrayOf())
    }
    catch (e: SocketException) {
        // Socket was closed.
        ""
    }
}

private fun extractData(multicastSocket: DatagramSocket?, bufferSizeInBytes: Int): ByteArray? {
    val packet = createPacket(bufferSizeInBytes)
    receiveData(multicastSocket, packet)
    if (!isDataReceived(packet, bufferSizeInBytes)) {
        return null
    }
    return extractSignificantData(packet)
}

private fun createPacket(bufferSizeInBytes: Int) = createPacket(createBuffer(bufferSizeInBytes))
private fun createBuffer(bufferSizeInBytes: Int) = ByteArray(bufferSizeInBytes)
private fun createPacket(buffer: ByteArray) = DatagramPacket(buffer, buffer.size)

private fun receiveData(multicastSocket: DatagramSocket?, packet: DatagramPacket) {
    multicastSocket?.receive(packet)
}

private fun isDataReceived(packet: DatagramPacket, bufferSizeInBytes: Int) =
    packet.length < bufferSizeInBytes || !packet.data.contentEquals(createBuffer(bufferSizeInBytes))

private fun extractSignificantData(packet: DatagramPacket) =
    packet.data.slice(0 until packet.length).toByteArray()

fun sendMessage(multicastSocket: DatagramSocket?, multicastAddress: InetAddress, multicastPort: Int, message: String) {
    try {
        multicastSocket?.send(createPacket(message, multicastAddress, multicastPort))
    }
    catch (e: SocketException) {
        // Socket was closed.
    }
}

private fun createPacket(message: String, multicastAddress: InetAddress, multicastPort: Int): DatagramPacket {
    val byteArray = message.toByteArray()
    return DatagramPacket(byteArray, byteArray.size, multicastAddress, multicastPort)
}
