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

package org.libreplicator.server

import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.eclipse.jetty.http.HttpScheme
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.AlreadySubscribedException
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.model.factory.ReplicatorNodeFactory
import org.libreplicator.server.api.ReplicatorServer
import org.libreplicator.server.testdouble.CipherStub
import org.libreplicator.server.testdouble.JsonMapperStub
import org.libreplicator.server.testdouble.MessageObserverMock

class DefaultReplicatorServerIntegrationTest {
    private companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val SERIALIZED_MESSAGE = "{\"nodeId\":\"nodeId\",\"eventLogs\":[],\"timeTable\":[]}"
        private val ENCRYPTED_MESSAGE = "a99a56aea35c30206f31da9a0164641855dbe332b34f9197d327"

        private val LOCAL_NODE = ReplicatorNodeFactory().create("localNode", "localhost", 12345)
        private val SYNC_PATH = "/sync"
    }

    private val jsonMapperStub: JsonMapper = JsonMapperStub(message = MESSAGE, deserializedMessage = SERIALIZED_MESSAGE)
    private val cipherStub: Cipher = CipherStub(message = SERIALIZED_MESSAGE, encryptedMessage = ENCRYPTED_MESSAGE)

    private lateinit var subscription: Subscription
    private lateinit var messageObserverMock: MessageObserverMock
    private lateinit var replicatorServer: ReplicatorServer

    @Before
    fun setUp() {
        messageObserverMock = MessageObserverMock(numberOfExpectedMessages = 1)
        replicatorServer = DefaultReplicatorServer(jsonMapperStub, cipherStub, LOCAL_NODE)
    }

    @After
    fun tearDown() {
        if (replicatorServer.hasSubscription()) {
            subscription.unsubscribe()
        }
    }

    @Test
    fun hasSubscription_returnFalse() {
        assertFalse(replicatorServer.hasSubscription())
    }

    @Test
    fun hasSubscription_returnsTrue_whenSubscribed() {
        subscription = replicatorServer.subscribe(messageObserverMock)

        assertTrue(replicatorServer.hasSubscription())
    }

    @Test
    fun hasSubscription_returnsFalse_whenNotSubscribed() {
        replicatorServer.subscribe(messageObserverMock).unsubscribe()

        assertFalse(replicatorServer.hasSubscription())
    }

    @Test(expected = AlreadySubscribedException::class)
    fun subscribe_throwsException_whenAlreadySubscribed() {
        subscription = replicatorServer.subscribe(messageObserverMock)

        replicatorServer.subscribe(messageObserverMock)
    }

    @Test(expected = NotSubscribedException::class)
    fun unsubscribe_throwsException_whenNotSubscribed() {
        val subscription = replicatorServer.subscribe(messageObserverMock)
        subscription.unsubscribe()

        subscription.unsubscribe()
    }

    @Test
    fun replicatorServer_shouldDecryptAndDeserializeMessage() {
        subscription = replicatorServer.subscribe(messageObserverMock)

        sendMessage(LOCAL_NODE, SYNC_PATH, ENCRYPTED_MESSAGE)

        assertThat(messageObserverMock.getObservedMessages(), equalTo(listOf(MESSAGE)))
    }

    @Test
    fun replicatorServer_shouldDecryptAndDeserializeMessages() {
        subscription = replicatorServer.subscribe(messageObserverMock)

        sendMessage(LOCAL_NODE, SYNC_PATH, ENCRYPTED_MESSAGE)
        sendMessage(LOCAL_NODE, SYNC_PATH, ENCRYPTED_MESSAGE)

        assertThat(messageObserverMock.getObservedMessages(), equalTo(listOf(MESSAGE, MESSAGE)))
    }

    private fun sendMessage(node: ReplicatorNode, path: String, message: String) {
        val httpClient = HttpClients.createMinimal()

        val httpPost = HttpPost(URIBuilder()
                .setScheme(HttpScheme.HTTP.asString())
                .setHost(node.url)
                .setPort(node.port)
                .setPath(path)
                .build())

        httpPost.entity = StringEntity(message)

        httpClient.use { httpClient.execute(httpPost) }
    }
}
