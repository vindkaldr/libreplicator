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

package org.libreplicator.json.deserializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.libreplicator.model.TimeTable
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class TimeTableDeserializerTest {
    companion object {
        private val NODE_1 = "node_1"
        private val NODE_2 = "node_2"
        private val NODE_3 = "node_3"
    }

    private lateinit var objectMapper: ObjectMapper

    @Before
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(
                SimpleModule().addDeserializer(TimeTable::class.java, TimeTableDeserializer())
        )
    }

    @Test(expected = ExpectedJsonArrayException::class)
    fun deserializer_shouldThrowException_whenReadingEmptyObject() {
        objectMapper.readValue("{}", TimeTable::class.java)
    }

    @Test(expected = ExpectedStringException::class)
    fun deserializer_shouldThrowException_whenReadingRowWithWrongType() {
        objectMapper.readValue("[{\"rowKey\":2}]", TimeTable::class.java)
    }

    @Test(expected = ExpectedStringException::class)
    fun deserializer_shouldThrowException_whenReadingColumnWithWrongType() {
        objectMapper.readValue("[{\"rowKey\":\"$NODE_1\",\"columnKey\":2}]", TimeTable::class.java)
    }

    @Test(expected = ExpectedLongException::class)
    fun deserializer_shouldThrowException_whenReadingValueWithWrongType() {
        objectMapper.readValue(
                "[" +
                        "{\"rowKey\":\"$NODE_1\",\"columnKey\":\"$NODE_2\",\"value\":\"value\"}" +
                "]", TimeTable::class.java)
    }

    @Test(expected = ExpectedPropertyException::class)
    fun deserializer_shouldThrowException_whenPropertyMissing() {
        objectMapper.readValue(
                "[" +
                        "{\"rowKey\":\"$NODE_1\"}" +
                "]", TimeTable::class.java)
    }

    @Ignore
    @Test
    fun deserializer_shouldReadEmptyString() {
        assertThat(objectMapper.readValue("", TimeTable::class.java), equalTo(TimeTable()))
    }

    @Test
    fun deserializer_shouldReadEmptyArray() {
        val actual = objectMapper.readValue("[]", TimeTable::class.java)
        val expected = TimeTable()

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun deserializer_shouldReadTimeTable() {
        val actual = objectMapper.readValue(
                "[" +
                        "{\"rowKey\":\"$NODE_1\",\"columnKey\":\"$NODE_2\",\"value\":2}," +
                        "{\"rowKey\":\"$NODE_1\",\"columnKey\":\"$NODE_3\",\"value\":3}," +
                        "{\"rowKey\":\"$NODE_2\",\"columnKey\":\"$NODE_3\",\"value\":5}" +
                "]", TimeTable::class.java)

        val expected = TimeTable()
        expected[NODE_1, NODE_2] = 2L
        expected[NODE_1, NODE_3] = 3L
        expected[NODE_2, NODE_3] = 5L

        assertThat(actual, equalTo(expected))
    }
}
