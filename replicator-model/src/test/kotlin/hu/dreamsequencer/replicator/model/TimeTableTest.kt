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

package hu.dreamsequencer.replicator.model

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
        timeTable = TimeTable(3)
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
    fun mergeRow_shouldMergeCellWiseStoringTheLargestValue() {
        timeTable[NODE_1, NODE_2] = 2L
        timeTable[NODE_1, NODE_3] = 3L
        timeTable[NODE_2, NODE_1] = 6L

        val timeTable2 = TimeTable(3)
        timeTable2[NODE_1, NODE_2] = 4L
        timeTable2[NODE_1, NODE_3] = 1L
        timeTable2[NODE_2, NODE_1] = 10L
        timeTable2[NODE_3, NODE_1] = 10L

        timeTable.mergeRow(timeTable2, NODE_1)

        assertThat(timeTable[NODE_1, NODE_2], equalTo(4L))
        assertThat(timeTable[NODE_1, NODE_3], equalTo(3L))
        assertThat(timeTable[NODE_2, NODE_1], equalTo(6L))
        assertThat(timeTable[NODE_3, NODE_1], equalTo(0L))
    }

    @Test
    fun merge_shouldMergeCellWiseStoringTheLargestValue() {
        timeTable[NODE_1, NODE_2] = 2L
        timeTable[NODE_1, NODE_3] = 3L
        timeTable[NODE_2, NODE_1] = 6L

        val timeTable2 = TimeTable(3)
        timeTable2[NODE_1, NODE_2] = 4L
        timeTable2[NODE_1, NODE_3] = 1L
        timeTable2[NODE_2, NODE_1] = 4L
        timeTable2[NODE_3, NODE_1] = 2L

        timeTable.merge(timeTable2)

        assertThat(timeTable[NODE_1, NODE_2], equalTo(4L))
        assertThat(timeTable[NODE_1, NODE_3], equalTo(3L))
        assertThat(timeTable[NODE_2, NODE_1], equalTo(6L))
        assertThat(timeTable[NODE_3, NODE_1], equalTo(2L))
    }
}
