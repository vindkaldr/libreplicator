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

import org.libreplicator.api.ReplicatorNode
import org.libreplicator.crypto.api.MessageCipher
import org.libreplicator.journal.api.ReplicatorStateJournal
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorState
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import javax.inject.Inject

class DefaultReplicatorStateJournal @Inject constructor(
        private val fileHandler: FileHandler,
        private val jsonMapper: JsonMapper,
        private val messageCipher: MessageCipher,
        journalsDirectory: Path,
        localNode: ReplicatorNode,
        remoteNodes: List<ReplicatorNode>) : ReplicatorStateJournal {

    private companion object {
        private val JOURNAL_FILE_NAME = "libreplicator-journal"
        private val LATEST_JOURNAL_FILE_NAME = "latest-libreplicator-journal"
    }
    private val journalDirectory = fileHandler.createDirectory(journalsDirectory,
            createJournalDirectoryName(localNode, remoteNodes))

    private val journalFile = journalDirectory.resolve(JOURNAL_FILE_NAME)
    private val latestJournalFile = journalFile.resolveSibling(LATEST_JOURNAL_FILE_NAME)

    override fun getReplicatorState(): ReplicatorState {
        return try {
            jsonMapper.read(messageCipher.decrypt(fileHandler.readFirstLine(latestJournalFile)), ReplicatorState::class)
        }
        catch (jsonReadException: JsonReadException) {
            ReplicatorState()
        }
        catch (noSuchFileException: NoSuchFileException) {
            ReplicatorState()
        }
        catch (throwable: Throwable) {
            ReplicatorState()
        }
    }

    override fun observe(observable: ReplicatorState) {
        fileHandler.write(journalFile, messageCipher.encrypt(jsonMapper.write(observable)))
        fileHandler.move(journalFile, latestJournalFile)
    }

    private fun createJournalDirectoryName(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>): String =
            "${localNode.nodeId}:${remoteNodes.map { it.nodeId }.joinToString(":")}"
}
