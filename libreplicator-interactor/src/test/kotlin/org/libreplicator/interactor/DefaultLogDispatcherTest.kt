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

package org.libreplicator.interactor

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.api.LogRouter
import org.libreplicator.interactor.api.LogRouterFactory
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultLogDispatcherTest {
    private companion object {
        private val LOCAL_NODE_ID = "localNode"
        private val REMOTE_NODE_1_ID = "remoteNode1"
        private val REMOTE_NODE_2_ID = "remoteNode2"

        private val LOCAL_LOG = "localLog"
        private val REMOTE_LOG_1 = "remoteLog"
        private val REMOTE_LOG_2 = "remoteLog2"
    }

    @Mock private lateinit var mockLogRouterFactory: LogRouterFactory
    @Mock private lateinit var mockLogRouter: LogRouter

    @Mock private lateinit var mockLocalNode: ReplicatorNode
    @Mock private lateinit var mockRemoteNode1: ReplicatorNode
    @Mock private lateinit var mockRemoteNode2: ReplicatorNode

    private lateinit var logDispatcher: LogDispatcher
    @Mock private lateinit var mockLogObserver: Observer<RemoteEventLog>

    private lateinit var messageObserver: Observer<ReplicatorMessage>

    private lateinit var localEventLog: LocalEventLog

    private lateinit var remoteEventLog1: EventLog
    private lateinit var remoteTimeTable1: TimeTable
    private lateinit var replicatorMessage1: ReplicatorMessage

    private lateinit var remoteEventLog2: EventLog
    private lateinit var remoteTimeTable2: TimeTable
    private lateinit var replicatorMessage2: ReplicatorMessage

    @Before
    fun setUp() {
        whenever(mockLogRouterFactory.create(eq(mockLocalNode))).thenReturn(mockLogRouter)

        whenever(mockLocalNode.nodeId).thenReturn(LOCAL_NODE_ID)
        whenever(mockRemoteNode1.nodeId).thenReturn(REMOTE_NODE_1_ID)
        whenever(mockRemoteNode2.nodeId).thenReturn(REMOTE_NODE_2_ID)

        logDispatcher = DefaultLogDispatcher(mockLogRouterFactory,
                mockLocalNode, listOf(mockRemoteNode1, mockRemoteNode2))
        logDispatcher.subscribe(mockLogObserver)

        val messageObserverCaptor = argumentCaptor<Observer<ReplicatorMessage>>()
        verify(mockLogRouter).subscribe(messageObserverCaptor.capture())
        messageObserver = messageObserverCaptor.firstValue
    }

    @Test
    fun dispatch_shouldDispatchLog() {
        localEventLog = EventLog("", 0, LOCAL_LOG)

        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOCAL_LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOCAL_LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }

    @Test
    fun receive_shouldNotifyObserver_whenUnknownLogReceived() {
        remoteEventLog1 = EventLog(REMOTE_NODE_1_ID, 1, REMOTE_LOG_1)
        remoteTimeTable1 = TimeTable(3)
        remoteTimeTable1[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 1
        replicatorMessage1 = ReplicatorMessage(REMOTE_NODE_1_ID, listOf(remoteEventLog1), remoteTimeTable1)

        messageObserver.observe(replicatorMessage1)

        verify(mockLogObserver).observe(remoteEventLog1)
        verifyNoMoreInteractions(mockLogObserver)
    }

    @Test
    fun dispatch_shouldDispatchLogsThatAreUnknownToNodes() {
        remoteEventLog1 = EventLog(REMOTE_NODE_1_ID, 1, REMOTE_LOG_1)
        remoteTimeTable1 = TimeTable(3)
        remoteTimeTable1[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 1
        replicatorMessage1 = ReplicatorMessage(REMOTE_NODE_1_ID, listOf(remoteEventLog1), remoteTimeTable1)
        messageObserver.observe(replicatorMessage1)

        verify(mockLogObserver).observe(remoteEventLog1)
        verifyNoMoreInteractions(mockLogObserver)

        localEventLog = EventLog("", 0, LOCAL_LOG)
        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOCAL_LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(2))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(REMOTE_NODE_1_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(REMOTE_LOG_1))
            assertThat(replicatorMessage.eventLogs[1].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[1].log, equalTo(LOCAL_LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }

    @Test
    fun dispatch_shouldDispatchMissingAndLocalLog_whenInformedAboutOutOfDateNode() {
        remoteEventLog1 = EventLog(REMOTE_NODE_1_ID, 1, REMOTE_LOG_1)
        remoteEventLog2 = EventLog(REMOTE_NODE_2_ID, 2, REMOTE_LOG_2)
        remoteTimeTable2 = TimeTable(3)
        remoteTimeTable2[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 1
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_2_ID] = 2
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_1_ID] = 1
        replicatorMessage2 = ReplicatorMessage(REMOTE_NODE_2_ID, listOf(remoteEventLog1, remoteEventLog2), remoteTimeTable2)
        messageObserver.observe(replicatorMessage2)

        verify(mockLogObserver).observe(remoteEventLog1)
        verify(mockLogObserver).observe(remoteEventLog2)
        verifyNoMoreInteractions(mockLogObserver)

        localEventLog = EventLog("", 0, LOCAL_LOG)
        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(2))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(REMOTE_NODE_2_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(REMOTE_LOG_2))
            assertThat(replicatorMessage.eventLogs[1].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[1].log, equalTo(LOCAL_LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOCAL_LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }

    @Test
    fun dispatch_shouldDispatchOnlyLocalLog_whenInformedThatOthersUpToDate() {
        remoteEventLog1 = EventLog(REMOTE_NODE_1_ID, 2, REMOTE_LOG_1)
        remoteEventLog2 = EventLog(REMOTE_NODE_2_ID, 2, REMOTE_LOG_2)
        remoteTimeTable2 = TimeTable(3)
        remoteTimeTable2[REMOTE_NODE_1_ID, REMOTE_NODE_1_ID] = 2
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_2_ID] = 2
        remoteTimeTable2[REMOTE_NODE_2_ID, REMOTE_NODE_1_ID] = 2
        remoteTimeTable2[REMOTE_NODE_1_ID, REMOTE_NODE_2_ID] = 2
        replicatorMessage2 = ReplicatorMessage(REMOTE_NODE_2_ID, listOf(remoteEventLog1, remoteEventLog2), remoteTimeTable2)
        messageObserver.observe(replicatorMessage2)

        verify(mockLogObserver).observe(remoteEventLog1)
        verify(mockLogObserver).observe(remoteEventLog2)
        verifyNoMoreInteractions(mockLogObserver)

        localEventLog = EventLog("", 0, LOCAL_LOG)
        logDispatcher.dispatch(localEventLog)

        verify(mockLogRouter).send(eq(mockRemoteNode1), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOCAL_LOG))
        })
        verify(mockLogRouter).send(eq(mockRemoteNode2), check<ReplicatorMessage> { replicatorMessage ->
            assertThat(replicatorMessage.eventLogs.size, equalTo(1))
            assertThat(replicatorMessage.eventLogs[0].nodeId, equalTo(LOCAL_NODE_ID))
            assertThat(replicatorMessage.eventLogs[0].log, equalTo(LOCAL_LOG))
        })
        verifyNoMoreInteractions(mockLogRouter)
    }
}
