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

package org.libreplicator.journal.read

import org.libreplicator.interactor.api.ReplicatorJournalProvider
import org.libreplicator.journal.JournalNotExistsException
import org.libreplicator.journal.model.RetrievedReplicatorJournal
import org.libreplicator.journal.file.FileReader
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorJournal
import org.libreplicator.model.ReplicatorJournalStatus
import java.io.File
import java.nio.file.Paths
import java.util.NoSuchElementException
import javax.inject.Inject
import javax.inject.Named

class DefaultJournalReader
@Inject constructor(@Named("journalDirectory") private val journalDirectory: File,
                    private val fileReader: FileReader,
                    private val jsonMapper: JsonMapper) : JournalReader, ReplicatorJournalProvider {
    private companion object {
        private val BEFORE_SERIALIZED_STATE = 0
        private val AFTER_SERIALIZED_STATE = 1
    }

    override fun read(journalId: Long): RetrievedReplicatorJournal {
        val journalEntryFile = getJournalFile(journalId)
        if (!journalEntryFile.exists()) {
            throw JournalNotExistsException()
        }
        return readJournalEntry(journalId, fileReader.readAllLines(journalEntryFile))
    }

    override fun readAll(): List<RetrievedReplicatorJournal> =
            journalDirectory.listFiles()
                    .filter { it.isFile }
                    .map { it.name.toLong() to fileReader.readAllLines(it) }
                    .map { readJournalEntry(it.first, it.second) }
                    .sortedBy { it.id }

    override fun getJournal(): ReplicatorJournal {
        try {
            return readAll().last { notEmptyJournal(it) }.replicatorJournal
        }
        catch (noSuchElementException: NoSuchElementException) {
            return ReplicatorJournal.EMPTY
        }
    }

    private fun getJournalFile(journalId: Long) =
            Paths.get(journalDirectory.absolutePath, journalId.toString()).toFile()

    private fun readJournalEntry(journalId: Long, journalFileLines: List<String>): RetrievedReplicatorJournal {
        if (journalFileLines.isEmpty()) {
            return RetrievedReplicatorJournal(journalId, ReplicatorJournal.EMPTY)
        }
        try {
            if (journalFileLines.size == 1) {
                return retrieveRecoverJournal(journalFileLines, journalId)
            }
            else {
                return retrieveRestoreJournal(journalId, journalFileLines)
            }
        }
        catch (jsonReadException: JsonReadException) {
            return RetrievedReplicatorJournal(journalId, ReplicatorJournal.EMPTY)
        }
    }

    private fun retrieveRecoverJournal(journalFileLines: List<String>, journalId: Long): RetrievedReplicatorJournal {
        return RetrievedReplicatorJournal(journalId, readRecoverJournal(journalFileLines))
    }

    private fun readRecoverJournal(journalFileLines: List<String>): ReplicatorJournal {
        return readJournal(journalFileLines[BEFORE_SERIALIZED_STATE]).copy(status = ReplicatorJournalStatus.RECOVER)
    }

    private fun readJournal(journalFileLine:  String) = jsonMapper.read(journalFileLine, ReplicatorJournal::class)

    private fun retrieveRestoreJournal(journalId: Long, journalFileLines: List<String>): RetrievedReplicatorJournal {
        try {
            return RetrievedReplicatorJournal(journalId, readRestoreJournal(journalFileLines))
        }
        catch (jsonReadException: JsonReadException) {
            return RetrievedReplicatorJournal(journalId, readRecoverJournal(journalFileLines))
        }
    }

    private fun readRestoreJournal(journalFileLines: List<String>): ReplicatorJournal {
        return readJournal(journalFileLines[AFTER_SERIALIZED_STATE]).copy(status = ReplicatorJournalStatus.RESTORE)
    }

    private fun notEmptyJournal(retrievedReplicatorJournal: RetrievedReplicatorJournal): Boolean {
        val replicatorJournal = retrievedReplicatorJournal.replicatorJournal
        return replicatorJournal != ReplicatorJournal.EMPTY
    }
}
