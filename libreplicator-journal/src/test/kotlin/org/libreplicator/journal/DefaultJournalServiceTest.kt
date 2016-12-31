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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.interactor.api.journal.JournalExistsException
import org.libreplicator.interactor.api.journal.JournalNotCommittedException
import org.libreplicator.interactor.api.journal.JournalNotExistsException
import org.libreplicator.interactor.api.journal.JournalService
import org.libreplicator.journal.reader.JournalEntryReader
import org.libreplicator.journal.writer.JournalEntryWriter
import org.libreplicator.model.journal.JournalEntry
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultJournalServiceTest {
    @Mock private lateinit var mockJournalEntryWriter: JournalEntryWriter

    private lateinit var journalService: JournalService

    @Before
    fun setUp() {
        journalService = DefaultJournalService(mockJournalEntryWriter)
    }

    @Test
    fun write_shouldReturnDifferentIds() {
        val firstId = journalService.write(JournalEntry.EMPTY)
        val secondId = journalService.write(JournalEntry.EMPTY)

        assertThat(firstId, not(secondId))
    }

    @Test
    fun write_shouldWriteThroughTheJournalEntryWriter() {
        journalService.write(JournalEntry.EMPTY)
        verify(mockJournalEntryWriter).write(any(), eq(JournalEntry.EMPTY))
    }

    @Test(expected = JournalExistsException::class)
    fun write_shouldLetThroughJournalExistsException() {
        whenever(mockJournalEntryWriter.write(any(), eq(JournalEntry.EMPTY)))
                .thenThrow(JournalExistsException::class.java)

        journalService.write(JournalEntry.EMPTY)
    }

    @Test
    fun commit_shouldCommitThroughTheJournalEntryWriter() {
        journalService.commit(JournalEntry.EMPTY.id)
        verify(mockJournalEntryWriter).commit(JournalEntry.EMPTY.id)
    }

    @Test(expected = JournalNotExistsException::class)
    fun commit_shouldLetThroughJournalNotExistsException() {
        whenever(mockJournalEntryWriter.commit(JournalEntry.EMPTY.id))
                .thenThrow(JournalNotExistsException::class.java)

        journalService.commit(JournalEntry.EMPTY.id)
    }

    @Test
    fun close_shouldCloseThroughTheJournalEntryWriter() {
        journalService.close(JournalEntry.EMPTY.id)
        verify(mockJournalEntryWriter).close(JournalEntry.EMPTY.id)
    }

    @Test(expected = JournalNotExistsException::class)
    fun close_shouldLetThroughJournalNotExistsException() {
        whenever(mockJournalEntryWriter.close(JournalEntry.EMPTY.id))
                .thenThrow(JournalNotExistsException::class.java)

        journalService.close(JournalEntry.EMPTY.id)
    }

    @Test(expected = JournalNotCommittedException::class)
    fun close_shouldLetThroughJournalNotCommittedException() {
        whenever(mockJournalEntryWriter.close(JournalEntry.EMPTY.id))
                .thenThrow(JournalNotCommittedException::class.java)

        journalService.close(JournalEntry.EMPTY.id)
    }
}
