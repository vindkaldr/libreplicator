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

package org.libreplicator.interactor.dispatcher

import org.libreplicator.api.LocalLog
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteLog
import org.libreplicator.api.Subscription
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.router.MessageRouter
import org.libreplicator.interactor.state.StateInteractor
import org.libreplicator.log.api.trace
import org.libreplicator.model.ReplicatorMessage
import javax.inject.Inject

class DefaultLogDispatcher @Inject constructor(
        private val messageRouter: MessageRouter,
        private val stateInteractor: StateInteractor
) : LogDispatcher {
    override suspend fun dispatch(localLog: LocalLog) {
        trace("Dispatching event log..")
        stateInteractor.getNodesWithMissingLogs(localLog).forEach {
            messageRouter.routeMessage(it.key, it.value)
        }
    }

    override suspend fun subscribe(observer: Observer<RemoteLog>): Subscription {
        trace("Subscribing to log dispatcher..")
        return messageRouter.subscribe(object : Observer<ReplicatorMessage> {
            override suspend fun observe(observable: ReplicatorMessage) {
                trace("Observed message")
                stateInteractor.getMissingLogs(observable).forEach { observer.observe(it) }
            }
        })
    }
}
