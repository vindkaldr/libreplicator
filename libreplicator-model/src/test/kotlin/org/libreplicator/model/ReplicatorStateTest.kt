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

package org.libreplicator.model

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.api.ReplicatorNode

class ReplicatorStateTest {
    private companion object {
        private val NODE_1_ID = "node1"
        private val NODE_2_ID = "node2"
        private val NODE_3_ID = "node3"

        private val NODE_1: ReplicatorNode = EventNode(NODE_1_ID, "", 0)
        private val NODE_2: ReplicatorNode = EventNode(NODE_2_ID, "", 0)
        private val NODE_3: ReplicatorNode = EventNode(NODE_3_ID, "", 0)

        private val NODE_1_LOG_1 = "node1Log1"
        private val NODE_1_LOG_2 = "node1Log2"
        private val NODE_2_LOG_1 = "node2Log1"
        private val NODE_2_LOG_2 = "node2Log2"
        private val NODE_3_LOG_1 = "node3Log1"
        private val NODE_3_LOG_2 = "node3Log2"
    }

    @Test
    fun getNodesWithMissingEventLogs_shouldReturnNodesWithEventLogsTheyDoNotHave() {
        val timeTable = TimeTable(3)
        timeTable[NODE_1_ID, NODE_1_ID] = 5
        timeTable[NODE_1_ID, NODE_2_ID] = 0
        timeTable[NODE_1_ID, NODE_3_ID] = 1
        timeTable[NODE_2_ID, NODE_1_ID] = 2
        timeTable[NODE_2_ID, NODE_2_ID] = 0
        timeTable[NODE_2_ID, NODE_3_ID] = 1
        timeTable[NODE_3_ID, NODE_1_ID] = 0
        timeTable[NODE_3_ID, NODE_2_ID] = 0
        timeTable[NODE_3_ID, NODE_3_ID] = 2

        val node1Log1 = EventLog(NODE_1_ID, 2, NODE_1_LOG_1)
        val node1Log2 = EventLog(NODE_1_ID, 5, NODE_1_LOG_2)
        val node3Log1 = EventLog(NODE_3_ID, 2, NODE_3_LOG_1)

        val state = ReplicatorState(mutableSetOf(node1Log1, node1Log2, node3Log1), timeTable)
        val actual = state.getNodesWithMissingEventLogs(listOf(NODE_1, NODE_2, NODE_3))

        val expected = mapOf(NODE_1 to listOf(node3Log1),
                NODE_2 to listOf(node3Log1, node1Log2),
                NODE_3 to listOf(node1Log1, node1Log2))

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun getMissingEventLogs_shouldReturnEventLogsThatNodeDoesNotHave() {
        val timeTable = TimeTable(3)
        timeTable[NODE_1_ID, NODE_2_ID] = 4
        timeTable[NODE_1_ID, NODE_3_ID] = 2

        val node2Log1 = EventLog(NODE_2_ID, 3, NODE_2_LOG_1)
        val node2Log2 = EventLog(NODE_2_ID, 5, NODE_2_LOG_2)
        val node3Log1 = EventLog(NODE_3_ID, 2, NODE_3_LOG_1)
        val node3Log2 = EventLog(NODE_3_ID, 3, NODE_3_LOG_2)

        val state = ReplicatorState(mutableSetOf(node2Log1, node2Log2, node3Log1, node3Log2), timeTable)
        val actual = state.getMissingEventLogs(NODE_1)

        val expected = listOf(node3Log2, node2Log2)

        assertThat(actual, equalTo(expected))
    }
}
