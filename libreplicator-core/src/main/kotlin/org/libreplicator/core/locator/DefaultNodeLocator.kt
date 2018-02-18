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

import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.core.locator.api.NodeLocator
import org.libreplicator.core.locator.api.NodeLocatorSettings
import org.libreplicator.json.api.JsonMapper
import java.net.MulticastSocket
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

class DefaultNodeLocator @Inject constructor(
    private val localNode: LocalNode,
    private val settings: NodeLocatorSettings,
    private val jsonMapper: JsonMapper
): NodeLocator {
    private val remoteNodes = mutableMapOf<String, RemoteNode>()
    private var multicastSocket: MulticastSocket? = null
    private var multicastTimer: Timer? = null

    override fun acquire(): NodeLocator = apply {
        check(multicastSocket == null, { "Object already acquired!" })
        multicastSocket = createMulticastSocket(settings.multicastAddress, settings.multicastPort)
        listenOnMulticastSocket()
        schedulePeriodicMulticast()
    }

    private fun listenOnMulticastSocket() = thread {
        generateSequence { receiveMessage(multicastSocket, settings.bufferSizeInBytes) }
            .takeWhile { it.isNotBlank() }
            .map { readMessage(it) }
            .forEach { updateRemoteNodes(it) }
    }

    private fun readMessage(message: String) = jsonMapper.read(message, NodeSyncMessage::class)

    private fun updateRemoteNodes(nodeSyncMessage: NodeSyncMessage) {
        nodeSyncMessage.removedNodeIds.forEach { remoteNodes.remove(it) }
        nodeSyncMessage.addedNodes.forEach { remoteNodes[it.nodeId] = it }
    }

    private fun schedulePeriodicMulticast() {
        multicastTimer = fixedRateTimer(period = settings.multicastPeriodInMilliseconds) {
            multicast(writeMessage(createMessage(localNode)))
        }
    }

    private fun createMessage(localNode: LocalNode) =
        NodeSyncMessage(addedNodes = listOf(RemoteNode(localNode.nodeId, localNode.hostname, localNode.port)))

    private fun writeMessage(nodeSyncMessage: NodeSyncMessage) = jsonMapper.write(nodeSyncMessage)

    override fun release() {
        check(multicastSocket != null, { "Object not yet acquired!" })
        multicast(writeMessage(createMessage(localNode.nodeId)))
        cancelPeriodicMulticast()
        closeSocket()
        removeNodes()
    }

    private fun multicast(message: String) {
        sendMessage(multicastSocket, settings.multicastAddress, settings.multicastPort, message)
    }

    private fun cancelPeriodicMulticast() {
        multicastTimer?.cancel()
        multicastTimer = null
    }

    private fun closeSocket() {
        multicastSocket?.close()
        multicastSocket = null
    }

    private fun removeNodes() {
        remoteNodes.clear()
    }

    private fun createMessage(nodeId: String) = NodeSyncMessage(removedNodeIds = listOf(nodeId))

    override fun getNode(nodeId: String): RemoteNode? {
        check(multicastSocket != null, { "Object not yet acquired!" })
        return remoteNodes[nodeId]
    }
}
