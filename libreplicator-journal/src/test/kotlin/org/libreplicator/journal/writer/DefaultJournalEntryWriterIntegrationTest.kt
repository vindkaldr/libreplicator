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

import com.google.common.io.Files
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.interactor.api.journal.JournalExistsException
import org.libreplicator.interactor.api.journal.JournalNotCommittedException
import org.libreplicator.interactor.api.journal.JournalNotExistsException
import org.libreplicator.journal.DefaultJournalServiceTest
import org.libreplicator.journal.reader.DefaultJournalEntryReader
import org.libreplicator.journal.reader.FileReader
import org.libreplicator.journal.reader.JournalEntryReader
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.journal.JournalEntry
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class DefaultJournalEntryWriterIntegrationTest {
    private companion object {
        private val JOURNAL_ENTRY_ID = 1L

        private val JOURNAL_ENTRY = JournalEntry.EMPTY.copy(id = JOURNAL_ENTRY_ID)
        private val SERIALIZED_JOURNAL_ENTRY = "serializedJournalEntry"

        private val EMPTY_JOURNAL_ENTRY = JournalEntry.EMPTY
        private val COMMITTED_JOURNAL_ENTRY = JournalEntry.EMPTY.copy(committed = true)
        private val CLOSED_JOURNAL_ENTRY = JournalEntry.EMPTY.copy( committed = true, closed = true)
    }

    private lateinit var journalDirectory: File
    @Mock private lateinit var mockJsonMapper: JsonMapper
    private lateinit var journalEntryReader: JournalEntryReader

    private lateinit var journalEntryWriter: JournalEntryWriter

    @Before
    fun setUp() {
        journalDirectory = Files.createTempDir()

        whenever(mockJsonMapper.write(JOURNAL_ENTRY)).thenReturn(SERIALIZED_JOURNAL_ENTRY)
        whenever(mockJsonMapper.read(SERIALIZED_JOURNAL_ENTRY, JournalEntry::class)).thenReturn(JOURNAL_ENTRY)

        journalEntryReader = DefaultJournalEntryReader(journalDirectory, FileReader(), mockJsonMapper)
        journalEntryWriter = DefaultJournalEntryWriter(journalDirectory, journalEntryReader, FileWriter(), mockJsonMapper)
    }

    @After
    fun tearDown() {
        journalDirectory.deleteRecursively()
    }

    @Test(expected = JournalExistsException::class)
    fun write_shouldThrowException_whenWritingExistingEntry() {
        journalEntryWriter.write(JOURNAL_ENTRY_ID, JOURNAL_ENTRY)
        journalEntryWriter.write(JOURNAL_ENTRY_ID, JOURNAL_ENTRY)
    }

    @Test
    fun write_shouldWriteEntry() {
        journalEntryWriter.write(JOURNAL_ENTRY_ID, JOURNAL_ENTRY)

        val actual = journalEntryReader.read(JOURNAL_ENTRY_ID)
        assertThat(actual, equalTo(JOURNAL_ENTRY))
    }

    @Test(expected = JournalNotExistsException::class)
    fun commit_shouldThrowException_whenCommittingNonExistentEntry() {
        journalEntryWriter.commit(JOURNAL_ENTRY_ID)
    }

    @Test
    fun commit_shouldCommitEntry() {
        journalEntryWriter.write(JOURNAL_ENTRY_ID, JOURNAL_ENTRY)
        journalEntryWriter.commit(JOURNAL_ENTRY_ID)

        val actual = journalEntryReader.read(JOURNAL_ENTRY_ID)
        val expected = JOURNAL_ENTRY.copy(committed = true)
        assertThat(actual, equalTo(expected))
    }

    @Test(expected = JournalNotExistsException::class)
    fun close_shouldThrowException_whenClosingNonExistentEntry() {
        journalEntryWriter.close(JOURNAL_ENTRY_ID)
    }

    @Test(expected = JournalNotCommittedException::class)
    fun close_shouldThrowException_whenClosingNotCommittedEntry() {
        journalEntryWriter.write(JOURNAL_ENTRY_ID, JOURNAL_ENTRY)
        journalEntryWriter.close(JOURNAL_ENTRY_ID)
    }

    @Test
    fun close_shouldCloseEntry() {
        journalEntryWriter.write(JOURNAL_ENTRY_ID, JOURNAL_ENTRY)
        journalEntryWriter.commit(JOURNAL_ENTRY_ID)
        journalEntryWriter.close(JOURNAL_ENTRY_ID)

        val actual = journalEntryReader.read(JOURNAL_ENTRY_ID)
        val expected = JOURNAL_ENTRY.copy(committed = true, closed = true)
        assertThat(actual, equalTo(expected))
    }

    @Test(expected = JournalNotExistsException::class)
    fun delete_shouldDeleteEntries() {
        journalEntryWriter.write(JOURNAL_ENTRY_ID, JOURNAL_ENTRY)
        journalEntryWriter.delete(listOf(journalEntryReader.read(JOURNAL_ENTRY_ID)))

        journalEntryReader.read(JOURNAL_ENTRY_ID)
    }

    @Test
    fun deleteAllButLatestThen_shouldNotInvokeOperation_whenNoEntries() {
        var isOperationExecuted = false
        journalEntryWriter.deleteAllButLatestThen { isOperationExecuted = true }

        assertFalse(isOperationExecuted)
    }

    @Test
    fun deleteAllButLatestThen_shouldNotInvokeOperation_whenNoCommittedOrClosedEntries() {
        val allJournalEntries = listOf(EMPTY_JOURNAL_ENTRY.copy(id = 1), EMPTY_JOURNAL_ENTRY.copy(id = 2),
                EMPTY_JOURNAL_ENTRY.copy(id = 3), EMPTY_JOURNAL_ENTRY.copy(id = 4))
        allJournalEntries.forEach { createEntry(it) }

        var isOperationExecuted = false
        journalEntryWriter.deleteAllButLatestThen { isOperationExecuted = true }

        assertThat(journalEntryReader.readAll(), equalTo(listOf()))
        assertFalse(isOperationExecuted)
    }

    @Test
    fun deleteAllButLatestThen_shouldKeepLatestClosedEntry_thenInvokeOperation() {
        val allJournalEntries = listOf(EMPTY_JOURNAL_ENTRY.copy(id = 1), COMMITTED_JOURNAL_ENTRY.copy(id = 2),
                CLOSED_JOURNAL_ENTRY.copy(id = 3), EMPTY_JOURNAL_ENTRY.copy(id = 4),
                CLOSED_JOURNAL_ENTRY.copy(id = 5), EMPTY_JOURNAL_ENTRY.copy(id = 6))
        allJournalEntries.forEach { createEntry(it) }

        var isOperationExecuted = false
        journalEntryWriter.deleteAllButLatestThen { isOperationExecuted = true }

        assertThat(journalEntryReader.readAll(), equalTo(listOf(CLOSED_JOURNAL_ENTRY.copy(id = 5))))
        assertTrue(isOperationExecuted)
    }

    @Test
    fun deleteAllButLatestThen_shouldKeepLatestCommittedEntry_thenInvokeOperation() {
        val allJournalEntries = listOf(EMPTY_JOURNAL_ENTRY.copy(id = 1), COMMITTED_JOURNAL_ENTRY.copy(id = 2),
                CLOSED_JOURNAL_ENTRY.copy(id = 3), COMMITTED_JOURNAL_ENTRY.copy(id = 4),
                EMPTY_JOURNAL_ENTRY.copy(id = 5), EMPTY_JOURNAL_ENTRY.copy(id = 6))
        allJournalEntries.forEach { createEntry(it) }

        var isOperationExecuted = false
        journalEntryWriter.deleteAllButLatestThen { isOperationExecuted = true }

        assertThat(journalEntryReader.readAll(), equalTo(listOf(COMMITTED_JOURNAL_ENTRY.copy(id = 4))))
        assertTrue(isOperationExecuted)
    }

    private fun createEntry(entry: JournalEntry) {
        whenever(mockJsonMapper.write(entry)).thenReturn(entry.id.toString())
        whenever(mockJsonMapper.read(entry.id.toString(), JournalEntry::class)).thenReturn(entry)

        journalEntryWriter.write(entry.id, entry)

        if (entry.closed) {
            journalEntryWriter.commit(entry.id)
            journalEntryWriter.close(entry.id)
        }
        else if (entry.committed) {
            journalEntryWriter.commit(entry.id)
        }
    }
}
