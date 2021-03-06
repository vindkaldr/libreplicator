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

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteLog
import org.libreplicator.api.RemoteNode
import org.libreplicator.core.journal.testdouble.ErroneousJournalReaderMock
import org.libreplicator.core.journal.testdouble.ExistingJournalReaderMock
import org.libreplicator.core.journal.testdouble.JournalDirectoryCreatorMock
import org.libreplicator.core.journal.testdouble.JournalHandlerMock
import org.libreplicator.core.journal.testdouble.JsonMapperStub
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.core.model.ReplicatorState
import java.nio.file.NoSuchFileException
import java.nio.file.Paths

private val JOURNALS_DIRECTORY = Paths.get(".")

private val LOCAL_NODE = LocalNode("localNode", "", 0)
private val REMOTE_NODE_1 = RemoteNode("remoteNode1", "", 0)
private val REMOTE_NODE_2 = RemoteNode("remoteNode2", "", 0)

private val JOURNAL_DIRECTORY_NAME = "${LOCAL_NODE.nodeId}:${REMOTE_NODE_1.nodeId}:${REMOTE_NODE_2.nodeId}"
private val JOURNAL_DIRECTORY = JOURNALS_DIRECTORY.resolve(JOURNAL_DIRECTORY_NAME)

private const val JOURNAL_FILE_NAME = "libreplicator-journal"
private val JOURNAL_FILE = JOURNAL_DIRECTORY.resolve(JOURNAL_FILE_NAME)

private const val LATEST_JOURNAL_FILE_NAME = "latest-libreplicator-journal"
private val LATEST_JOURNAL_FILE = JOURNAL_DIRECTORY.resolve(LATEST_JOURNAL_FILE_NAME)

private const val SERIALIZED_REPLICATOR_STATE = "serializedReplicatorState"
private val REPLICATOR_STATE = ReplicatorState(
        LOCAL_NODE,
        listOf(REMOTE_NODE_1, REMOTE_NODE_2),
        mutableSetOf(RemoteLog(LOCAL_NODE.nodeId, 0, ""))
)

class DefaultReplicatorStateJournalTest {
    private val jsonMapperStub: JsonMapper = JsonMapperStub(
        REPLICATOR_STATE,
        SERIALIZED_REPLICATOR_STATE
    )

    private val cipherStub: Cipher = object : Cipher {
        override fun encrypt(content: String): String = content
        override fun decrypt(encryptedContent: String): String = encryptedContent
    }

    @Test
    fun constructing_setsUp_journalDirectory() {
        val fileHandlerMock = JournalDirectoryCreatorMock()

        DefaultReplicatorStateJournal(
                fileHandlerMock,
                jsonMapperStub,
                cipherStub,
                JOURNALS_DIRECTORY,
                LOCAL_NODE,
                listOf(REMOTE_NODE_1, REMOTE_NODE_2)
        )

        assertTrue(fileHandlerMock.createdDirectoryWith(JOURNALS_DIRECTORY, JOURNAL_DIRECTORY_NAME))
    }

    @Test
    fun getReplicatorState_returnsInitialState_whenJournalNotPresent() {
        val fileHandlerMock = ErroneousJournalReaderMock(
            journal = Paths.get(LATEST_JOURNAL_FILE_NAME),
            exceptionToThrow = NoSuchFileException("")
        )

        val replicatorStateJournal = DefaultReplicatorStateJournal(
                fileHandlerMock,
                jsonMapperStub,
                cipherStub,
                JOURNALS_DIRECTORY,
                LOCAL_NODE,
                listOf(REMOTE_NODE_1, REMOTE_NODE_2)
        )

        assertThat(replicatorStateJournal.getReplicatorState(), equalTo(ReplicatorState()))
    }

    @Test
    fun getReplicatorState_returnsInitialState_whenJournalNotReadable() {
        val fileHandlerMock = ErroneousJournalReaderMock(
            journal = Paths.get(LATEST_JOURNAL_FILE_NAME),
            exceptionToThrow = JsonReadException(NoSuchFileException(""))
        )

        val replicatorStateJournal = DefaultReplicatorStateJournal(
                fileHandlerMock,
                jsonMapperStub,
                cipherStub,
                JOURNALS_DIRECTORY,
                LOCAL_NODE,
                listOf(REMOTE_NODE_1, REMOTE_NODE_2)
        )

        assertThat(replicatorStateJournal.getReplicatorState(), equalTo(ReplicatorState()))
    }

    @Test
    fun getReplicatorState_returnsState() {
        val fileHandlerMock = ExistingJournalReaderMock(
            journal = LATEST_JOURNAL_FILE,
            content = SERIALIZED_REPLICATOR_STATE
        )

        val replicatorStateJournal = DefaultReplicatorStateJournal(
                fileHandlerMock,
                jsonMapperStub,
                cipherStub,
                JOURNALS_DIRECTORY,
                LOCAL_NODE,
                listOf(REMOTE_NODE_1, REMOTE_NODE_2)
        )

        assertThat(replicatorStateJournal.getReplicatorState(), equalTo(REPLICATOR_STATE))
    }

    @Test
    fun journal_writesState() = runBlocking {
        val fileHandlerMock = JournalHandlerMock(
            journalsDirectory = JOURNALS_DIRECTORY,
            journalDirectoryName = JOURNAL_DIRECTORY_NAME,
            journalDirectory = JOURNAL_DIRECTORY
        )

        val replicatorStateJournal = DefaultReplicatorStateJournal(
                fileHandlerMock,
                jsonMapperStub,
                cipherStub,
                JOURNALS_DIRECTORY,
                LOCAL_NODE,
                listOf(REMOTE_NODE_1, REMOTE_NODE_2)
        )

        assertTrue(fileHandlerMock.createdDirectory(JOURNALS_DIRECTORY, JOURNAL_DIRECTORY_NAME))

        replicatorStateJournal.observe(REPLICATOR_STATE)

        assertTrue(fileHandlerMock.wroteJournal(JOURNAL_FILE, SERIALIZED_REPLICATOR_STATE))
        assertTrue(fileHandlerMock.movedJournal(JOURNAL_FILE, LATEST_JOURNAL_FILE))
    }
}
