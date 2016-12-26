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

package org.libreplicator.interactor

import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.api.LogRouterFactory
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import javax.inject.Inject

class DefaultLogDispatcher
@Inject constructor(logRouterFactory: LogRouterFactory,
                    private val localNode: ReplicatorNode,
                    private val remoteNodes: List<ReplicatorNode>) : LogDispatcher {
    companion object {
        private val NUMBER_OF_LOCAL_NODES = 1
    }

    private val eventLogs = mutableSetOf<EventLog>()
    private val timeTable = TimeTable(NUMBER_OF_LOCAL_NODES + remoteNodes.size)

    private val logRouter = logRouterFactory.create(localNode)

    override fun dispatch(localEventLog: LocalEventLog) = synchronized(this) {
        val currentTime = getCurrentTime()

        updateTimeTable(currentTime)
        addToEventLogs(currentTime, localEventLog.log)
        updateRemoteNodes()
    }

    override fun subscribe(observer: Observer<RemoteEventLog>): Subscription = synchronized(this) {
        return logRouter.subscribe(object : Observer<ReplicatorMessage> {
            override fun observe(observable: ReplicatorMessage) = synchronized(this@DefaultLogDispatcher) {
                receive(observer, observable)
            }
        })
    }

    override fun hasSubscription(): Boolean = logRouter.hasSubscription()

    private fun receive(observer: Observer<RemoteEventLog>, message: ReplicatorMessage) {
        notifyObserver(observer, message.eventLogs)

        addToEventLogs(message.eventLogs)
        updateTimeTable(message)
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
                logRouter.send(remoteNode, ReplicatorMessage(localNode.nodeId, missingLogs, timeTable))
            }
        }
    }

    private fun hasEventLog(replicatorNode: ReplicatorNode, eventLog: EventLog): Boolean {
        return eventLog.time <= timeTable[replicatorNode.nodeId, eventLog.nodeId]
    }

    private fun addToEventLogs(remoteEventLogs: List<EventLog>) {
        eventLogs.addAll(remoteEventLogs)
    }

    private fun notifyObserver(remoteEventLogObserver: Observer<RemoteEventLog>, remoteEventLogs: List<EventLog>) {
        remoteEventLogs.filter { !hasEventLog(localNode, it) }
                .sortedBy { it.time }
                .forEach { remoteEventLogObserver.observe(it) }
    }

    private fun updateTimeTable(replicatorMessage: ReplicatorMessage) {
        timeTable.mergeRow(localNode.nodeId, replicatorMessage.timeTable, replicatorMessage.nodeId)
        timeTable.merge(replicatorMessage.timeTable)
    }

    private fun cleanUpEventLogs() {
        eventLogs.removeAll { eventLog ->
            remoteNodes.all { remoteNode ->
                hasEventLog(remoteNode, eventLog)
            }
        }
    }
}
