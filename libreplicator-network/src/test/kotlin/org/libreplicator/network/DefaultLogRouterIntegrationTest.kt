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

import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.Subscription
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.EventNode
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.network.api.LogRouter

class DefaultLogRouterIntegrationTest {
    companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val SERIALIZED_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"

        private val NODE_1 = EventNode("node1", "localhost", 12345)
        private val NODE_2 = EventNode("node2", "localhost", 12346)
    }

    private var jsonMapperStub: JsonMapper= JsonMapperStub(MESSAGE, SERIALIZED_MESSAGE)

    private lateinit var logRouter1: LogRouter
    private lateinit var messageObserverMock1: MessageObserverMock
    private lateinit var subscription1: Subscription

    private lateinit var logRouter2: LogRouter
    private lateinit var messageObserverMock2: MessageObserverMock
    private lateinit var subscription2: Subscription

    @Before
    fun setUp() {
        logRouter1 = DefaultLogRouter(jsonMapperStub, NODE_1)
        logRouter2 = DefaultLogRouter(jsonMapperStub, NODE_2)
    }

    @After
    fun tearDown() {
        subscription1.unsubscribe()
        subscription2.unsubscribe()
    }

    @Test
    fun router_shouldRouteMessage() {
        messageObserverMock1 = MessageObserverMock.createWithExpectedMessageCount(1)
        subscription1 = logRouter1.subscribe(messageObserverMock1)
        messageObserverMock2 = MessageObserverMock.createWithExpectedMessageCount(1)
        subscription2 = logRouter2.subscribe(messageObserverMock2)

        logRouter1.send(NODE_2, MESSAGE)
        logRouter2.send(NODE_1, MESSAGE)

        assertThat(messageObserverMock1.getObservedMessages(), equalTo(listOf(MESSAGE)))
        assertThat(messageObserverMock2.getObservedMessages(), equalTo(listOf(MESSAGE)))
    }

    @Test
    fun router_shouldRouteMessages() {
        messageObserverMock1 = MessageObserverMock.createWithExpectedMessageCount(2)
        subscription1 = logRouter1.subscribe(messageObserverMock1)
        messageObserverMock2 = MessageObserverMock.createWithExpectedMessageCount(2)
        subscription2 = logRouter2.subscribe(messageObserverMock2)

        logRouter1.send(NODE_2, MESSAGE)
        logRouter1.send(NODE_2, MESSAGE)

        logRouter2.send(NODE_1, MESSAGE)
        logRouter2.send(NODE_1, MESSAGE)

        assertThat(messageObserverMock1.getObservedMessages(), equalTo(listOf(MESSAGE, MESSAGE)))
        assertThat(messageObserverMock2.getObservedMessages(), equalTo(listOf(MESSAGE, MESSAGE)))
    }
}
