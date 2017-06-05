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

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.EventNode
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.network.api.MessageRouter
import org.libreplicator.network.testdouble.CipherStub
import org.libreplicator.network.testdouble.JsonMapperStub
import org.libreplicator.network.testdouble.MessageObserverDummy
import org.libreplicator.network.testdouble.ReplicatorClientMock
import org.libreplicator.network.testdouble.ReplicatorClientProviderStub
import javax.inject.Provider

class DefaultMessageRouterTest {
    private companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val SERIALIZED_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"
        private val LOCAL_NODE = EventNode("localNode", "localhost", 12345)
        private val REMOTE_NODE = EventNode("remoteNode", "localhost", 12346)
    }

    private lateinit var replicatorClient: ReplicatorClientMock
    private lateinit var replicatorClientProviderStub: Provider<ReplicatorClient>
    private val jsonMapperStub: JsonMapper = JsonMapperStub(message = MESSAGE, deserializedMessage = SERIALIZED_MESSAGE)
    private val cipherStub: Cipher = CipherStub()

    private lateinit var messageRouter: MessageRouter

    @Before
    fun setUp() {
        replicatorClient = ReplicatorClientMock()
        replicatorClientProviderStub = ReplicatorClientProviderStub(replicatorClient)

        messageRouter = DefaultMessageRouter(replicatorClientProviderStub, jsonMapperStub, cipherStub, LOCAL_NODE)
    }

    @Test(expected = NotSubscribedException::class)
    fun routeMessage_shouldThrowException_whenNotSubscribed() {
        messageRouter.routeMessage(REMOTE_NODE, MESSAGE)
    }

    @Test
    fun routeMessage_shouldPassRemoteNodeAndMessage_toReplicatorClient() {
        messageRouter.subscribe(MessageObserverDummy())

        messageRouter.routeMessage(REMOTE_NODE, MESSAGE)

        assertTrue(replicatorClient.sentMessage(REMOTE_NODE, MESSAGE))
    }

    @Test
    fun unsubscribe_shouldCloseReplicatorClient() {
        messageRouter.subscribe(MessageObserverDummy()).unsubscribe()

        assertTrue(replicatorClient.wasClosed())
    }
}
