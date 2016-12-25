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

import org.libreplicator.api.ReplicatorNode
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.api.LogDispatcherFactory
import org.libreplicator.interactor.api.LogRouterFactory
import javax.inject.Inject

class DefaultLogDispatcherFactory
@Inject constructor(private val logRouterFactory: LogRouterFactory) : LogDispatcherFactory {
    override fun create(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>): LogDispatcher =
            DefaultLogDispatcher(logRouterFactory, localNode, remoteNodes)
}
