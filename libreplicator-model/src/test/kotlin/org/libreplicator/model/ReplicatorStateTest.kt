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

package org.libreplicator.model

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteLog
import org.libreplicator.api.RemoteNode
import org.libreplicator.model.testdouble.StateObserverMock

class ReplicatorStateTest {
    private companion object {
        private val NODE_1_ID = "node1"
        private val REMOTE_NODE_1_ID = "remoteNode1"
        private val REMOTE_NODE_2_ID = "remoteNode2"
        private val REMOTE_NODE_3_ID = "remoteNode3"

        private val LOCAL_NODE = LocalNode(NODE_1_ID, "", 0)
        private val REMOTE_NODE_1 = RemoteNode(REMOTE_NODE_1_ID, "", 0)
        private val REMOTE_NODE_2 = RemoteNode(REMOTE_NODE_2_ID, "", 0)
        private val REMOTE_NODE_3 = RemoteNode(REMOTE_NODE_3_ID, "", 0)

        private val NODE_1_LOG_1 = "node1Log1"
        private val NODE_1_LOG_2 = "node1Log2"
        private val NODE_2_LOG_1 = "node2Log1"
        private val NODE_2_LOG_2 = "node2Log2"
        private val NODE_3_LOG_1 = "node3Log1"
        private val NODE_3_LOG_2 = "node3Log2"

        val NODE_1_EVENT_LOG_1 = LocalLog(NODE_1_LOG_1)
        val NODE_2_EVENT_LOG_1 = RemoteLog(REMOTE_NODE_2_ID, 1L, NODE_2_LOG_1)
        val NODE_2_REPLICATOR_MESSAGE = ReplicatorMessage(REMOTE_NODE_2_ID, listOf(NODE_2_EVENT_LOG_1),
                TimeTable(mutableMapOf(REMOTE_NODE_2_ID to mutableMapOf(REMOTE_NODE_2_ID to 1L))))
    }

    private lateinit var replicatorStateWithRemoteNode2: ReplicatorState
    private lateinit var replicatorStateWithRemoteNode2And3: ReplicatorState
    private lateinit var replicatorStateWithRemoteNode1And2And3: ReplicatorState
    private lateinit var stateObserverMock: StateObserverMock

    @Before
    fun setUp() {
        replicatorStateWithRemoteNode2 = ReplicatorState(LOCAL_NODE, listOf(REMOTE_NODE_2))
        replicatorStateWithRemoteNode2And3 = ReplicatorState(LOCAL_NODE, listOf(REMOTE_NODE_2, REMOTE_NODE_3))
        replicatorStateWithRemoteNode1And2And3 = ReplicatorState(LOCAL_NODE, listOf(REMOTE_NODE_1, REMOTE_NODE_2, REMOTE_NODE_3))
        stateObserverMock = StateObserverMock(numberOfExpectedStates = 1)
    }

