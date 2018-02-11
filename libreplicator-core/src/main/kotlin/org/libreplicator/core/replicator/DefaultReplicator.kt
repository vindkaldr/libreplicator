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

package org.libreplicator.core.replicator

import org.libreplicator.api.LocalLog
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteLog
import org.libreplicator.api.Replicator
import org.libreplicator.api.Subscription
import org.libreplicator.core.router.api.MessageRouter
import org.libreplicator.core.interactor.api.StateInteractor
import org.libreplicator.core.wrapper.api.PayloadWrapper
import org.libreplicator.log.api.trace
import org.libreplicator.model.ReplicatorMessage
import javax.inject.Inject

class DefaultReplicator @Inject constructor(
    private val groupId: String,
    private val stateInteractor: StateInteractor,
    private val payloadWrapper: PayloadWrapper,
    private val messageRouter: MessageRouter
) : Replicator {
    override suspend fun replicate(localLog: String) {
        replicate(LocalLog(localLog))
    }

    override suspend fun replicate(localLog: LocalLog) {
        trace("Replicating event log..")
        stateInteractor.getNodesWithMissingLogs(localLog).forEach {
            messageRouter.routeMessage(it.key, payloadWrapper.wrap(it.value))
        }
    }

    override suspend fun subscribe(observer: Observer<RemoteLog>): Subscription {
        trace("Subscribing to replicator..")
        return messageRouter.subscribe(groupId, object : Observer<ReplicatorMessage> {
            override suspend fun observe(observable: ReplicatorMessage) {
                trace("Observed message")
                stateInteractor.getMissingLogs(payloadWrapper.unwrap(observable)).forEach { observer.observe(it) }
            }
        })
    }
}
