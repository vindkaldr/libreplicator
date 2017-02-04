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

package org.libreplicator.network

import com.nhaarman.mockito_kotlin.timeout
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.api.Observer
import org.libreplicator.api.Subscription
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.EventNode
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.network.api.LogRouter
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultLogRouterIntegrationTest {
    companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val SERIALIZED_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"

        private val NODE_1 = EventNode("node1", "localhost", 12345)
        private val NODE_2 = EventNode("node2", "localhost", 12346)
    }

    @Mock private lateinit var mockJsonMapper: JsonMapper

    private lateinit var logRouter1: LogRouter
    @Mock private lateinit var mockMessageObserver1: Observer<ReplicatorMessage>
    private lateinit var subscription1: Subscription

    private lateinit var logRouter2: LogRouter
    @Mock private lateinit var mockMessageObserver2: Observer<ReplicatorMessage>
    private lateinit var subscription2: Subscription

    @Before
    fun setUp() {
        whenever(mockJsonMapper.write(MESSAGE)).thenReturn(SERIALIZED_MESSAGE)
        whenever(mockJsonMapper.read(SERIALIZED_MESSAGE, ReplicatorMessage::class)).thenReturn(MESSAGE)

        logRouter1 = DefaultLogRouter(mockJsonMapper, NODE_1)
        subscription1 = logRouter1.subscribe(mockMessageObserver1)

        logRouter2 = DefaultLogRouter(mockJsonMapper, NODE_2)
        subscription2 = logRouter2.subscribe(mockMessageObserver2)
    }

    @After
    fun tearDown() {
        subscription1.unsubscribe()
        subscription2.unsubscribe()
    }

    @Test
    fun router_shouldRouteMessage() {
        logRouter1.send(NODE_2, MESSAGE)

        verify(mockMessageObserver2, timeout(1000)).observe(MESSAGE)
        verifyNoMoreInteractions(mockMessageObserver2)

        logRouter2.send(NODE_1, MESSAGE)

        verify(mockMessageObserver1, timeout(1000)).observe(MESSAGE)
        verifyNoMoreInteractions(mockMessageObserver1)
    }

    @Test
    fun router_shouldRouteMessages() {
        logRouter1.send(NODE_2, MESSAGE)
        logRouter1.send(NODE_2, MESSAGE)

        verify(mockMessageObserver2, timeout(1000).times(2)).observe(MESSAGE)
        verifyNoMoreInteractions(mockMessageObserver2)

        logRouter2.send(NODE_1, MESSAGE)
        logRouter2.send(NODE_1, MESSAGE)

        verify(mockMessageObserver1, timeout(1000).times(2)).observe(MESSAGE)
        verifyNoMoreInteractions(mockMessageObserver1)
    }
}
