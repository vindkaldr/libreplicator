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

package org.libreplicator.integration

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.ReplicatorTestFactory
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.testdouble.RemoteEventLogObserverMock

private const val localLog = "localLog"
private const val otherLocalLog = "otherLocalLog"
private const val remoteLog = "remoteLog"
private const val otherRemoteLog = "otherRemoteLog"

private val localNode = LocalNode("localNode", "localhost", 12345)
private val remoteNode = RemoteNode("remoteNode", "localhost", 12346)
private val otherRemoteNode = RemoteNode("otherRemoteNode", "localhost", 12347)

private const val groupId = "groupId"
private const val otherGroupId = "otherGroupId"

private val localReplicatorFactory = ReplicatorTestFactory(localNode)
private val localReplicator = localReplicatorFactory.create(groupId, listOf(remoteNode))
private val localObserver = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 1)
private val otherLocalReplicator = localReplicatorFactory.create(otherGroupId, listOf(otherRemoteNode))
private val otherLocalObserver = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 1)

private val remoteReplicatorFactory = ReplicatorTestFactory(remoteNode.toLocalNode())
private val remoteReplicator = remoteReplicatorFactory.create(groupId, listOf(localNode.toRemoteNode()))
private val remoteObserver = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 1)

private val otherRemoteReplicatorFactory = ReplicatorTestFactory(otherRemoteNode.toLocalNode())
private val otherRemoteReplicator = otherRemoteReplicatorFactory.create(otherGroupId, listOf(localNode.toRemoteNode()))
private val otherRemoteObserver = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 1)

private fun LocalNode.toRemoteNode() = RemoteNode(nodeId, hostname, port)
private fun RemoteNode.toLocalNode() = LocalNode(nodeId, hostname, port)

class ReplicatorRouterIntegrationTest {
    @Test
    fun `libreplicator uses one local server and routes messages to replicators`() = runBlocking {
        val localSubscription = localReplicator.subscribe(localObserver)
        val otherLocalSubscription = otherLocalReplicator.subscribe(otherLocalObserver)
        val remoteSubscription = remoteReplicator.subscribe(remoteObserver)
        val otherRemoteSubscription = otherRemoteReplicator.subscribe(otherRemoteObserver)

        localReplicator.replicate(localLog)
        otherLocalReplicator.replicate(otherLocalLog)
        remoteReplicator.replicate(remoteLog)
        otherRemoteReplicator.replicate(otherRemoteLog)

        assertThat(localObserver.getObservedLogs(), equalTo(listOf(remoteLog)))
        assertThat(otherLocalObserver.getObservedLogs(), equalTo(listOf(otherRemoteLog)))
        assertThat(remoteObserver.getObservedLogs(), equalTo(listOf(localLog)))
        assertThat(otherRemoteObserver.getObservedLogs(), equalTo(listOf(otherLocalLog)))

        localSubscription.unsubscribe()
        otherLocalSubscription.unsubscribe()
        remoteSubscription.unsubscribe()
        otherRemoteSubscription.unsubscribe()
    }
}
