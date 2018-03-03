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

package org.libreplicator.core.journal

import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.core.journal.api.ReplicatorStateJournal
import org.libreplicator.core.journal.file.FileHandler
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.core.model.ReplicatorState
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import javax.inject.Inject

private const val JOURNAL_FILE_NAME = "libreplicator-journal"
private const val LATEST_JOURNAL_FILE_NAME = "latest-libreplicator-journal"

class DefaultReplicatorStateJournal @Inject constructor(
    private val fileHandler: FileHandler,
    private val jsonMapper: JsonMapper,
    private val cipher: Cipher,
    journalsDirectory: Path,
    localNode: LocalNode,
    remoteNodes: List<RemoteNode>
) : ReplicatorStateJournal {
    private val journalDirectory = fileHandler.createDirectory(journalsDirectory,
            createJournalDirectoryName(localNode, remoteNodes))

    private val journalFile = journalDirectory.resolve(JOURNAL_FILE_NAME)
    private val latestJournalFile = journalFile.resolveSibling(LATEST_JOURNAL_FILE_NAME)

    override fun getReplicatorState(): ReplicatorState {
        return try {
            jsonMapper.read(cipher.decrypt(fileHandler.readFirstLine(latestJournalFile)), ReplicatorState::class)
        }
        catch (noSuchFileException: NoSuchFileException) {
            ReplicatorState()
        }
        catch (jsonReadException: JsonReadException) {
            ReplicatorState()
        }
    }

    override suspend fun observe(observable: ReplicatorState) {
        fileHandler.write(journalFile, cipher.encrypt(jsonMapper.write(observable)))
        fileHandler.move(journalFile, latestJournalFile)
    }

    private fun createJournalDirectoryName(localNode: LocalNode, remoteNodes: List<RemoteNode>): String =
            "${localNode.nodeId}:${remoteNodes.joinToString(":") { it.nodeId }}"
}
