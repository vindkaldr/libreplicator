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

package org.libreplicator.core.router

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.RemoteNode
import org.libreplicator.core.router.api.MessageRouter
import org.libreplicator.core.router.testdouble.InvalidJsonMapperStub
import org.libreplicator.core.router.testdouble.ObserverDummy
import org.libreplicator.core.router.testdouble.ObserverStub
import org.libreplicator.core.router.testdouble.ReplicatorClientStub
import org.libreplicator.core.router.testdouble.ReplicatorServerStub
import org.libreplicator.core.router.testdouble.SubscriptionStub
import org.libreplicator.core.testdouble.JsonMapperStub
import org.libreplicator.core.model.ReplicatorMessage

private const val groupId = "groupId"
private const val otherGroupId = "otherGroupId"
private val remoteNode = RemoteNode("remoteNode", "localhost", 12346)

private const val payload = "payload"
private val message = ReplicatorMessage(groupId, payload)
private const val serializedMessage = "serializedMessage"

private const val otherPayload = "otherPayload"
private val otherMessage = ReplicatorMessage(otherGroupId, otherPayload)
private const val otherSerializedMessage = "otherSerializedMessage"

class DefaultMessageRouterTest {
    private lateinit var jsonMapperStub: JsonMapperStub<ReplicatorMessage>
    private lateinit var replicatorClientStub: ReplicatorClientStub
    private lateinit var subscriptionStub: SubscriptionStub
    private lateinit var replicatorServerStub: ReplicatorServerStub

    private lateinit var observerDummy: ObserverDummy
    private lateinit var observerStub: ObserverStub
    private lateinit var otherObserverStub: ObserverStub

    private lateinit var messageRouter: MessageRouter

    @Before
    fun setUp() {
        jsonMapperStub = JsonMapperStub(
            message to serializedMessage,
            otherMessage to otherSerializedMessage
        )
        replicatorClientStub = ReplicatorClientStub()
        subscriptionStub = SubscriptionStub()
        replicatorServerStub = ReplicatorServerStub(subscriptionStub)

        observerDummy = ObserverDummy()
        observerStub = ObserverStub()
        otherObserverStub = ObserverStub()

        messageRouter = DefaultMessageRouter(
            jsonMapperStub,
            replicatorClientStub,
            replicatorServerStub
        )
    }

    @Test
    fun `subscribe initializes client`() = runBlocking {
        messageRouter.subscribe(groupId, observerDummy)
        assertTrue(replicatorClientStub.isInitialized())
    }

    @Test
    fun `unsubscribe closes client`() = runBlocking {
        messageRouter.subscribe(groupId, observerDummy).unsubscribe()
        assertTrue(replicatorClientStub.isClosed())
    }

    @Test
    fun `routeMessage writes message to client`() = runBlocking {
        messageRouter.routeMessage(remoteNode, message)
        assertTrue(replicatorClientStub.sentMessage(remoteNode, serializedMessage))
    }

    @Test
    fun `subscribe subscribes to server`() = runBlocking {
        messageRouter.subscribe(groupId, observerDummy)
        assertTrue(replicatorServerStub.hasBeenSubscribedTo())
    }

    @Test
    fun `unsubscribe unsubscribes from server`() = runBlocking {
        messageRouter.subscribe(groupId, observerDummy).unsubscribe()
        assertTrue(subscriptionStub.hasBeenUnsubscribedFrom())
    }

    @Test
    fun `router notifies observer about received message`() = runBlocking {
        messageRouter.subscribe(groupId, observerStub)
        replicatorServerStub.observedObserver?.observe(serializedMessage)

        assertThat(observerStub.observedMessage, equalTo(message))
    }

    @Test
    fun `router not notifies observer about invalid message`() = runBlocking {
        messageRouter = DefaultMessageRouter(InvalidJsonMapperStub(), replicatorClientStub, replicatorServerStub)
        messageRouter.subscribe(groupId, observerStub)
        replicatorServerStub.observedObserver?.observe(serializedMessage)

        assertThat(observerStub.observedMessage, nullValue())
    }

