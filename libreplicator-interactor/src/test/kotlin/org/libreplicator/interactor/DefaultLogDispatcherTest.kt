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
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.api.LogRouter
import org.libreplicator.interactor.api.LogRouterFactory
import org.libreplicator.interactor.api.journal.JournalService
import org.libreplicator.model.EventLog
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.model.journal.JournalEntry
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultLogDispatcherTest {
    private companion object {
        private val EVENT_LOG = EventLog.EMPTY
        private val REPLICATOR_MESSAGE = ReplicatorMessage.EMPTY.copy(eventLogs = listOf(EVENT_LOG))
        private val JOURNAL_ENTRY = JournalEntry.EMPTY.copy(replicatorMessage = REPLICATOR_MESSAGE)
    }

    @Mock private lateinit var mockLogRouterFactory: LogRouterFactory
    @Mock private lateinit var mockLogRouter: LogRouter
    @Mock private lateinit var mockLocalNode: ReplicatorNode
    @Mock private lateinit var mockRemoteNodes: List<ReplicatorNode>
    @Mock private lateinit var mockEventLogHandler: EventLogHandler
    @Mock private lateinit var mockJournalService: JournalService

    private lateinit var logDispatcher: LogDispatcher

    @Mock private lateinit var mockLogObserver: Observer<RemoteEventLog>

    @Before
    fun setUp() {
        whenever(mockLogRouterFactory.create(mockLocalNode)).thenReturn(mockLogRouter)
        whenever(mockLocalNode.nodeId).thenReturn("nodeId")
        whenever(mockEventLogHandler.getMissingEventLogs(TimeTable.EMPTY, mockLocalNode, listOf(EventLog.EMPTY))).thenReturn(listOf(EventLog.EMPTY))
        whenever(mockJournalService.write(JOURNAL_ENTRY)).thenReturn(JOURNAL_ENTRY.id)

        logDispatcher = DefaultLogDispatcher(mockLogRouterFactory, mockLocalNode, mockRemoteNodes, mockEventLogHandler, mockJournalService)
    }

    @Test
    fun dispatcher_shouldWriteJournalOnEveryObservedMessage() {
        logDispatcher.subscribe(mockLogObserver)

        val messageObserver = captureSubscribedMessageObserver()
        messageObserver.observe(REPLICATOR_MESSAGE)

        inOrder(mockJournalService, mockLogObserver) {
            verify(mockJournalService).write(JOURNAL_ENTRY)
            verify(mockJournalService).commit(JOURNAL_ENTRY.id)
            verify(mockLogObserver).observe(EVENT_LOG)
            verify(mockJournalService).close(JOURNAL_ENTRY.id)
        }
    }

    private fun captureSubscribedMessageObserver(): Observer<ReplicatorMessage> {
        val argumentCaptor = argumentCaptor<Observer<ReplicatorMessage>>()
        verify(mockLogRouter).subscribe(argumentCaptor.capture())
        return argumentCaptor.firstValue
    }
}
