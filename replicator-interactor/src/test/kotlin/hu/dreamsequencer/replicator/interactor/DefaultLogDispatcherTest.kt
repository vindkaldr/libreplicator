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

package hu.dreamsequencer.replicator.interactor

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import hu.dreamsequencer.replicator.api.LocalEventLog
import hu.dreamsequencer.replicator.api.RemoteEventLogObserver
import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogRouter
import hu.dreamsequencer.replicator.interactor.api.LogRouterFactory
import hu.dreamsequencer.replicator.model.EventLog
import hu.dreamsequencer.replicator.model.ReplicatorMessage
import hu.dreamsequencer.replicator.model.TimeTable
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultLogDispatcherTest {
    private companion object {
        private val LOCAL_NODE_ID = "localNode"
        private val REMOTE_NODE_1_ID = "remoteNode1"
        private val REMOTE_NODE_2_ID = "remoteNode2"

        private val LOG = "log"
        private val REMOTE_LOG = "remoteLog"
        private val REMOTE_LOG_2 = "remoteLog2"
    }

    @Mock private lateinit var mockLogRouterFactory: LogRouterFactory
    @Mock private lateinit var mockLogRouter: LogRouter

    @Mock private lateinit var mockLocalNode: ReplicatorNode
    @Mock private lateinit var mockRemoteNode1: ReplicatorNode
    @Mock private lateinit var mockRemoteNode2: ReplicatorNode
    @Mock private lateinit var mockObserver: RemoteEventLogObserver

    private lateinit var logDispatcher: LogDispatcher

    private lateinit var localEventLog: LocalEventLog
    private lateinit var remoteEventLog: EventLog
    private lateinit var remoteTimeTable: TimeTable
    private lateinit var remoteEventLog2: EventLog
    private lateinit var remoteTimeTable2: TimeTable
    private lateinit var replicatorMessage: ReplicatorMessage
    private lateinit var replicatorMessage2: ReplicatorMessage

    @Before
    fun setUp() {
        whenever(mockLogRouterFactory.create(eq(mockLocalNode), any())).thenReturn(mockLogRouter)

        whenever(mockLocalNode.nodeId).thenReturn(LOCAL_NODE_ID)
        whenever(mockRemoteNode1.nodeId).thenReturn(REMOTE_NODE_1_ID)
        whenever(mockRemoteNode2.nodeId).thenReturn(REMOTE_NODE_2_ID)

        logDispatcher = DefaultLogDispatcher(mockLogRouterFactory,
                mockLocalNode, listOf(mockRemoteNode1, mockRemoteNode2), mockObserver)
    }

    @Test
    fun dispatch_shouldDispatchLog() {
        localEventLog = EventLog("", 0, LOG)

        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }

    @Test
    fun receive_shouldNotifyObserver_whenUnknownLogReceived() {
        remoteEventLog = EventLog(REMOTE_NODE_1_ID, 1, REMOTE_LOG)
        remoteTimeTable = TimeTable(3)
        remoteTimeTable[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 1
        replicatorMessage = ReplicatorMessage(REMOTE_NODE_1_ID, listOf(remoteEventLog), remoteTimeTable)

        logDispatcher.receive(replicatorMessage)

        verify(mockObserver).observe(remoteEventLog)
        verifyNoMoreInteractions(mockObserver)
    }

    @Test
    fun dispatch_shouldDispatchLogsThatAreUnknownToNodes() {
        remoteEventLog = EventLog(REMOTE_NODE_1_ID, 1, REMOTE_LOG)
        remoteTimeTable = TimeTable(3)
        remoteTimeTable[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 1
        replicatorMessage = ReplicatorMessage(REMOTE_NODE_1_ID, listOf(remoteEventLog), remoteTimeTable)
        logDispatcher.receive(replicatorMessage)

        verify(mockObserver).observe(remoteEventLog)
        verifyNoMoreInteractions(mockObserver)

        localEventLog = EventLog("", 0, LOG)
        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(2))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(REMOTE_NODE_1_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(REMOTE_LOG))
            assertThat(replicatorMessage.eventLogs[1].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[1].log, equalTo(LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }

    @Test
    fun dispatch_shouldDispatchMissingAndLocalLog_whenInformedAboutOutOfDateNode() {
        remoteEventLog = EventLog(REMOTE_NODE_1_ID, 1, REMOTE_LOG)
        remoteEventLog2 = EventLog(REMOTE_NODE_2_ID, 2, REMOTE_LOG_2)
        remoteTimeTable2 = TimeTable(3)
        remoteTimeTable2[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 1
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_2_ID] = 2
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_1_ID] = 1
        replicatorMessage2 = ReplicatorMessage(REMOTE_NODE_2_ID, listOf(remoteEventLog, remoteEventLog2), remoteTimeTable2)
        logDispatcher.receive(replicatorMessage2)

        verify(mockObserver).observe(remoteEventLog)
        verify(mockObserver).observe(remoteEventLog2)
        verifyNoMoreInteractions(mockObserver)

        localEventLog = EventLog("", 0, LOG)
        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(2))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(REMOTE_NODE_2_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(REMOTE_LOG_2))
            assertThat(replicatorMessage.eventLogs[1].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[1].log, equalTo(LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }

    @Test
    fun dispatch_shouldDispatchOnlyLocalLog_whenInformedThatOthersUpToDate() {
        remoteEventLog = EventLog(REMOTE_NODE_1_ID, 2, REMOTE_LOG)
        remoteEventLog2 = EventLog(REMOTE_NODE_2_ID, 2, REMOTE_LOG_2)
        remoteTimeTable2 = TimeTable(3)
        remoteTimeTable2[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 2
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_2_ID] = 2
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_1_ID] = 2
        remoteTimeTable2[REMOTE_NODE_1_ID, REMOTE_NODE_2_ID] = 2
        replicatorMessage2 = ReplicatorMessage(REMOTE_NODE_2_ID, listOf(remoteEventLog, remoteEventLog2), remoteTimeTable2)
        logDispatcher.receive(replicatorMessage2)

        verify(mockObserver).observe(remoteEventLog)
        verify(mockObserver).observe(remoteEventLog2)
        verifyNoMoreInteractions(mockObserver)

        localEventLog = EventLog("", 0, LOG)
        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }
}
