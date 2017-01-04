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

package org.libreplicator.journal.read

import com.google.common.io.Files
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.journal.JournalNotExistsException
import org.libreplicator.journal.model.RetrievedReplicatorJournal
import org.libreplicator.journal.file.FileReader
import org.libreplicator.journal.read.DefaultJournalReader
import org.libreplicator.journal.read.JournalReader
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorJournal
import org.libreplicator.model.ReplicatorJournalStatus
import org.libreplicator.model.ReplicatorState
import org.libreplicator.model.journal.JournalEntry
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class DefaultJournalReaderIntegrationTest {
    companion object {
        private val PACKAGE_OF_JOURNAL_FILES = "org/libreplicator/journal/read"

        private val LOGS = mutableSetOf(EventLog("nodeId", 1, "log"))

        private val JOURNAL = ReplicatorJournal.EMPTY
        private val RECOVER_JOURNAL = JOURNAL.copy(status = ReplicatorJournalStatus.RECOVER)
        private val RESTORE_JOURNAL = JOURNAL.copy(status = ReplicatorJournalStatus.RESTORE)

        private val OTHER_JOURNAL = JOURNAL.copy(replicatorState = ReplicatorState.EMPTY.copy(logs = LOGS))
        private val OTHER_RECOVER_JOURNAL = OTHER_JOURNAL.copy(status = ReplicatorJournalStatus.RECOVER)
        private val OTHER_RESTORE_JOURNAL = OTHER_JOURNAL.copy(status = ReplicatorJournalStatus.RESTORE)

        private val RETRIEVED_JOURNAL = RetrievedReplicatorJournal.EMPTY
        private val RETRIEVED_RECOVER_JOURNAL = RETRIEVED_JOURNAL.copy(replicatorJournal = RECOVER_JOURNAL)
        private val RETRIEVED_RESTORE_JOURNAL = RETRIEVED_JOURNAL.copy(replicatorJournal = RESTORE_JOURNAL)

        private val SERIALIZED_JOURNAL = "serializedJournal"
        private val OTHER_SERIALIZED_JOURNAL = "otherSerializedJournal"
        private val INVALID_JOURNAL = "invalidJournal"
    }

    private val journalDirectory = getJournalDirectory()
    private val fileReader = FileReader()
    @Mock private lateinit var mockJsonMapper: JsonMapper

    private lateinit var journalReader: DefaultJournalReader

    @Before
    fun setUp() {
        whenever(mockJsonMapper.read(SERIALIZED_JOURNAL, ReplicatorJournal::class)).thenReturn(JOURNAL)
        whenever(mockJsonMapper.read(OTHER_SERIALIZED_JOURNAL, ReplicatorJournal::class)).thenReturn(OTHER_JOURNAL)
        whenever(mockJsonMapper.read(INVALID_JOURNAL, ReplicatorJournal::class)).thenThrow(JsonReadException::class.java)
        whenever(mockJsonMapper.read("", ReplicatorJournal::class)).thenThrow(JsonReadException::class.java)

        journalReader = DefaultJournalReader(journalDirectory, fileReader, mockJsonMapper)
    }

    @Test(expected = JournalNotExistsException::class)
    fun read_shouldThrowException_whenReadingNonExistingJournal() {
        journalReader.read(-1)
    }

    @Test
    fun read_shouldReadJournalEntry() {
        val actualJournal = journalReader.read(4)
        val expectedJournal = RETRIEVED_JOURNAL.copy(4, JOURNAL.copy(status = ReplicatorJournalStatus.RESTORE))

        assertThat(actualJournal, equalTo(expectedJournal))
    }

    @Test
    fun readAll_shouldReadAllJournalEntry() {
        val journalEntries = journalReader.readAll()

        assertThat(journalEntries.size, equalTo(5))
        assertThat(journalEntries[0], equalTo(RETRIEVED_JOURNAL.copy(id = 0)))
        assertThat(journalEntries[1], equalTo(RETRIEVED_JOURNAL.copy(id = 1)))
        assertThat(journalEntries[2], equalTo(RETRIEVED_RECOVER_JOURNAL.copy(id = 2)))
        assertThat(journalEntries[3], equalTo(RETRIEVED_RECOVER_JOURNAL.copy(id = 3)))
        assertThat(journalEntries[4], equalTo(RETRIEVED_RESTORE_JOURNAL.copy(id = 4)))
    }

    @Test
    fun getJournal_shouldRetrieveLastRecoverJournal() {
        journalReader = DefaultJournalReader(getRecoverDirectory(), fileReader, mockJsonMapper)
        assertThat(journalReader.getJournal(), equalTo(OTHER_RECOVER_JOURNAL))
    }

    @Test
    fun getJournal_shouldRetrieveLastRestoreJournal() {
        journalReader = DefaultJournalReader(getRestoreDirectory(), fileReader, mockJsonMapper)
        assertThat(journalReader.getJournal(), equalTo(OTHER_RESTORE_JOURNAL))
    }

    @Test
    fun getJournal_shouldReturnEmptyRestoreJournal_whenNoValidJournal() {
        journalReader = DefaultJournalReader(getFallbackDirectory(), fileReader, mockJsonMapper)
        assertThat(journalReader.getJournal(), equalTo(JOURNAL))
    }

    @Test
    fun getJournal_shouldReturnEmptyRestoreJournal_whenEmptyDirectory() {
        val emptyDirectory = Files.createTempDir()

        journalReader = DefaultJournalReader(emptyDirectory, fileReader, mockJsonMapper)
        assertThat(journalReader.getJournal(), equalTo(JOURNAL))

        emptyDirectory.delete()
    }

    private fun getJournalDirectory(): File =
            File(javaClass.classLoader.getResource("$PACKAGE_OF_JOURNAL_FILES/0").toURI()).parentFile

    private fun getRecoverDirectory(): File =
            File(javaClass.classLoader.getResource("$PACKAGE_OF_JOURNAL_FILES/recover/0").toURI()).parentFile

    private fun getRestoreDirectory(): File =
        File(javaClass.classLoader.getResource("$PACKAGE_OF_JOURNAL_FILES/restore/0").toURI()).parentFile

    private fun getFallbackDirectory(): File =
            File(javaClass.classLoader.getResource("$PACKAGE_OF_JOURNAL_FILES/fallback/0").toURI()).parentFile
}
