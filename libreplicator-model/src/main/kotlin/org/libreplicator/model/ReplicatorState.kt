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

package org.libreplicator.model

import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.Node
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteLog
import org.libreplicator.api.RemoteNode
import org.libreplicator.api.Subscribable
import org.libreplicator.api.Subscription

data class ReplicatorState constructor(
        private var logs: MutableSet<RemoteLog> = mutableSetOf(),
        private var timeTable: TimeTable = TimeTable()
): Subscribable<ReplicatorState> {
    private var observer: Observer<ReplicatorState> = object : Observer<ReplicatorState> {
        override suspend fun observe(observable: ReplicatorState) {}
    }

    override suspend fun subscribe(observer: Observer<ReplicatorState>): Subscription {
        this.observer = observer
        return object : Subscription {
            override suspend fun unsubscribe() {}
        }
    }

    suspend fun addLocalEventLog(localNode: LocalNode, localLog: LocalLog) {
        val currentTime = getCurrentTimeInMillis()

        updateTimeOfNode(localNode, currentTime)
        addLogs(RemoteLog(localNode.nodeId, currentTime, localLog.log))

        observer.observe(this)
    }

    suspend fun updateFromMessage(localNode: LocalNode, remoteNodes: List<RemoteNode>, message: ReplicatorMessage) {
        logs.addAll(message.eventLogs)
        timeTable.merge(localNode.nodeId, message)

        val logsToRemove = getDistributedEventLogs(remoteNodes)
        logs.removeAll(logsToRemove)

        observer.observe(this)
    }

    fun getNodesWithMissingEventLogs(localNode: LocalNode, remoteNodes: List<RemoteNode>): Map<RemoteNode, ReplicatorMessage> =
            remoteNodes.map { node -> node.to(getMissingEventLogs(node)) }
                    .filter { it.second.isNotEmpty() }
                    .toMap()
                    .mapValues { ReplicatorMessage(localNode.nodeId, it.value, timeTable) }

    fun getMissingEventLogs(node: Node, eventLogs: List<RemoteLog> = logs.toList()): List<RemoteLog> =
            eventLogs.filter { !hasEventLog(node, it) }
                    .sortedBy { it.time }

    private fun getCurrentTimeInMillis() : Long {
        fun throttle() = Thread.sleep(1)

        val currentTime = System.currentTimeMillis()
        throttle()
        return currentTime
    }

    private fun updateTimeOfNode(localNode: LocalNode, time: Long) {
        timeTable[localNode.nodeId, localNode.nodeId] = time
    }

    private fun addLogs(vararg eventLogs: RemoteLog) {
        logs.addAll(eventLogs)
    }

    private fun getDistributedEventLogs(nodes: List<RemoteNode>): List<RemoteLog> =
            logs.filter { log ->
                nodes.all { node ->
                    hasEventLog(node, log)
                }
            }

    private fun hasEventLog(node: Node, eventLog: RemoteLog): Boolean =
            eventLog.time <= timeTable[node.nodeId, eventLog.nodeId]

    // Do not change or call. These are here for serialization/deserialization purposes.
    private fun getLogs() = logs
    private fun getTimeTable() = timeTable
}
