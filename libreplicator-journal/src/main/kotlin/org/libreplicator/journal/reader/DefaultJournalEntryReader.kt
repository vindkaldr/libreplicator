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

package org.libreplicator.journal.reader

import org.libreplicator.interactor.api.journal.JournalNotExistsException
import org.libreplicator.journal.writer.DefaultJournalEntryWriter
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.journal.JournalEntry
import java.io.File
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named

class DefaultJournalEntryReader
@Inject constructor(@Named("journalDirectory") private val journalDirectory: File,
                    private val fileReader: FileReader,
                    private val jsonMapper: JsonMapper) : JournalEntryReader {
    private companion object {
        private val JOURNAL_ENTRY_POSITION = 0
        private val COMMITTED_SYMBOL_POSITION = 1
        private val CLOSED_SYMBOL_POSITION = 2
    }

    override fun read(journalEntryId: Long): JournalEntry {
        val journalEntryFile = getJournalEntryFile(journalEntryId)
        if (!journalEntryFile.exists()) {
            throw JournalNotExistsException()
        }
        return readJournalEntry(journalEntryId, fileReader.readAllLines(journalEntryFile))
    }

    override fun readAll(): List<JournalEntry> =
            journalDirectory.listFiles()
                    .map { it.name.toLong() to fileReader.readAllLines(it) }
                    .map { readJournalEntry(it.first, it.second) }
                    .sortedBy { it.id }

    private fun readJournalEntry(journalEntryId: Long, journalEntryFileLines: List<String>): JournalEntry {
        if (journalEntryFileLines.isEmpty()) {
            return JournalEntry.EMPTY.copy(journalEntryId)
        }

        val journalEntryLine = journalEntryFileLines[JOURNAL_ENTRY_POSITION]
        try {
            if (isNotCommittedOrClosedJournalEntry(journalEntryFileLines)) {
                return readJournalEntry(journalEntryId, journalEntryLine)
            }
            else if (isCommittedJournalEntry(journalEntryFileLines)) {
                return readCommittedJournalEntry(journalEntryId, journalEntryLine)
            }
            else if (isClosedJournalEntry(journalEntryFileLines)) {
                return readClosedJournalEntry(journalEntryId, journalEntryLine)
            }
            else if (isBrokenCommittedJournalEntry(journalEntryFileLines)) {
                return readJournalEntry(journalEntryId, journalEntryLine)
            }
            else if (isBrokenClosedJournalEntry(journalEntryFileLines)) {
                return readCommittedJournalEntry(journalEntryId, journalEntryLine)
            }
        }
        catch (jsonReadException: JsonReadException) {
            // Ignore it. In such case we want to return with an empty journal.
        }
        return JournalEntry.EMPTY.copy(id = journalEntryId)
    }

    private fun isNotCommittedOrClosedJournalEntry(journalEntryFileLines: List<String>) =
            (journalEntryFileLines.size == 1)

    private fun readJournalEntry(journalEntryId: Long, journalEntryLine: String) =
            readJournalEntry(journalEntryLine).copy(id = journalEntryId, committed = false, closed = false)

    private fun isCommittedJournalEntry(journalEntryFileLines: List<String>) =
            (journalEntryFileLines.size == 2) &&
                    (journalEntryFileLines[COMMITTED_SYMBOL_POSITION] == DefaultJournalEntryWriter.COMMITTED_SYMBOL)

    private fun readCommittedJournalEntry(journalEntryId: Long, journalEntryLine: String) =
            readJournalEntry(journalEntryLine).copy(id = journalEntryId, committed = true, closed = false)

    private fun readJournalEntry(journalEntryLine: String) = jsonMapper.read(journalEntryLine, JournalEntry::class)

    private fun isClosedJournalEntry(journalEntryFileLines: List<String>) =
            (journalEntryFileLines.size == 3) &&
                    (journalEntryFileLines[COMMITTED_SYMBOL_POSITION] == DefaultJournalEntryWriter.COMMITTED_SYMBOL) &&
                    (journalEntryFileLines[CLOSED_SYMBOL_POSITION] == DefaultJournalEntryWriter.CLOSED_SYMBOL)

    private fun readClosedJournalEntry(journalEntryId: Long, journalEntryLine: String) =
            readJournalEntry(journalEntryLine).copy(id = journalEntryId, committed = true, closed = true)

    private fun isBrokenCommittedJournalEntry(journalEntryFileLines: List<String>) =
            (journalEntryFileLines.size == 2)

    private fun isBrokenClosedJournalEntry(journalEntryFileLines: List<String>) =
            (journalEntryFileLines.size == 3) &&
                    (journalEntryFileLines[COMMITTED_SYMBOL_POSITION] == DefaultJournalEntryWriter.COMMITTED_SYMBOL)

    private fun getJournalEntryFile(journalId: Long): File =
            Paths.get(journalDirectory.absolutePath, journalId.toString()).toFile()
}