    @Test
    fun getNodesWithMissingEventLogs_shouldReturnNodesWithEventLogsTheyDoNotHave() {
        val timeTable = TimeTable()
        timeTable[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 5
        timeTable[REMOTE_NODE_1_ID, REMOTE_NODE_2_ID] = 0
        timeTable[REMOTE_NODE_1_ID, REMOTE_NODE_3_ID] = 1
        timeTable[REMOTE_NODE_2_ID, REMOTE_NODE_1_ID] = 2
        timeTable[REMOTE_NODE_2_ID, REMOTE_NODE_2_ID] = 0
        timeTable[REMOTE_NODE_2_ID, REMOTE_NODE_3_ID] = 1
        timeTable[REMOTE_NODE_3_ID, REMOTE_NODE_1_ID] = 0
        timeTable[REMOTE_NODE_3_ID, REMOTE_NODE_2_ID] = 0
        timeTable[REMOTE_NODE_3_ID, REMOTE_NODE_3_ID] = 2

        val node1Log1 = RemoteLog(REMOTE_NODE_1_ID, 2, NODE_1_LOG_1)
        val node1Log2 = RemoteLog(REMOTE_NODE_1_ID, 5, NODE_1_LOG_2)
        val node3Log1 = RemoteLog(REMOTE_NODE_3_ID, 2, NODE_3_LOG_1)

        val state = ReplicatorState(LOCAL_NODE, listOf(REMOTE_NODE_1, REMOTE_NODE_2, REMOTE_NODE_3),
                mutableSetOf(node1Log1, node1Log2, node3Log1), timeTable)
        val actual = state.getNodesWithMissingEventLogs()

        val expected = mapOf(REMOTE_NODE_1 to ReplicatorMessage(LOCAL_NODE.nodeId, listOf(node3Log1), timeTable),
                REMOTE_NODE_2 to ReplicatorMessage(LOCAL_NODE.nodeId, listOf(node3Log1, node1Log2), timeTable),
                REMOTE_NODE_3 to ReplicatorMessage(LOCAL_NODE.nodeId, listOf(node1Log1, node1Log2), timeTable))

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun getMissingEventLogs_shouldReturnEventLogsThatNodeDoesNotHave() {
        val timeTable = TimeTable()
        timeTable[NODE_1_ID, REMOTE_NODE_2_ID] = 4
        timeTable[NODE_1_ID, REMOTE_NODE_3_ID] = 2

        val node2Log1 = RemoteLog(REMOTE_NODE_2_ID, 3, NODE_2_LOG_1)
        val node2Log2 = RemoteLog(REMOTE_NODE_2_ID, 5, NODE_2_LOG_2)
        val node3Log1 = RemoteLog(REMOTE_NODE_3_ID, 2, NODE_3_LOG_1)
        val node3Log2 = RemoteLog(REMOTE_NODE_3_ID, 3, NODE_3_LOG_2)

        val state = ReplicatorState(LOCAL_NODE, listOf(REMOTE_NODE_2, REMOTE_NODE_3),
                mutableSetOf(node2Log1, node2Log2, node3Log1, node3Log2), timeTable)
        val actual = state.getMissingEventLogs(LOCAL_NODE)

        val expected = listOf(node3Log2, node2Log2)

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun addLocalEventLog_createsAnOutgoingEventLog() = runBlocking {
        replicatorStateWithRemoteNode2.addLocalEventLog(NODE_1_EVENT_LOG_1)

        val remoteNodesWithMessage = replicatorStateWithRemoteNode2.getNodesWithMissingEventLogs()
        assertThat(remoteNodesWithMessage.size, equalTo(1))

        val message = remoteNodesWithMessage[REMOTE_NODE_2]
        assertThat(message?.eventLogs?.size, equalTo(1))

        val missingEventLog = message?.eventLogs?.get(0)
        assertThat(missingEventLog?.nodeId, equalTo(LOCAL_NODE.nodeId))
        assertThat(missingEventLog?.time, not(equalTo(0L)))
        assertThat(missingEventLog?.log, equalTo(NODE_1_EVENT_LOG_1.log))
    }

    @Test
    fun addLocalEventLog_notifiesReplicatorStateObserver() = runBlocking {
        replicatorStateWithRemoteNode1And2And3.subscribe(stateObserverMock)

        replicatorStateWithRemoteNode1And2And3.addLocalEventLog(NODE_1_EVENT_LOG_1)

        assertThat(stateObserverMock.getObservedStates(), equalTo(listOf(replicatorStateWithRemoteNode1And2And3)))
    }

    @Test
    fun updateFromMessage_administersOutgoingEventLog() = runBlocking {
        replicatorStateWithRemoteNode2And3.updateFromMessage(NODE_2_REPLICATOR_MESSAGE)

        val nodesWithMessages = replicatorStateWithRemoteNode2And3.getNodesWithMissingEventLogs()
        assertThat(nodesWithMessages.size, equalTo(1))

        val timeTable = TimeTable()
        timeTable[LOCAL_NODE.nodeId, REMOTE_NODE_2.nodeId] = 1
        timeTable[REMOTE_NODE_2.nodeId, REMOTE_NODE_2.nodeId] = 1

        assertThat(nodesWithMessages[REMOTE_NODE_3],
                equalTo(ReplicatorMessage(LOCAL_NODE.nodeId, listOf(NODE_2_EVENT_LOG_1), timeTable)))
    }

    @Test
    fun updateFromMessage_notifiesReplicatorStateObserver() = runBlocking {
        replicatorStateWithRemoteNode2.subscribe(stateObserverMock)

        replicatorStateWithRemoteNode2.updateFromMessage(NODE_2_REPLICATOR_MESSAGE)

        assertThat(stateObserverMock.getObservedStates(), equalTo(listOf(replicatorStateWithRemoteNode2)))
    }
}
