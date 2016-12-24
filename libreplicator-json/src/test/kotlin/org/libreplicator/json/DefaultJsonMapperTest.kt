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

import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class DefaultJsonMapperTest {

    private lateinit var jsonMapper: JsonMapper

    @Before
    fun setUp() {
        jsonMapper = DefaultJsonMapper()
    }

    @Test
    fun write_shouldSerializeEventLog() {
        val log = EventLog("nodeId", 5L, "log")

        assertThat(jsonMapper.write(log), equalTo("{\"nodeId\":\"nodeId\",\"time\":5,\"log\":\"log\"}"))
    }

    @Test
    fun read_shouldDeserializeEventLog() {
        val log = "{\"nodeId\":\"nodeId\",\"time\":5,\"log\":\"log\"}"

        assertThat(jsonMapper.read(log, EventLog::class), equalTo(EventLog("nodeId", 5L, "log")))
    }

    @Test
    fun write_shouldSerializeReplicatorMessage() {
        val message = ReplicatorMessage("nodeId", listOf(), TimeTable())

        assertThat(jsonMapper.write(message), equalTo("{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"))
    }

    @Test
    fun read_shouldDeserializeReplicatorMessage() {
        val message = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"

        val actual = jsonMapper.read(message, ReplicatorMessage::class)
        val expected = ReplicatorMessage("nodeId", listOf(), TimeTable())

        assertThat(actual, equalTo(expected))
    }
}
