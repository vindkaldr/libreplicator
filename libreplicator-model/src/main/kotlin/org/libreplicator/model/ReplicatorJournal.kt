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

package org.libreplicator.model

data class ReplicatorJournal(val replicatorState: ReplicatorState,
                             val lastReplicatorMessage: ReplicatorMessage,
                             val status: ReplicatorJournalStatus = ReplicatorJournalStatus.RESTORE) {
    companion object {
        val EMPTY = ReplicatorJournal(ReplicatorState.EMPTY, ReplicatorMessage.EMPTY, ReplicatorJournalStatus.RESTORE)
    }

    constructor(replicatorState: ReplicatorState) :
            this(replicatorState, ReplicatorMessage.EMPTY, ReplicatorJournalStatus.RESTORE)

    constructor(replicatorState: ReplicatorState, lastReplicatorMessage: ReplicatorMessage) :
            this(replicatorState, lastReplicatorMessage, ReplicatorJournalStatus.RESTORE)
}
