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

package hu.dreamsequencer.replicator.network

import com.nhaarman.mockito_kotlin.timeout
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import hu.dreamsequencer.replicator.interactor.api.LogDispatcher
import hu.dreamsequencer.replicator.interactor.api.LogRouter
import hu.dreamsequencer.replicator.json.api.JsonMapper
import hu.dreamsequencer.replicator.model.EventNode
import hu.dreamsequencer.replicator.model.ReplicatorMessage
import hu.dreamsequencer.replicator.model.TimeTable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultLogRouterTest {

    private val message = ReplicatorMessage("nodeId", listOf(), TimeTable())
    private val serializedMessage = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"

    @Mock private lateinit var mockJsonMapper: JsonMapper

    private var node1 = EventNode("node1", "localhost", 12345)
    @Mock private lateinit var mockLogDispatcher1: LogDispatcher
    private lateinit var logRouter: LogRouter

    private var node2 = EventNode("node2", "localhost", 12346)
    @Mock private lateinit var mockLogDispatcher2: LogDispatcher
    private lateinit var logRouter2: LogRouter

    @Before
    fun setUp() {
        whenever(mockJsonMapper.write(message)).thenReturn(serializedMessage)
        whenever(mockJsonMapper.read(serializedMessage, ReplicatorMessage::class)).thenReturn(message)

        logRouter = DefaultLogRouter(mockJsonMapper, node1, mockLogDispatcher1)
        logRouter2 = DefaultLogRouter(mockJsonMapper, node2, mockLogDispatcher2)
    }

    @After
    fun tearDown() {
        logRouter.close()
        logRouter2.close()
    }

    @Test
    fun router_shouldRouteMessages() {
        logRouter.send(node2, message)
        logRouter.send(node2, message)

        verify(mockLogDispatcher2, timeout(1000).times(2)).receive(message)
        verifyNoMoreInteractions(mockLogDispatcher2)

        logRouter2.send(node1, message)
        logRouter2.send(node1, message)

        verify(mockLogDispatcher1, timeout(1000).times(2)).receive(message)
        verifyNoMoreInteractions(mockLogDispatcher1)
    }
}
