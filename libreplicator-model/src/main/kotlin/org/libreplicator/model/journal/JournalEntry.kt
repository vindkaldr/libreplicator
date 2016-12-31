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

package org.libreplicator.model.journal

import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable

data class JournalEntry
constructor(val id: Long,
            val eventLogs: Set<EventLog>,
            val timeTable: TimeTable,
            val replicatorMessage: ReplicatorMessage,
            val committed: Boolean,
            val closed: Boolean) {
    companion object {
        val EMPTY = JournalEntry(0L, setOf(), TimeTable.EMPTY, ReplicatorMessage.EMPTY, false, false)
    }

    constructor(eventLogs: Set<EventLog>, timeTable: TimeTable, replicatorMessage: ReplicatorMessage) :
            this(0L, eventLogs, timeTable, replicatorMessage, false, false)
}
