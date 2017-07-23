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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.RemoteNode
import org.libreplicator.interactor.router.testdouble.MessageObserverDummy
import org.libreplicator.interactor.router.testdouble.ReplicatorClientMock
import org.libreplicator.interactor.router.testdouble.ReplicatorServerMock
import org.libreplicator.interactor.router.testdouble.SubscriptionMock
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.model.TimeTable

class DefaultMessageRouterTest {
    private companion object {
        private val MESSAGE = ReplicatorMessage("nodeId", listOf(), TimeTable())
        private val REMOTE_NODE = RemoteNode("remoteNode", "localhost", 12346)
    }

    private lateinit var replicatorClientMock: ReplicatorClientMock

    private lateinit var messageObserverDummy: MessageObserverDummy
    private lateinit var subscriptionMock: SubscriptionMock
    private lateinit var replicatorServerMock: ReplicatorServerMock

    private lateinit var messageRouter: MessageRouter

    @Before
    fun setUp() {
        replicatorClientMock = ReplicatorClientMock()

        messageObserverDummy = MessageObserverDummy()
        subscriptionMock = SubscriptionMock()
        replicatorServerMock = ReplicatorServerMock(subscriptionMock)

        messageRouter = DefaultMessageRouter(replicatorClientMock, replicatorServerMock)
    }

    @Test
    fun routeMessage_shouldPassRemoteNodeAndMessage_toReplicatorClient() = runBlocking {
        messageRouter.subscribe(messageObserverDummy)

        messageRouter.routeMessage(REMOTE_NODE, MESSAGE)

        assertTrue(replicatorClientMock.sentMessage(REMOTE_NODE, MESSAGE))
    }

    @Test
    fun unsubscribe_shouldCloseReplicatorClient() = runBlocking {
        messageRouter.subscribe(messageObserverDummy).unsubscribe()

        assertTrue(replicatorClientMock.wasClosed())
    }

    @Test
    fun subscribe_shouldSubscribe_toReplicatorServer() = runBlocking {
        messageRouter.subscribe(messageObserverDummy)

        assertTrue(replicatorServerMock.hasBeenSubscribedTo(messageObserverDummy))
    }

    @Test
    fun unsubscribe_shouldUnsubscribe_fromReplicatorServer() = runBlocking {
        messageRouter.subscribe(messageObserverDummy).unsubscribe()

        assertTrue(subscriptionMock.hasBeenUnsubscribedFrom())
    }
}
