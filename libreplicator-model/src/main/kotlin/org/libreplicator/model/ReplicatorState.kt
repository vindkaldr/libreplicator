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

import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.Observer
import org.libreplicator.api.ReplicatorNode

data class ReplicatorState constructor(
        var logs: MutableSet<EventLog> = mutableSetOf(),
        var timeTable: TimeTable = TimeTable()) : Bindable<ReplicatorState> {

    private var observer: Observer<ReplicatorState> = object : Observer<ReplicatorState> {
        override fun observe(observable: ReplicatorState) {
        }
    }

    override fun bind(observer: Observer<ReplicatorState>) = synchronized(this) {
        this.observer = observer
    }

    fun addLocalEventLog(localNode: ReplicatorNode, localEventLog: LocalEventLog) {
        val currentTime = getCurrentTimeInMillis()

        updateTimeOfNode(localNode, currentTime)
        addLogs(EventLog(localNode.nodeId, currentTime, localEventLog.log))

        observer.observe(this)
    }

    private fun getCurrentTimeInMillis() : Long {
        fun throttle() = Thread.sleep(1)

        val currentTime = System.currentTimeMillis()
        throttle()
        return currentTime
    }

    private fun updateTimeOfNode(replicatorNode: ReplicatorNode, time: Long) {
        timeTable[replicatorNode.nodeId, replicatorNode.nodeId] = time
    }

    private fun addLogs(vararg eventLogs: EventLog) {
        logs.addAll(eventLogs)
    }

    fun updateFromMessage(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>, message: ReplicatorMessage) {
        logs.addAll(message.eventLogs)
        timeTable.merge(localNode.nodeId, message)

        val logsToRemove = getDistributedEventLogs(remoteNodes)
        logs.removeAll(logsToRemove)

        observer.observe(this)
    }

    fun getNodesWithMissingEventLogs(nodes: List<ReplicatorNode>): Map<ReplicatorNode, List<EventLog>> =
            nodes.map { node ->
                node.to(getMissingEventLogs(node)) }
                    .filter { it.second.isNotEmpty() }
                    .toMap()

    fun getMissingEventLogs(localNode: ReplicatorNode, eventLogs: List<EventLog> = logs.toList()): List<EventLog> =
            eventLogs.filter { !hasEventLog(localNode, it) }
                    .sortedBy { it.time }

    private fun getDistributedEventLogs(nodes: List<ReplicatorNode>): List<EventLog> =
            logs.filter { log ->
                nodes.all { node ->
                    hasEventLog(node, log)
                }
            }

    private fun hasEventLog(node: ReplicatorNode, eventLog: EventLog): Boolean =
            eventLog.time <= timeTable[node.nodeId, eventLog.nodeId]
}
