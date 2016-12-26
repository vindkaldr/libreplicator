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

package org.libreplicator.boundary

import org.libreplicator.api.AlreadySubscribedException
import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.api.Replicator
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.interactor.api.LogDispatcherFactory
import javax.inject.Inject

class DefaultReplicator
@Inject constructor(logDispatcherFactory: LogDispatcherFactory,
                    localNode: ReplicatorNode,
                    remoteNodes:List<ReplicatorNode>) : Replicator {

    private val logDispatcher = logDispatcherFactory.create(localNode, remoteNodes)

    override fun replicate(localEventLog: LocalEventLog) {
        if (!logDispatcher.hasSubscription()) {
            throw NotSubscribedException()
        }
        logDispatcher.dispatch(localEventLog)
    }

    override fun subscribe(remoteEventLogObserver: Observer<RemoteEventLog>): Subscription {
        if (logDispatcher.hasSubscription()) {
            throw AlreadySubscribedException()
        }
        return logDispatcher.subscribe(remoteEventLogObserver)
    }
}
