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

package org.libreplicator.model.time

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class TimeProviderInteractor(private val timeProvider: TimeProvider) : TimeProvider {
    private val actor = actor<TimeProviderInteraction>(CommonPool) {
        for (interaction in channel) {
            when (interaction) {
                is TimeProviderInteraction.GetTime -> {
                    interaction.channel.send(timeProvider.getTime())
                }
            }
        }
    }

    suspend override fun getTime(): Long {
        val channel = Channel<Long>()
        actor.send(TimeProviderInteraction.GetTime(channel))
        return channel.receive()
    }
}
