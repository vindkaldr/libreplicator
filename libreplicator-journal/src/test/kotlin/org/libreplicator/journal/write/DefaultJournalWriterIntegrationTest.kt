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

import com.google.common.io.Files
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.journal.JournalExistsException
import org.libreplicator.journal.JournalNotExistsException
import org.libreplicator.journal.file.FileReader
import org.libreplicator.journal.file.FileWriter
import org.libreplicator.journal.model.RetrievedReplicatorJournal
import org.libreplicator.journal.read.DefaultJournalReader
import org.libreplicator.journal.read.JournalReader
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorJournal
import org.libreplicator.model.ReplicatorJournalStatus
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class DefaultJournalWriterIntegrationTest {
    private companion object {
        private val JOURNAL_ID = 1L

        private val JOURNAL = ReplicatorJournal.EMPTY
        private val RETRIEVED_JOURNAL = RetrievedReplicatorJournal.EMPTY.copy(id = JOURNAL_ID)
        private val RETRIEVED_RECOVER_JOURNAL = RETRIEVED_JOURNAL.copy(replicatorJournal = JOURNAL.copy(status = ReplicatorJournalStatus.RECOVER))
        private val RETRIEVED_RESTORE_JOURNAL = RETRIEVED_JOURNAL.copy(replicatorJournal = JOURNAL.copy(status = ReplicatorJournalStatus.RESTORE))

        private val SERIALIZED_JOURNAL = "serializedJournal"
    }

    private lateinit var journalDirectory: File
    private lateinit var journalReader: JournalReader
    @Mock private lateinit var mockJsonMapper: JsonMapper

    private lateinit var journalWriter: DefaultJournalWriter

    @Before
    fun setUp() {
        journalDirectory = Files.createTempDir()

        whenever(mockJsonMapper.write(JOURNAL)).thenReturn(SERIALIZED_JOURNAL)
        whenever(mockJsonMapper.read(SERIALIZED_JOURNAL, ReplicatorJournal::class)).thenReturn(JOURNAL)

        journalReader = DefaultJournalReader(journalDirectory, FileReader(), mockJsonMapper)
        journalWriter = DefaultJournalWriter(journalDirectory, journalReader, FileWriter(), mockJsonMapper)
    }

    @After
    fun tearDown() {
        journalDirectory.deleteRecursively()
    }

    @Test(expected = JournalExistsException::class)
    fun beforeReplicatorStateUpdate_shouldThrowException_whenWritingExistingJournal() {
        journalWriter.beforeReplicatorStateUpdate(JOURNAL_ID, JOURNAL.replicatorState, JOURNAL.lastReplicatorMessage)
        journalWriter.beforeReplicatorStateUpdate(JOURNAL_ID, JOURNAL.replicatorState, JOURNAL.lastReplicatorMessage)
    }

    @Test
    fun beforeReplicatorStateUpdate_shouldWriteBeforeState() {
        journalWriter.beforeReplicatorStateUpdate(JOURNAL_ID, JOURNAL.replicatorState, JOURNAL.lastReplicatorMessage)

        val actual = journalReader.read(JOURNAL_ID)
        assertThat(actual, equalTo(RETRIEVED_RECOVER_JOURNAL))
    }

    @Test(expected = JournalNotExistsException::class)
    fun afterReplicatorStateUpdate_shouldThrowException_whenWritingNonExistingJournal() {
        journalWriter.afterReplicatorStateUpdate(JOURNAL_ID, JOURNAL.replicatorState)
    }

    @Test
    fun afterReplicatorStateUpdate_shouldWriteAfterState() {
        journalWriter.beforeReplicatorStateUpdate(JOURNAL_ID, JOURNAL.replicatorState, JOURNAL.lastReplicatorMessage)
        journalWriter.afterReplicatorStateUpdate(JOURNAL_ID, JOURNAL.replicatorState)

        val actual = journalReader.read(JOURNAL_ID)
        assertThat(actual, equalTo(RETRIEVED_RESTORE_JOURNAL))
    }

//    @Test(expected = JournalNotExistsException::class)
//    fun delete_shouldDeleteEntries() {
//        journalWriter.writeStateBeforeUpdate(JOURNAL_ID, JOURNAL_ENTRY)
//        journalWriter.delete(listOf(journalReader.read(JOURNAL_ID)))
//
//        journalReader.read(JOURNAL_ID)
//    }
//
//    @Test
//    fun deleteAllButLatestThen_shouldNotInvokeOperation_whenNoEntries() {
//        var isOperationExecuted = false
//        journalWriter.deleteAllButLatestThen { isOperationExecuted = true }
//
//        assertFalse(isOperationExecuted)
//    }
//
//    @Test
//    fun deleteAllButLatestThen_shouldNotInvokeOperation_whenNoCommittedOrClosedEntries() {
//        val allJournalEntries = listOf(EMPTY_JOURNAL_ENTRY.copy(id = 1), EMPTY_JOURNAL_ENTRY.copy(id = 2),
//                EMPTY_JOURNAL_ENTRY.copy(id = 3), EMPTY_JOURNAL_ENTRY.copy(id = 4))
//        allJournalEntries.forEach { createEntry(it) }
//
//        var isOperationExecuted = false
//        journalWriter.deleteAllButLatestThen { isOperationExecuted = true }
//
//        assertThat(journalReader.readAll(), equalTo(listOf()))
//        assertFalse(isOperationExecuted)
//    }
//
//    @Test
//    fun deleteAllButLatestThen_shouldKeepLatestClosedEntry_thenInvokeOperation() {
//        val allJournalEntries = listOf(EMPTY_JOURNAL_ENTRY.copy(id = 1), COMMITTED_JOURNAL_ENTRY.copy(id = 2),
//                CLOSED_JOURNAL_ENTRY.copy(id = 3), EMPTY_JOURNAL_ENTRY.copy(id = 4),
//                CLOSED_JOURNAL_ENTRY.copy(id = 5), EMPTY_JOURNAL_ENTRY.copy(id = 6))
//        allJournalEntries.forEach { createEntry(it) }
//
//        var isOperationExecuted = false
//        journalWriter.deleteAllButLatestThen { isOperationExecuted = true }
//
//        assertThat(journalReader.readAll(), equalTo(listOf(CLOSED_JOURNAL_ENTRY.copy(id = 5))))
//        assertTrue(isOperationExecuted)
//    }
//
//    @Test
//    fun deleteAllButLatestThen_shouldKeepLatestCommittedEntry_thenInvokeOperation() {
//        val allJournalEntries = listOf(EMPTY_JOURNAL_ENTRY.copy(id = 1), COMMITTED_JOURNAL_ENTRY.copy(id = 2),
//                CLOSED_JOURNAL_ENTRY.copy(id = 3), COMMITTED_JOURNAL_ENTRY.copy(id = 4),
//                EMPTY_JOURNAL_ENTRY.copy(id = 5), EMPTY_JOURNAL_ENTRY.copy(id = 6))
//        allJournalEntries.forEach { createEntry(it) }
//
//        var isOperationExecuted = false
//        journalWriter.deleteAllButLatestThen { isOperationExecuted = true }
//
//        assertThat(journalReader.readAll(), equalTo(listOf(COMMITTED_JOURNAL_ENTRY.copy(id = 4))))
//        assertTrue(isOperationExecuted)
//    }
//
//    private fun createEntry(entry: JournalEntry) {
//        whenever(mockJsonMapper.write(entry)).thenReturn(entry.id.toString())
//        whenever(mockJsonMapper.read(entry.id.toString(), JournalEntry::class)).thenReturn(entry)
//
//        journalWriter.writeStateBeforeUpdate(entry.id, entry)
//
//        if (entry.closed) {
//            journalWriter.commit(entry.id)
//            journalWriter.writeStateAfterUpdate(entry.id)
//        }
//        else if (entry.committed) {
//            journalWriter.commit(entry.id)
//        }
//    }
}
