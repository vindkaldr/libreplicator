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

package org.libreplicator

import org.libreplicator.api.Replicator
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.boundary.DefaultReplicator
import org.libreplicator.interactor.DefaultLogDispatcher
import org.libreplicator.interactor.api.ReplicatorStateListener
import org.libreplicator.json.DefaultJsonMapper
import org.libreplicator.model.ReplicatorState
import org.libreplicator.network.DefaultLogRouter

class ReplicatorFactory {
    fun create(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>): Replicator {
        val jsonMapper = DefaultJsonMapper()
        val logRouter = DefaultLogRouter(jsonMapper, localNode)

        val replicatorState = ReplicatorState.copy(ReplicatorState.EMPTY)
        val replicatorStateListener = object : ReplicatorStateListener {}

        val logDispatcher = DefaultLogDispatcher(logRouter, replicatorState, replicatorStateListener, localNode, remoteNodes)

        return DefaultReplicator(logDispatcher)
    }
}
