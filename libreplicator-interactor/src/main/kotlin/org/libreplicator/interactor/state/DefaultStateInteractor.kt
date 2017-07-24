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

package org.libreplicator.interactor.state

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteLog
import org.libreplicator.api.RemoteNode
import org.libreplicator.interactor.state.interaction.StateInteraction
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.ReplicatorState
import javax.inject.Inject

class DefaultStateInteractor @Inject constructor(
        private val replicatorState: ReplicatorState,
        private val localNode: LocalNode,
        private val remoteNodes: List<RemoteNode>
) : StateInteractor {
    private val replicatorStateActor = actor<StateInteraction>(CommonPool) {
        for (interaction in channel) {
            when (interaction) {
                is StateInteraction.ObserveLocalEvent -> {
                    replicatorState.addLocalEventLog(localNode, interaction.localLog)
                    interaction.channel.send(replicatorState.getNodesWithMissingEventLogs(localNode, remoteNodes))
                }
                is StateInteraction.ObserveRemoteMessage -> {
                    val missingEventLogs = replicatorState.getMissingEventLogs(localNode, interaction.message.eventLogs)
                    replicatorState.updateFromMessage(localNode, remoteNodes, interaction.message)
                    interaction.channel.send(missingEventLogs)
                }
            }
        }
    }

    override suspend fun getNodesWithMissingLogs(localLog: LocalLog): Map<RemoteNode, ReplicatorMessage> {
        val channel = Channel<Map<RemoteNode, ReplicatorMessage>>()

        val interaction = StateInteraction.ObserveLocalEvent(localLog, channel)
        replicatorStateActor.send(interaction)

        return channel.receive()
    }

    override suspend fun getMissingLogs(message: ReplicatorMessage): List<RemoteLog> {
        val channel = Channel<List<RemoteLog>>()

        val interaction = StateInteraction.ObserveRemoteMessage(message, channel)
        replicatorStateActor.send(interaction)

        return channel.receive()
    }
}
