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
import org.libreplicator.model.ReplicatorState
import org.libreplicator.model.TimeTable

class DefaultJsonMapperTest {
    private companion object {
        private val EVENT_LOG = EventLog("nodeId", 5L, "log")
        private val SERIALIZED_EVENT_LOG = "{\"nodeId\":\"nodeId\",\"time\":5,\"log\":\"log\"}"

        private val REPLICATOR_MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val SERIALIZED_REPLICATOR_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"

        private val REPLICATOR_STATE = ReplicatorState(mutableSetOf(), TimeTable())
        private val SERIALIZED_REPLICATOR_STATE = "{\"logs\":[],\"timeTable\":[]}"
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
    fun write_shouldSerializeReplicatorState() {
        assertThat(jsonMapper.write(REPLICATOR_STATE), equalTo(SERIALIZED_REPLICATOR_STATE))
    }

    @Test
    fun read_shouldDeserializeReplicatorState() {
        assertThat(jsonMapper.read(SERIALIZED_REPLICATOR_STATE, ReplicatorState::class), equalTo(REPLICATOR_STATE))
    }
}
