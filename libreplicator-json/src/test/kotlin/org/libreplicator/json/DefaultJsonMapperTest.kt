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

package org.libreplicator.json

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.model.journal.JournalEntry

class DefaultJsonMapperTest {
    private companion object {
        private val EVENT_LOG = EventLog("nodeId", 5L, "log")
        private val SERIALIZED_EVENT_LOG = "{\"nodeId\":\"nodeId\",\"time\":5,\"log\":\"log\"}"

        private val REPLICATOR_MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable.EMPTY)
        private val SERIALIZED_REPLICATOR_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"

        private val JOURNAL_ENTRY = JournalEntry(setOf(), TimeTable(), REPLICATOR_MESSAGE)
        private val SERIALIZED_JOURNAL_ENTRY = "{\"eventLogs\":[],\"timeTable\":[],\"replicatorMessage\":$SERIALIZED_REPLICATOR_MESSAGE}"
    }

    private lateinit var jsonMapper: JsonMapper

    @Before
    fun setUp() {
        jsonMapper = DefaultJsonMapper()
    }

    @Test
    fun write_shouldSerializeEventLog() {
        assertThat(jsonMapper.write(EVENT_LOG), equalTo(SERIALIZED_EVENT_LOG))
    }

    @Test
    fun read_shouldDeserializeEventLog() {
        assertThat(jsonMapper.read(SERIALIZED_EVENT_LOG, EventLog::class), equalTo(EVENT_LOG))
    }

    @Test
    fun write_shouldSerializeReplicatorMessage() {
        assertThat(jsonMapper.write(REPLICATOR_MESSAGE), equalTo(SERIALIZED_REPLICATOR_MESSAGE))
    }

    @Test
    fun read_shouldDeserializeReplicatorMessage() {
        assertThat(jsonMapper.read(SERIALIZED_REPLICATOR_MESSAGE, ReplicatorMessage::class), equalTo(REPLICATOR_MESSAGE))
    }

    @Test
    fun write_shouldSerializeJournalEntry() {
        assertThat(jsonMapper.write(JOURNAL_ENTRY), equalTo(SERIALIZED_JOURNAL_ENTRY))
    }

    @Test
    fun read_shouldDeserializeJournalEntry() {
        assertThat(jsonMapper.read(SERIALIZED_JOURNAL_ENTRY, JournalEntry::class), equalTo(JOURNAL_ENTRY))
    }
}
