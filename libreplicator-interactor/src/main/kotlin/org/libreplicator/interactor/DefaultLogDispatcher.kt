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
import org.libreplicator.interactor.api.ReplicatorStateListener
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.ReplicatorState
import org.libreplicator.network.api.LogRouter

class DefaultLogDispatcher
constructor(private val logRouter: LogRouter,
            private val replicatorState: ReplicatorState,
            private val replicatorStateListener: ReplicatorStateListener,
            private val localNode: ReplicatorNode,
            private val remoteNodes: List<ReplicatorNode>) : LogDispatcher {

    override fun dispatch(localEventLog: LocalEventLog) = synchronized(this) { listenedTo {
        replicatorState.addLocalEventLog(localNode, localEventLog)
        replicatorState.getNodesWithMissingEventLogs(remoteNodes)
                .forEach { logRouter.send(it.key, ReplicatorMessage(localNode.nodeId, it.value, replicatorState.timeTable)) }
    }}

    override fun subscribe(remoteEventLogObserver: Observer<RemoteEventLog>): Subscription = synchronized(this) {
        return logRouter.subscribe(ReplicatorMessageObserver(remoteEventLogObserver))
    }

    override fun hasSubscription(): Boolean = logRouter.hasSubscription()

    private fun listenedTo(block: () -> Unit) {
        replicatorStateListener.replicatorStateChanged(replicatorState)
        block()
        replicatorStateListener.replicatorStateChanged(replicatorState)
    }

    inner class ReplicatorMessageObserver
    constructor(private val remoteEventLogObserver: Observer<RemoteEventLog>) : Observer<ReplicatorMessage> {
        override fun observe(observable: ReplicatorMessage) = synchronized(this@DefaultLogDispatcher) { listenedTo {
            updateClient(remoteEventLogObserver, observable)
            updateState(observable)
        }}

        private fun updateClient(observer: Observer<RemoteEventLog>, message: ReplicatorMessage) {
            replicatorState.getMissingEventLogs(localNode, message.eventLogs)
                    .forEach { observer.observe(it) }
        }

        private fun updateState(message: ReplicatorMessage) {
            replicatorState.updateFromMessage(localNode, remoteNodes, message)
        }
    }
}
