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

package org.libreplicator.interactor.router

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.RemoteNode
import org.libreplicator.interactor.router.testdouble.CipherStub
import org.libreplicator.interactor.router.testdouble.CorruptedCipherStub
import org.libreplicator.interactor.router.testdouble.InvalidJsonMapperStub
import org.libreplicator.interactor.router.testdouble.JsonMapperStub
import org.libreplicator.interactor.router.testdouble.ObserverDummy
import org.libreplicator.interactor.router.testdouble.ObserverStub
import org.libreplicator.interactor.router.testdouble.ReplicatorClientStub
import org.libreplicator.interactor.router.testdouble.ReplicatorServerStub
import org.libreplicator.interactor.router.testdouble.SubscriptionStub
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable

class DefaultMessageRouterTest {
    private companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val SERIALIZED_MESSAGE = "serializedMessage"
        private val ENCRYPTED_SERIALIZED_MESSAGE = "encryptedSerializedMessage"
        private val REMOTE_NODE = RemoteNode("remoteNode", "localhost", 12346)
    }

    private lateinit var jsonMapperStub: JsonMapperStub
    private lateinit var cipherStub: CipherStub
    private lateinit var replicatorClientStub: ReplicatorClientStub
    private lateinit var subscriptionStub: SubscriptionStub
    private lateinit var replicatorServerStub: ReplicatorServerStub

    private lateinit var observerDummy: ObserverDummy
    private lateinit var observerStub: ObserverStub

    private lateinit var messageRouter: MessageRouter

    @Before
    fun setUp() {
        jsonMapperStub = JsonMapperStub(objectToWrite = MESSAGE, stringToRead = SERIALIZED_MESSAGE)
        cipherStub = CipherStub(contentToEncrypt = SERIALIZED_MESSAGE, contentToDecrypt = ENCRYPTED_SERIALIZED_MESSAGE)
        replicatorClientStub = ReplicatorClientStub()
        subscriptionStub = SubscriptionStub()
        replicatorServerStub = ReplicatorServerStub(subscriptionStub)

        observerDummy = ObserverDummy()
        observerStub = ObserverStub()

        messageRouter = DefaultMessageRouter(jsonMapperStub, cipherStub, replicatorClientStub, replicatorServerStub)
    }

    @Test
    fun subscribe_shouldInitializeClient() = runBlocking {
        messageRouter.subscribe(observerDummy)
        assertTrue(replicatorClientStub.wasInitialized())
    }

    @Test
    fun unsubscribe_shouldCloseClient() = runBlocking {
        messageRouter.subscribe(observerDummy).unsubscribe()
        assertTrue(replicatorClientStub.wasClosed())
    }

    @Test
    fun routeMessage_shouldSerializeAndEncryptAndRouteMessageToClient() = runBlocking {
        messageRouter.routeMessage(REMOTE_NODE, MESSAGE)
        assertTrue(replicatorClientStub.sentMessage(REMOTE_NODE, ENCRYPTED_SERIALIZED_MESSAGE))
    }

    @Test
    fun subscribe_shouldSubscribeToServer() = runBlocking {
        messageRouter.subscribe(observerDummy)
        assertTrue(replicatorServerStub.hasBeenSubscribedTo())
    }

    @Test
    fun unsubscribe_shouldUnsubscribeFromServer() = runBlocking {
        messageRouter.subscribe(observerDummy).unsubscribe()
        assertTrue(subscriptionStub.hasBeenUnsubscribedFrom())
    }

    @Test
    fun router_shouldNotifyObserver_aboutReceivedMessage() = runBlocking {
        messageRouter.subscribe(observerStub)
        replicatorServerStub.observedObserver?.observe(ENCRYPTED_SERIALIZED_MESSAGE)

        assertThat(observerStub.observedMessage, equalTo(MESSAGE))
    }

    @Test
    fun router_shouldNotNotifyObserver_aboutCorruptedMessage() = runBlocking {
        messageRouter = DefaultMessageRouter(jsonMapperStub, CorruptedCipherStub(), replicatorClientStub, replicatorServerStub)
        messageRouter.subscribe(observerStub)
        replicatorServerStub.observedObserver?.observe(ENCRYPTED_SERIALIZED_MESSAGE)

        assertThat(observerStub.observedMessage, nullValue())
    }

    @Test
    fun router_shouldNotNotifyObserver_aboutInvalidMessage() = runBlocking {
        messageRouter = DefaultMessageRouter(InvalidJsonMapperStub(), cipherStub, replicatorClientStub, replicatorServerStub)
        messageRouter.subscribe(observerStub)
        replicatorServerStub.observedObserver?.observe(ENCRYPTED_SERIALIZED_MESSAGE)

        assertThat(observerStub.observedMessage, nullValue())
    }
}
