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

package org.libreplicator.json.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.libreplicator.json.serializer.TimeTableSerializer
import org.libreplicator.model.TimeTable
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class TimeTableSerializerTest {
    companion object {
        private val NODE_1 = "node_1"
        private val NODE_2 = "node_2"
        private val NODE_3 = "node_3"
    }

    private lateinit var objectMapper: ObjectMapper

    @Before
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(
                SimpleModule().addSerializer(TimeTableSerializer()))
    }

    @Test
    fun serializer_shouldWriteEmptyTimeTable() {
        val timeTable = TimeTable(3)

        assertThat(objectMapper.writeValueAsString(timeTable), equalTo("[]"))
    }

    @Test
    fun serializer_shouldNotWriteZeroValues() {
        val timeTable = TimeTable(3)
        timeTable[NODE_1, NODE_2] = 0L

        assertThat(objectMapper.writeValueAsString(timeTable), equalTo("[]"))
    }

    @Test
    fun serializer_shouldWriteTimeTable() {
        val timeTable = TimeTable(3)
        timeTable[NODE_1, NODE_2] = 2L
        timeTable[NODE_1, NODE_3] = 3L
        timeTable[NODE_2, NODE_3] = 5L

        assertThat(objectMapper.writeValueAsString(timeTable), equalTo(
                "[" +
                        "{\"rowKey\":\"$NODE_1\",\"columnKey\":\"$NODE_2\",\"value\":2}," +
                        "{\"rowKey\":\"$NODE_1\",\"columnKey\":\"$NODE_3\",\"value\":3}," +
                        "{\"rowKey\":\"$NODE_2\",\"columnKey\":\"$NODE_3\",\"value\":5}" +
                "]"))
    }
}