    @Test(expected = IllegalStateException::class)
    fun `subscribe not allows subscribing to same group twice`() = runBlocking<Unit> {
        messageRouter.subscribe(groupId, observerStub)
        messageRouter.subscribe(groupId, observerStub)
    }

    @Test(expected = IllegalStateException::class)
    fun `subscribe not allows subscribing to same group twice with other subscriptions`() = runBlocking<Unit> {
        messageRouter.subscribe(groupId, observerStub)
        messageRouter.subscribe(otherGroupId, observerStub)
        messageRouter.subscribe(otherGroupId, observerStub)
    }

    @Test(expected = IllegalStateException::class)
    fun `unsubscribe not allows unsubscribing from the same group twice`() = runBlocking {
        val subscription = messageRouter.subscribe(groupId, observerStub)
        subscription.unsubscribe()
        subscription.unsubscribe()
    }

    @Test
    fun `subscribe initializes client only for the first time`() = runBlocking {
        messageRouter.subscribe(groupId, observerStub)
        messageRouter.subscribe(otherGroupId, observerStub)
        assertTrue(replicatorClientStub.isInitializedOnce())
    }

    @Test
    fun `unsubscribe not closes client if other also subscribed`() = runBlocking {
        val groupSubscription = messageRouter.subscribe(groupId, observerStub)
        messageRouter.subscribe(otherGroupId, observerStub)

        groupSubscription.unsubscribe()

        assertFalse(replicatorClientStub.isClosed())
    }

    @Test
    fun `unsubscribe closes client after last unsubscribed`() = runBlocking {
        val groupSubscription = messageRouter.subscribe(groupId, observerStub)
        val otherGroupSubscription = messageRouter.subscribe(otherGroupId, observerStub)

        groupSubscription.unsubscribe()
        otherGroupSubscription.unsubscribe()

        assertTrue(replicatorClientStub.isClosedOnce())
    }

    @Test
    fun `subscribe subscribes to server only for the first time`() = runBlocking {
        messageRouter.subscribe(groupId, observerStub)
        messageRouter.subscribe(otherGroupId, observerStub)
        assertTrue(replicatorServerStub.hasBeenSubscribedToOnlyOnce())
    }

    @Test
    fun `unsubscribe not unsubscribes from server if other also subscribed`() = runBlocking {
        val groupSubscription = messageRouter.subscribe(groupId, observerStub)
        messageRouter.subscribe(otherGroupId, observerStub)

        groupSubscription.unsubscribe()

        assertFalse(subscriptionStub.hasBeenUnsubscribedFrom())
    }

    @Test
    fun `unsubscribe unsubscribes from server after last unsubscribed`() = runBlocking {
        val groupSubscription = messageRouter.subscribe(groupId, observerStub)
        val otherGroupSubscription = messageRouter.subscribe(otherGroupId, observerStub)

        groupSubscription.unsubscribe()
        otherGroupSubscription.unsubscribe()

        assertTrue(subscriptionStub.hasBeenUnsubscribedFromOnce())
    }

    @Test
    fun `router notifies observers about received messages`() = runBlocking {
        messageRouter.subscribe(groupId, observerStub)
        replicatorServerStub.observedObserver?.observe(serializedMessage)

        messageRouter.subscribe(otherGroupId, otherObserverStub)
        replicatorServerStub.observedObserver?.observe(otherSerializedMessage)

        assertThat(observerStub.observedMessage, equalTo(message))
        assertThat(otherObserverStub.observedMessage, equalTo(otherMessage))
    }

    @Test
    fun `router allows subscribers to come and go`() = runBlocking {
        messageRouter.subscribe(groupId, observerStub)

        messageRouter.subscribe(otherGroupId, otherObserverStub).unsubscribe()
        messageRouter.subscribe(otherGroupId, otherObserverStub)

        replicatorServerStub.observedObserver?.observe(otherSerializedMessage)

        assertThat(otherObserverStub.observedMessage, equalTo(otherMessage))
    }
}
