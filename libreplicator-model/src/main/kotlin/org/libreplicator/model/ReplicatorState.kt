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

package org.libreplicator.model

import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.Node
import org.libreplicator.api.Observable
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteLog
import org.libreplicator.api.RemoteNode
import org.libreplicator.api.Subscription
import org.libreplicator.model.time.TimeProviderInteractor
import org.libreplicator.model.time.epoch.EpochTimeProvider
import org.libreplicator.model.time.unique.UniqueTimeProvider

data class ReplicatorState constructor(
        private val localNode: LocalNode? = null,
        private val remoteNodes: List<RemoteNode>? = null,
        private var logs: MutableSet<RemoteLog> = mutableSetOf(),
        private var timeTable: TimeTable = TimeTable()
): Observable<ReplicatorState> {
    private val timeProvider = TimeProviderInteractor(UniqueTimeProvider(EpochTimeProvider()))

    private var observer: Observer<ReplicatorState> = object : Observer<ReplicatorState> {
        override suspend fun observe(observable: ReplicatorState) {}
    }

    override suspend fun subscribe(observer: Observer<ReplicatorState>): Subscription {
        this.observer = observer
        return object : Subscription {
            override suspend fun unsubscribe() {}
        }
    }

    suspend fun addLocalEventLog(localLog: LocalLog) {
        val currentTime = timeProvider.getTime()

        updateTimeOfNode(currentTime)
        addLogs(RemoteLog(localNode!!.nodeId, currentTime, localLog.log))

        observer.observe(this)
    }

    suspend fun updateFromMessage(payload: ReplicatorPayload) {
        logs.addAll(payload.eventLogs)
        timeTable.merge(localNode!!.nodeId, payload)

        val logsToRemove = getDistributedEventLogs()
        logs.removeAll(logsToRemove)

        observer.observe(this)
    }

    fun getNodesWithMissingEventLogs(): Map<RemoteNode, ReplicatorPayload> =
            remoteNodes!!.map { node -> node to getMissingEventLogs(node) }
                    .filter { it.second.isNotEmpty() }
                    .toMap()
                    .mapValues { ReplicatorPayload(localNode!!.nodeId, it.value, timeTable) }

    fun getMissingEventLogs(node: Node = localNode!!, eventLogs: List<RemoteLog> = logs.toList()): List<RemoteLog> =
            eventLogs.filter { !hasEventLog(node, it) }
                    .sortedBy { it.time }

    private fun updateTimeOfNode(time: Long) {
        timeTable[localNode!!.nodeId, localNode.nodeId] = time
    }

    private fun addLogs(vararg eventLogs: RemoteLog) {
        logs.addAll(eventLogs)
    }

    private fun getDistributedEventLogs(): List<RemoteLog> =
            logs.filter { log ->
                remoteNodes!!.all { node ->
                    hasEventLog(node, log)
                }
            }

    private fun hasEventLog(node: Node, eventLog: RemoteLog): Boolean =
            eventLog.time <= timeTable[node.nodeId, eventLog.nodeId]

    // Do not change or call. These are here for serialization/deserialization purposes.
    private fun getLogs() = logs
    private fun getTimeTable() = timeTable
}
