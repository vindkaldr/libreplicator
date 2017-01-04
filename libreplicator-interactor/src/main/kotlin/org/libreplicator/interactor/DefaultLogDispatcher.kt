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
import org.libreplicator.interactor.api.ReplicatorJournalProvider
import org.libreplicator.interactor.api.ReplicatorListener
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorJournal
import org.libreplicator.model.ReplicatorJournalStatus
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.ReplicatorState
import java.lang.Thread.sleep
import java.util.Optional
import javax.inject.Inject

class DefaultLogDispatcher
@Inject constructor(logRouterFactory: LogRouterFactory,
                    private val localNode: ReplicatorNode,
                    private val remoteNodes: List<ReplicatorNode>,
                    private val eventLogHandler: EventLogHandler,
                    private val replicatorJournalProvider: Optional<ReplicatorJournalProvider>,
                    private val replicatorListeners: Optional<Set<ReplicatorListener>>) : LogDispatcher {
    private val logRouter = logRouterFactory.create(localNode)
    private var state = ReplicatorState.EMPTY

    override fun dispatch(localEventLog: LocalEventLog) = synchronized(this) {
        updateState(localEventLog)
        updateRemoteNodes()
    }

    override fun subscribe(remoteEventLogObserver: Observer<RemoteEventLog>): Subscription = synchronized(this) {
        replicatorJournalProvider.ifPresent { restoreStateFromBackup(remoteEventLogObserver, it.getJournal()) }
        return logRouter.subscribe(ReplicatorMessageObserver(remoteEventLogObserver))
    }

    override fun hasSubscription(): Boolean = logRouter.hasSubscription()

    private fun updateState(localEventLog: LocalEventLog) {
        val currentTime = getCurrentTimeInMillis()

        state.timeTable[localNode.nodeId, localNode.nodeId] = currentTime
        state.logs.add(EventLog(localNode.nodeId, currentTime, localEventLog.log))
    }

    private fun getCurrentTimeInMillis() : Long {
        fun throttle() = sleep(1)

        val currentTime = System.currentTimeMillis()
        throttle()
        return currentTime
    }

    private fun updateRemoteNodes() {
        eventLogHandler.getNodesWithMissingEventLogs(state.timeTable, remoteNodes, state.logs)
                .forEach { logRouter.send(it.key, ReplicatorMessage(localNode.nodeId, it.value, state.timeTable)) }
    }

    private fun restoreStateFromBackup(observer: Observer<RemoteEventLog>, journal: ReplicatorJournal) {
        when (journal.status) {
            ReplicatorJournalStatus.RECOVER -> recoverState(observer, journal)
            ReplicatorJournalStatus.RESTORE  -> restoreState(journal)
        }
    }

    private fun recoverState(observer: Observer<RemoteEventLog>, journal: ReplicatorJournal) {
        state = ReplicatorState.copy(journal.replicatorState)
        updateClient(observer, journal.lastReplicatorMessage)
        updateState(journal.lastReplicatorMessage)
    }

    private fun restoreState(journal: ReplicatorJournal) {
        state = ReplicatorState.copy(journal.replicatorState)
    }

    private fun updateClient(observer: Observer<RemoteEventLog>, message: ReplicatorMessage) {
        eventLogHandler.getMissingEventLogs(state.timeTable, localNode, message.eventLogs)
                .forEach { observer.observe(it) }
    }

    private fun updateState(message: ReplicatorMessage) {
        state.logs.addAll(message.eventLogs)
        state.timeTable.mergeRow(localNode.nodeId, message.timeTable, message.nodeId)
        state.timeTable.merge(message.timeTable)

        val logsToRemove = eventLogHandler.getDistributedEventLogs(state.timeTable, remoteNodes, state.logs)
        state.logs.removeAll(logsToRemove)
    }

    inner class ReplicatorMessageObserver
    constructor(private val remoteEventLogObserver: Observer<RemoteEventLog>) : Observer<ReplicatorMessage> {
        override fun observe(observable: ReplicatorMessage): Unit = synchronized(this@DefaultLogDispatcher) {
            val currentTime = getCurrentTimeInMillis()

            replicatorListeners.ifPresent { listeners ->
                listeners.forEach { listener -> listener.beforeReplicatorStateUpdate(currentTime, state, observable) }
            }

            updateClient(remoteEventLogObserver, observable)
            updateState(observable)

            replicatorListeners.ifPresent { listeners ->
                listeners.forEach { listener -> listener.afterReplicatorStateUpdate(currentTime, state) }
            }
        }
    }
}
