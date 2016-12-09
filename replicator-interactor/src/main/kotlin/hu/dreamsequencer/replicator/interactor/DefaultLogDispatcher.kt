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

package hu.dreamsequencer.replicator.interactor

import com.google.inject.assistedinject.Assisted
import hu.dreamsequencer.replicator.api.LocalEventLog
import hu.dreamsequencer.replicator.api.RemoteEventLogObserver
import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogRouterFactory
import hu.dreamsequencer.replicator.model.EventLog
import hu.dreamsequencer.replicator.model.ReplicatorMessage
import hu.dreamsequencer.replicator.model.TimeTable
import javax.inject.Inject

internal class DefaultLogDispatcher
@Inject constructor(logRouterFactory: LogRouterFactory,
                    @Assisted private val localNode: ReplicatorNode,
                    @Assisted private val remoteNodes: List<ReplicatorNode>,
                    @Assisted private val remoteEventLogObserver: RemoteEventLogObserver) : LogDispatcher {

    companion object {
        private val NUMBER_OF_LOCAL_NODES = 1
    }

    private val eventLogs = mutableSetOf<EventLog>()
    private val timeTable = TimeTable(NUMBER_OF_LOCAL_NODES + remoteNodes.size)

    private val logRouter = logRouterFactory.create(localNode, this)

    override fun dispatch(localEventLog: LocalEventLog) = synchronized(this) {
        val currentTime = getCurrentTime()

        updateTimeTable(currentTime)
        addToEventLogs(currentTime, localEventLog.log)
        updateRemoteNodes()
    }

    override fun receive(replicatorMessage: ReplicatorMessage) = synchronized(this) {
        notifyObserver(replicatorMessage.eventLogs)

        addToEventLogs(replicatorMessage.eventLogs)
        updateTimeTable(replicatorMessage.timeTable)
        cleanUpEventLogs()
    }

    private fun getCurrentTime(): Long {
        return System.currentTimeMillis();
    }

    private fun updateTimeTable(currentTime: Long) {
        timeTable[localNode.nodeId, localNode.nodeId] = currentTime
    }

    private fun addToEventLogs(currentTime: Long, log: String) {
        eventLogs.add(EventLog(localNode.nodeId, currentTime, log))
    }

    private fun updateRemoteNodes() {
        remoteNodes.forEach { remoteNode ->
            val missingLogs = eventLogs.filter { !hasEventLog(remoteNode, it) }.sortedBy { it.time }
            if (!missingLogs.isEmpty()) {
                logRouter.send(remoteNode, ReplicatorMessage(missingLogs, timeTable))
            }
        }
    }

    private fun hasEventLog(replicatorNode: ReplicatorNode, eventLog: EventLog): Boolean {
        return eventLog.time <= timeTable[replicatorNode.nodeId, eventLog.nodeId]
    }

    private fun addToEventLogs(remoteEventLogs: List<EventLog>) {
        eventLogs.addAll(remoteEventLogs)
    }

    private fun notifyObserver(remoteEventLogs: List<EventLog>) {
        remoteEventLogs.filter { !hasEventLog(localNode, it) }
                .sortedBy { it.time }
                .forEach { remoteEventLogObserver.observe(it) }
    }

    private fun updateTimeTable(remoteTimeTable: TimeTable) {
        timeTable.mergeRow(remoteTimeTable, localNode.nodeId)
        timeTable.merge(remoteTimeTable)
    }

    private fun cleanUpEventLogs() {
        eventLogs.removeAll { eventLog ->
            remoteNodes.all { remoteNode ->
                hasEventLog(remoteNode, eventLog)
            }
        }
    }
}
