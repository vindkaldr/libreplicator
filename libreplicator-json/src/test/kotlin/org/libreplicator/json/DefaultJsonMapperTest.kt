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

package org.libreplicator.json

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonMixin
import org.libreplicator.json.mixin.ReplicatorStateMixin
import org.libreplicator.json.mixin.TimeTableMixin
import org.libreplicator.model.ReplicatorPayload
import org.libreplicator.model.ReplicatorState
import org.libreplicator.model.TimeTable

class DefaultJsonMapperTest {
    private companion object {
        private val REPLICATOR_MESSAGE = ReplicatorPayload("nodeId", listOf(), TimeTable())
        private val SERIALIZED_REPLICATOR_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":{}}"

        private val REPLICATOR_STATE = ReplicatorState(LocalNode("", "", 0), listOf(RemoteNode("")), mutableSetOf(), TimeTable())
        private val SERIALIZED_REPLICATOR_STATE = "{\"logs\":[],\"timeTable\":{}}"
        private val DESERIALIZED_REPLICATOR_STATE = ReplicatorState(null, null, mutableSetOf(), TimeTable())

        private val TIME_TABLE = TimeTable(mutableMapOf("nodeId1" to mutableMapOf("nodeId2" to 2L)))
        private val SERIALIZED_TIME_TABLE = "{\"nodeId1\":{\"nodeId2\":2}}"
    }

    private lateinit var jsonMapper: JsonMapper

    @Before
    fun setUp() {
        jsonMapper = DefaultJsonMapper(setOf(JsonMixin(TimeTable::class, TimeTableMixin::class),
                JsonMixin(ReplicatorState::class, ReplicatorStateMixin::class)))
    }

    @Test
    fun write_shouldSerializeReplicatorMessage() {
        assertThat(jsonMapper.write(REPLICATOR_MESSAGE), equalTo(SERIALIZED_REPLICATOR_MESSAGE))
    }

    @Test
    fun read_shouldDeserializeReplicatorMessage() {
        assertThat(jsonMapper.read(SERIALIZED_REPLICATOR_MESSAGE, ReplicatorPayload::class), equalTo(REPLICATOR_MESSAGE))
    }

    @Test
    fun write_shouldSerializeReplicatorState() {
        assertThat(jsonMapper.write(REPLICATOR_STATE), equalTo(SERIALIZED_REPLICATOR_STATE))
    }

    @Test
    fun read_shouldDeserializeReplicatorState() {
        assertThat(jsonMapper.read(SERIALIZED_REPLICATOR_STATE, ReplicatorState::class), equalTo(DESERIALIZED_REPLICATOR_STATE))
    }

    @Test
    fun write_shouldSerializeTimeTable() {
        assertThat(jsonMapper.write(TIME_TABLE), equalTo(SERIALIZED_TIME_TABLE))
    }

    @Test
    fun read_shouldDeserializeTimeTable() {
        assertThat(jsonMapper.read(SERIALIZED_TIME_TABLE, TimeTable::class), equalTo(TIME_TABLE))
    }
}
