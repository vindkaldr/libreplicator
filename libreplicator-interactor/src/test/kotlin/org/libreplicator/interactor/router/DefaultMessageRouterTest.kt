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

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.interactor.router.testdouble.MessageObserverDummy
import org.libreplicator.interactor.router.testdouble.ReplicatorClientMock
import org.libreplicator.interactor.router.testdouble.ReplicatorClientProviderStub
import org.libreplicator.interactor.router.testdouble.ReplicatorServerMock
import org.libreplicator.interactor.router.testdouble.ReplicatorServerProviderStub
import org.libreplicator.interactor.router.testdouble.SubscriptionMock
import org.libreplicator.model.EventNode
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable
import org.libreplicator.server.api.ReplicatorServer
import javax.inject.Provider

class DefaultMessageRouterTest {
    private companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val REMOTE_NODE = EventNode("remoteNode", "localhost", 12346)
    }

    private lateinit var replicatorClientMock: ReplicatorClientMock
    private lateinit var replicatorClientProviderStub: Provider<ReplicatorClient>

    private lateinit var messageObserverDummy: MessageObserverDummy
    private lateinit var subscriptionMock: SubscriptionMock
    private lateinit var replicatorServerMock: ReplicatorServerMock
    private lateinit var replicatorServerProviderStub: Provider<ReplicatorServer>

    private lateinit var messageRouter: MessageRouter

    @Before
    fun setUp() {
        replicatorClientMock = ReplicatorClientMock()
        replicatorClientProviderStub = ReplicatorClientProviderStub(replicatorClientMock)

        messageObserverDummy = MessageObserverDummy()
        subscriptionMock = SubscriptionMock()
        replicatorServerMock = ReplicatorServerMock(subscriptionMock)
        replicatorServerProviderStub = ReplicatorServerProviderStub(replicatorServerMock)

        messageRouter = DefaultMessageRouter(replicatorClientProviderStub, replicatorServerProviderStub)
    }

    @Test(expected = NotSubscribedException::class)
    fun routeMessage_shouldThrowException_whenNotSubscribed() {
        messageRouter.routeMessage(REMOTE_NODE, MESSAGE)
    }

    @Test
    fun routeMessage_shouldPassRemoteNodeAndMessage_toReplicatorClient() {
        messageRouter.subscribe(messageObserverDummy)

        messageRouter.routeMessage(REMOTE_NODE, MESSAGE)

        assertTrue(replicatorClientMock.sentMessage(REMOTE_NODE, MESSAGE))
    }

    @Test
    fun unsubscribe_shouldCloseReplicatorClient() {
        messageRouter.subscribe(messageObserverDummy).unsubscribe()

        assertTrue(replicatorClientMock.wasClosed())
    }

    @Test
    fun subscribe_shouldSubscribe_toReplicatorServer() {
        messageRouter.subscribe(messageObserverDummy)

        assertTrue(replicatorServerMock.hasBeenSubscribedTo(messageObserverDummy))
    }

    @Test
    fun unsubscribe_shouldUnsubscribe_fromReplicatorServer() {
        messageRouter.subscribe(messageObserverDummy).unsubscribe()

        assertTrue(subscriptionMock.hasBeenUnsubscribedFrom())
    }
}
