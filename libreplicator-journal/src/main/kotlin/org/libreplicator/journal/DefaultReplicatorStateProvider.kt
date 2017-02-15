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

import org.libreplicator.api.Observer
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.journal.api.ReplicatorStateProvider
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorState
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class DefaultReplicatorStateProvider constructor(
        private val journalsDirectory: Path,
        private val fileHandler: FileHandler,
        private val jsonMapper: JsonMapper) : ReplicatorStateProvider {

    private companion object {
        val JOURNAL_FILE_NAME = "libreplicator-journal"
        val LATEST_JOURNAL_FILE_NAME = "latest-libreplicator-journal"
    }

    private lateinit var journalDirectory: Path
    private lateinit var journalFile: Path
    private lateinit var latestJournalFile: Path

    fun init(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>) {
        val journalDirectoryName = createJournalDirectoryName(localNode, remoteNodes)

        journalDirectory = fileHandler.createDirectory(journalsDirectory, journalDirectoryName)
        journalFile = journalDirectory.resolve(JOURNAL_FILE_NAME)
        latestJournalFile = journalFile.resolveSibling(LATEST_JOURNAL_FILE_NAME)
    }

    override fun getReplicatorState(): ReplicatorState {
        val state = try {
            jsonMapper.read(fileHandler.readFirstLine(latestJournalFile), ReplicatorState::class)
        }
        catch (jsonReadException: JsonReadException) {
            ReplicatorState()
        }
        catch (noSuchFileException: NoSuchFileException) {
            ReplicatorState()
        }

        state.bind(ReplicatorStateObserver())
        return state
    }

    fun replicatorStateChanged(replicatorState: ReplicatorState) {
        fileHandler.write(journalFile, jsonMapper.write(replicatorState))
        fileHandler.move(journalFile, latestJournalFile)
    }

    private fun createJournalDirectoryName(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>): String =
            "${localNode.nodeId}:${remoteNodes.map { it.nodeId }.joinToString(":")}"

    inner class ReplicatorStateObserver : Observer<ReplicatorState> {
        override fun observe(observable: ReplicatorState) {
            replicatorStateChanged(observable)
        }
    }
}
