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
import org.libreplicator.interactor.api.journal.JournalService
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.api.LogRouterFactory
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.model.journal.JournalEntry
import java.lang.Thread.sleep
import javax.inject.Inject

class DefaultLogDispatcher
@Inject constructor(logRouterFactory: LogRouterFactory,
                    private val localNode: ReplicatorNode,
                    private val remoteNodes: List<ReplicatorNode>,
                    private val eventLogHandler: EventLogHandler,
                    private val journalService: JournalService) : LogDispatcher {
    companion object {
        private val NUMBER_OF_LOCAL_NODES = 1
    }

    private val logRouter = logRouterFactory.create(localNode)

    private var eventLogs = mutableSetOf<EventLog>()
    private var timeTable = TimeTable(NUMBER_OF_LOCAL_NODES + remoteNodes.size)

    override fun dispatch(localEventLog: LocalEventLog) = synchronized(this) {
        fun throttleDispatching() = sleep(1)

        updateTimeTableAndEventLogs(localEventLog)
        updateRemoteNodesWithMissingEventLogs()
        throttleDispatching()
    }

    override fun subscribe(observer: Observer<RemoteEventLog>): Subscription = synchronized(this) {
        journalService.getLatestJournalEntryThen { restoreStateFromJournal(observer, it) }

        return logRouter.subscribe(object : Observer<ReplicatorMessage> {
            override fun observe(observable: ReplicatorMessage): Unit = synchronized(this@DefaultLogDispatcher) {
                writeJournalAndNotifyObserver(observer, observable)
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

    private fun restoreStateFromJournal(observer: Observer<RemoteEventLog>, journalEntry: JournalEntry) {
        if (journalEntry.closed) {
            recoverFromJournalEntry(journalEntry)
        }
        else if (journalEntry.committed) {
            replayJournalEntry(observer, journalEntry)
        }
    }

    private fun recoverFromJournalEntry(journalEntry: JournalEntry) {
        eventLogs = journalEntry.eventLogs.toMutableSet()
        timeTable = journalEntry.timeTable

        updateEventLogsAndTimeTable(journalEntry.replicatorMessage)
        removeDistributedEventLogs()
    }

    private fun replayJournalEntry(observer: Observer<RemoteEventLog>, journalEntry: JournalEntry) {
        eventLogs = journalEntry.eventLogs.toMutableSet()
        timeTable = journalEntry.timeTable

        notifyObserver(observer, journalEntry.eventLogs.sortedBy { it.time })

        updateEventLogsAndTimeTable(journalEntry.replicatorMessage)
        removeDistributedEventLogs()

        journalService.close(journalEntry.id)
    }

    private fun writeJournalAndNotifyObserver(observer: Observer<RemoteEventLog>, message: ReplicatorMessage) {
        val journalId = journalService.write(JournalEntry(eventLogs.toSet(), timeTable, message))
        journalService.commit(journalId)

        notifyObserver(observer, message.eventLogs)

        journalService.close(journalId)
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
