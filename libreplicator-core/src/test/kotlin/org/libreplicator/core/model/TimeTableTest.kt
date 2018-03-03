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

package org.libreplicator.core.model

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class TimeTableTest {
    private val NODE_1 = "node_1"
    private val NODE_2 = "node_2"
    private val NODE_3 = "node_3"

    private lateinit var timeTable: TimeTable

    @Before
    fun setUp() {
        timeTable = TimeTable()
    }

    @Test
    fun get_shouldReturnZero_whenValueNotStoredYet() {
        assertThat(timeTable[NODE_1, NODE_2], equalTo(0L))
    }

    @Test
    fun get_shouldReturnValue_whenValueAlreadyStored() {
        timeTable[NODE_1, NODE_2] = 1L

        assertThat(timeTable[NODE_1, NODE_2], equalTo(1L))
    }

    @Test
    fun merge_shouldMergeTimeTablesWithFewEntries() {
        val timeTable2 = TimeTable()
        timeTable2[NODE_2, NODE_2] = 10L

        timeTable.merge(NODE_1, ReplicatorPayload(NODE_2, listOf(), timeTable2))

        assertThat(timeTable[NODE_1, NODE_1], equalTo(0L))
        assertThat(timeTable[NODE_1, NODE_2], equalTo(10L))
        assertThat(timeTable[NODE_1, NODE_3], equalTo(0L))
        assertThat(timeTable[NODE_2, NODE_1], equalTo(0L))
        assertThat(timeTable[NODE_2, NODE_2], equalTo(10L))
        assertThat(timeTable[NODE_2, NODE_3], equalTo(0L))
        assertThat(timeTable[NODE_3, NODE_1], equalTo(0L))
        assertThat(timeTable[NODE_3, NODE_2], equalTo(0L))
        assertThat(timeTable[NODE_3, NODE_3], equalTo(0L))
    }

    @Test
    fun merge_shouldMergeTimeTablesWithEntries() {
        arrangeTimeTable()
        val otherTimeTable = createOtherTimeTable()

        timeTable.merge(NODE_1, ReplicatorPayload(NODE_2, listOf(), otherTimeTable))

        assertThat(timeTable[NODE_1, NODE_1], equalTo(42L))
        assertThat(timeTable[NODE_1, NODE_2], equalTo(10L))
        assertThat(timeTable[NODE_1, NODE_3], equalTo(12L))
        assertThat(timeTable[NODE_2, NODE_1], equalTo(9L))
        assertThat(timeTable[NODE_2, NODE_2], equalTo(10L))
        assertThat(timeTable[NODE_2, NODE_3], equalTo(3L))
        assertThat(timeTable[NODE_3, NODE_1], equalTo(14L))
        assertThat(timeTable[NODE_3, NODE_2], equalTo(0L))
        assertThat(timeTable[NODE_3, NODE_3], equalTo(0L))
    }

    private fun arrangeTimeTable() {
        timeTable[NODE_1, NODE_1] = 42L
        timeTable[NODE_1, NODE_2] = 2L
        timeTable[NODE_1, NODE_3] = 12L
        timeTable[NODE_2, NODE_1] = 6L
        timeTable[NODE_3, NODE_1] = 8L
    }

    private fun createOtherTimeTable(): TimeTable {
        val otherTimeTable = TimeTable()
        otherTimeTable[NODE_2, NODE_1] = 9L
        otherTimeTable[NODE_2, NODE_2] = 10L
        otherTimeTable[NODE_2, NODE_3] = 3L
        otherTimeTable[NODE_1, NODE_2] = 4L
        otherTimeTable[NODE_1, NODE_3] = 7L
        otherTimeTable[NODE_3, NODE_1] = 14L
        return otherTimeTable
    }
}
