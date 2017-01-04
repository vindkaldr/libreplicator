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

package org.libreplicator.journal.write

import org.libreplicator.interactor.api.ReplicatorListener
import org.libreplicator.journal.JournalExistsException
import org.libreplicator.journal.JournalNotCommittedException
import org.libreplicator.journal.file.FileWriter
import org.libreplicator.journal.read.JournalReader
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorJournal
import org.libreplicator.model.ReplicatorJournalStatus
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.ReplicatorState
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named

class DefaultJournalWriter
@Inject constructor(@Named("journalDirectory") private val journalDirectory: File,
                    private val defaultJournalReader: JournalReader,
                    private val fileWriter: FileWriter,
                    private val jsonMapper: JsonMapper) : JournalWriter, ReplicatorListener
{
    override fun beforeReplicatorStateUpdate(id: Long, state: ReplicatorState, message: ReplicatorMessage) {
        val journalFilePath = getJournalFilePath(id)
        if (fileWriter.exists(journalFilePath)) {
            throw JournalExistsException()
        }
        fileWriter.write(journalFilePath, jsonMapper.write(ReplicatorJournal(state, message)))
    }

    override fun afterReplicatorStateUpdate(id: Long, state: ReplicatorState) {
        val retrievedReplicatorJournal = defaultJournalReader.read(id)
        if (retrievedReplicatorJournal.replicatorJournal.status != ReplicatorJournalStatus.RECOVER) {
            throw JournalNotCommittedException()
        }
        fileWriter.append(getJournalFilePath(id), jsonMapper.write(ReplicatorJournal(state)))
    }

    private fun getJournalFilePath(journalEntryId: Long): Path =
            Paths.get(journalDirectory.absolutePath, journalEntryId.toString())
}
