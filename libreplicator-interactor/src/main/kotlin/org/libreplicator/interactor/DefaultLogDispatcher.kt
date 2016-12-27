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
import java.lang.Thread.sleep
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

    private val eventLogHandler = EventLogHandler()

    override fun dispatch(localEventLog: LocalEventLog) = synchronized(this) {
        fun throttleDispatching() = sleep(1)

        updateTimeTableAndEventLogs(localEventLog)
        updateRemoteNodesWithMissingEventLogs()
        throttleDispatching()
    }

    override fun subscribe(observer: Observer<RemoteEventLog>): Subscription = synchronized(this) {
        return logRouter.subscribe(object : Observer<ReplicatorMessage> {
            override fun observe(observable: ReplicatorMessage): Unit = synchronized(this@DefaultLogDispatcher) {
                notifyObserver(observer, observable.eventLogs)
                updateEventLogsAndTimeTable(observable)
                removeDistributedEventLogs()
            }
        })
    }

    override fun hasSubscription(): Boolean = logRouter.hasSubscription()

    private fun updateTimeTableAndEventLogs(localEventLog: LocalEventLog) {
        val currentTime = System.currentTimeMillis()

        timeTable[localNode.nodeId, localNode.nodeId] = currentTime
        eventLogs.add(EventLog(localNode.nodeId, currentTime, localEventLog.log))
    }

    private fun updateRemoteNodesWithMissingEventLogs() {
        eventLogHandler.getNodesWithMissingEventLogs(timeTable, remoteNodes, eventLogs)
                .forEach { logRouter.send(it.key, ReplicatorMessage(localNode.nodeId, it.value, timeTable)) }
    }

    private fun notifyObserver(remoteEventLogObserver: Observer<RemoteEventLog>, remoteEventLogs: List<EventLog>) {
        eventLogHandler.getMissingEventLogs(timeTable, localNode, remoteEventLogs)
                .forEach { remoteEventLogObserver.observe(it) }
    }

    private fun updateEventLogsAndTimeTable(message: ReplicatorMessage) {
        eventLogs.addAll(message.eventLogs)
        timeTable.mergeRow(localNode.nodeId, message.timeTable, message.nodeId)
        timeTable.merge(message.timeTable)
    }

    private fun removeDistributedEventLogs() {
        eventLogs.removeAll(eventLogHandler.getDistributedEventLogs(timeTable, remoteNodes, eventLogs))
    }
}
