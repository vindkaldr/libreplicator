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

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.EventLog
import org.libreplicator.model.EventNode
import org.libreplicator.model.ReplicatorState
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.nio.file.NoSuchFileException
import java.nio.file.Paths

@RunWith(MockitoJUnitRunner::class)
class DefaultReplicatorStateJournalTest {
    private companion object {
        private val JOURNALS_DIRECTORY = Paths.get(".")

        private val LOCAL_NODE = EventNode("localNode", "", 0)
        private val REMOTE_NODE_1 = EventNode("remoteNode1", "", 0)
        private val REMOTE_NODE_2 = EventNode("remoteNode2", "", 0)

        private val JOURNAL_DIRECTORY_NAME = "${LOCAL_NODE.nodeId}:${REMOTE_NODE_1.nodeId}:${REMOTE_NODE_2.nodeId}"
        private val JOURNAL_DIRECTORY = JOURNALS_DIRECTORY.resolve(JOURNAL_DIRECTORY_NAME)

        private val JOURNAL_FILE_NAME = "libreplicator-journal"
        private val JOURNAL_FILE = JOURNAL_DIRECTORY.resolve(JOURNAL_FILE_NAME)

        private val LATEST_JOURNAL_FILE_NAME = "latest-libreplicator-journal"
        private val LATEST_JOURNAL_FILE = JOURNAL_DIRECTORY.resolve(LATEST_JOURNAL_FILE_NAME)

        private val SERIALIZED_REPLICATOR_STATE = "serializedReplicatorState"
        private val REPLICATOR_STATE = ReplicatorState(mutableSetOf(EventLog(LOCAL_NODE.nodeId, 0, "")))
    }

    @Mock private lateinit var mockFileHandler: FileHandler
    @Mock private lateinit var mockJsonMapper: JsonMapper

    private lateinit var replicatorStateJournal: DefaultReplicatorStateJournal

    @Before
    fun setUp() {
        whenever(mockFileHandler.createDirectory(JOURNALS_DIRECTORY, JOURNAL_DIRECTORY_NAME))
                .thenReturn(JOURNAL_DIRECTORY)

        whenever(mockFileHandler.readFirstLine(LATEST_JOURNAL_FILE)).thenReturn(SERIALIZED_REPLICATOR_STATE)
        whenever(mockJsonMapper.read(SERIALIZED_REPLICATOR_STATE, ReplicatorState::class))
                .thenReturn(REPLICATOR_STATE)

        whenever(mockJsonMapper.write(REPLICATOR_STATE)).thenReturn(SERIALIZED_REPLICATOR_STATE)

        replicatorStateJournal = DefaultReplicatorStateJournal(mockFileHandler, mockJsonMapper,
                JOURNALS_DIRECTORY, LOCAL_NODE, listOf(REMOTE_NODE_1, REMOTE_NODE_2))
    }

    @Test
    fun constructing_setsUp_journalDirectory() {
        verify(mockFileHandler).createDirectory(JOURNALS_DIRECTORY, JOURNAL_DIRECTORY_NAME)
        verifyNoMoreInteractions(mockFileHandler)
    }

    @Test
    fun getReplicatorState_returnsInitialState_whenJournalNotPresent() {
        whenever(mockFileHandler.readFirstLine(LATEST_JOURNAL_FILE)).thenThrow(NoSuchFileException::class.java)

        assertThat(replicatorStateJournal.getReplicatorState(), equalTo(ReplicatorState()))
    }

    @Test
    fun getReplicatorState_returnsInitialState_whenJournalNotReadable() {
        whenever(mockFileHandler.readFirstLine(LATEST_JOURNAL_FILE)).thenThrow(JsonReadException::class.java)

        assertThat(replicatorStateJournal.getReplicatorState(), equalTo(ReplicatorState()))
    }

    @Test
    fun getReplicatorState_returnsInitialState_whenProblemEncountered() {
        whenever(mockFileHandler.readFirstLine(LATEST_JOURNAL_FILE)).thenThrow(Throwable::class.java)

        assertThat(replicatorStateJournal.getReplicatorState(), equalTo(ReplicatorState()))
    }

    @Test
    fun getReplicatorState_returnsState() {
        assertThat(replicatorStateJournal.getReplicatorState(), equalTo(REPLICATOR_STATE))
    }

    @Test
    fun journal_writesState() {
        verify(mockFileHandler).createDirectory(JOURNALS_DIRECTORY, JOURNAL_DIRECTORY_NAME)

        replicatorStateJournal.observe(REPLICATOR_STATE)

        verify(mockFileHandler).write(JOURNAL_FILE, SERIALIZED_REPLICATOR_STATE)
        verify(mockFileHandler).move(JOURNAL_FILE, LATEST_JOURNAL_FILE)
        verifyNoMoreInteractions(mockFileHandler)
    }
}
