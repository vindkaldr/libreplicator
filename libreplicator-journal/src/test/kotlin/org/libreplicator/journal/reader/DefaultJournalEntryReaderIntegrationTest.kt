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

import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.interactor.api.journal.JournalNotExistsException
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.journal.JournalEntry
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class DefaultJournalEntryReaderIntegrationTest {
    companion object {
        val PACKAGE_OF_JOURNAL_FILES = "org/libreplicator/journal/reader"

        private val JOURNAL_ENTRY = JournalEntry.EMPTY
        private val SERIALIZED_JOURNAL_ENTRY = "serializedJournalEntry"
        private val INVALID_JOURNAL_ENTRY = "invalid entry"
    }

    private val journalDirectory = getJournalDirectory()
    private val fileReader = FileReader()
    @Mock private lateinit var mockJsonMapper: JsonMapper

    private lateinit var journalEntryReader: JournalEntryReader

    @Before
    fun setUp() {
        whenever(mockJsonMapper.read(SERIALIZED_JOURNAL_ENTRY, JournalEntry::class)).thenReturn(JOURNAL_ENTRY)
        whenever(mockJsonMapper.read(INVALID_JOURNAL_ENTRY, JournalEntry::class)).thenThrow(JsonReadException::class.java)

        journalEntryReader = DefaultJournalEntryReader(journalDirectory, fileReader, mockJsonMapper)
    }

    @Test(expected = JournalNotExistsException::class)
    fun read_shouldThrowException_whenReadingNonExistingJournal() {
        journalEntryReader.read(-1)
    }

    @Test
    fun read_shouldReadJournalEntry() {
        val actualJournalEntry = journalEntryReader.read(6)
        val expectedJournalEntry = JOURNAL_ENTRY.copy(id = 6, committed = true, closed = true)

        assertThat(actualJournalEntry, equalTo(expectedJournalEntry))
    }

    @Test
    fun readAll_shouldReadAllJournalEntry() {
        val journalEntries = journalEntryReader.readAll()

        assertThat(journalEntries.size, equalTo(7))
        assertThat(journalEntries[0], equalTo(JournalEntry.EMPTY.copy(id = 0)))
        assertThat(journalEntries[1], equalTo(JournalEntry.EMPTY.copy(id = 1)))
        assertThat(journalEntries[2], equalTo(JOURNAL_ENTRY.copy(id = 2, committed = false, closed = false)))
        assertThat(journalEntries[3], equalTo(JOURNAL_ENTRY.copy(id = 3, committed = false, closed = false)))
        assertThat(journalEntries[4], equalTo(JOURNAL_ENTRY.copy(id = 4, committed = true, closed = false)))
        assertThat(journalEntries[5], equalTo(JOURNAL_ENTRY.copy(id = 5, committed = true, closed = false)))
        assertThat(journalEntries[6], equalTo(JOURNAL_ENTRY.copy(id = 6, committed = true, closed = true)))
    }

    private fun getJournalDirectory(): File =
            File(javaClass.classLoader.getResource("$PACKAGE_OF_JOURNAL_FILES/0").toURI()).parentFile
}
