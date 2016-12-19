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

import hu.dreamsequencer.replicator.api.RemoteEventLogObserver
import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogDispatcherFactory
import hu.dreamsequencer.replicator.interactor.api.LogRouterFactory
import javax.inject.Inject
import javax.inject.Provider

class DefaultLogDispatcherFactory
@Inject constructor(private val logRouterFactoryProvider: Provider<LogRouterFactory>) : LogDispatcherFactory {
    override fun create(localNode: ReplicatorNode,
                        remoteNodes: List<ReplicatorNode>,
                        remoteEventLogObserver: RemoteEventLogObserver): LogDispatcher =
            DefaultLogDispatcher(logRouterFactoryProvider.get(), localNode, remoteNodes, remoteEventLogObserver)
}
