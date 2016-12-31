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

package org.libreplicator.journal.writer

import org.libreplicator.interactor.api.journal.JournalExistsException
import org.libreplicator.interactor.api.journal.JournalNotCommittedException
import org.libreplicator.interactor.api.journal.JournalNotExistsException
import org.libreplicator.journal.reader.JournalEntryReader
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.journal.JournalEntry
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.NoSuchElementException
import javax.inject.Inject
import javax.inject.Named

class DefaultJournalEntryWriter
@Inject constructor(@Named("journalDirectory") private val journalDirectory: File,
                    private val journalEntryReader: JournalEntryReader,
                    private val fileWriter: FileWriter,
                    private val jsonMapper: JsonMapper) : JournalEntryWriter {
    companion object {
        val COMMITTED_SYMBOL = "COMMITTED"
        val CLOSED_SYMBOL = "CLOSED"
    }

    override fun write(journalEntryId: Long, entry: JournalEntry) {
        val journalFilePath = getJournalEntryFilePath(journalEntryId)
        if (fileWriter.exists(journalFilePath)) {
            throw JournalExistsException()
        }
        fileWriter.write(journalFilePath, jsonMapper.write(entry))
    }

    override fun commit(journalEntryId: Long) {
        val journalEntryFilePath = getJournalEntryFilePath(journalEntryId)
        if (!fileWriter.exists(journalEntryFilePath)) {
            throw JournalNotExistsException()
        }
        fileWriter.append(journalEntryFilePath, COMMITTED_SYMBOL)
    }

    override fun close(journalEntryId: Long) {
        val journalEntry = journalEntryReader.read(journalEntryId)
        if (!journalEntry.committed) {
            throw JournalNotCommittedException()
        }
        fileWriter.append(getJournalEntryFilePath(journalEntryId), CLOSED_SYMBOL)
    }

    override fun delete(journalEntries: List<JournalEntry>) {
        journalEntries.forEach { journalEntry -> fileWriter.delete(getJournalEntryFilePath(journalEntry.id)) }
    }

    override fun deleteAllButLatest() {
        deleteAllButLatestThen {  }
    }

    override fun deleteAllButLatestThen(operation: (JournalEntry) -> Unit) {
        val journalEntries = journalEntryReader.readAll()
        try {
            val latestJournalEntry = journalEntries.last { it.committed || it.closed }

            delete(journalEntries - latestJournalEntry)
            operation(latestJournalEntry)
        }
        catch (noSuchElementException: NoSuchElementException) {
            delete(journalEntries)
        }
    }

    private fun getJournalEntryFilePath(journalEntryId: Long): Path =
            Paths.get(journalDirectory.absolutePath, journalEntryId.toString())
}
