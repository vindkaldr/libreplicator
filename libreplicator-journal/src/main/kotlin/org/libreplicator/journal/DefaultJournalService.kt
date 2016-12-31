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

package org.libreplicator.journal

import org.libreplicator.interactor.api.journal.JournalService
import org.libreplicator.journal.reader.JournalEntryReader
import org.libreplicator.journal.writer.JournalEntryWriter
import org.libreplicator.model.journal.JournalEntry
import java.lang.Thread.sleep
import javax.inject.Inject

class DefaultJournalService
@Inject constructor(private val journalEntryWriter: JournalEntryWriter) : JournalService {
    private companion object {
        private var closeOperationCounter = 0
    }

    override fun write(entry: JournalEntry): Long = synchronized(this) {
        val journalId = getJournalEntryId()
        journalEntryWriter.write(journalId, entry)
        return journalId
    }

    override fun commit(journalEntryId: Long) = synchronized(this) {
        journalEntryWriter.commit(journalEntryId)
    }

    override fun close(journalEntryId: Long) = synchronized(this) {
        journalEntryWriter.close(journalEntryId)

        if (++closeOperationCounter == 10) {
            journalEntryWriter.deleteAllButLatest()
            closeOperationCounter = 0
        }
    }

    override fun getLatestJournalEntryThen(operation: (JournalEntry) -> Unit) = synchronized(this) {
        journalEntryWriter.deleteAllButLatestThen { operation(it) }
    }

    private fun getJournalEntryId(): Long {
        fun throttle() = sleep(1)

        val currentTime = System.currentTimeMillis()
        throttle()
        return currentTime
    }
}
