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
import org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.ReplicatorTestFactory
import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.testdouble.RemoteEventLogObserverMock

private const val groupId = "groupId"

class ReplicatorLocatorIntegrationTest {
    private companion object {
        private val LOG_1 = "log1"
        private val LOG_2 = "log2"
    }

    private val localNode = LocalNode("nodeId1", "localhost", 12345)
    private val localReplicatorFactory = ReplicatorTestFactory(localNode)

    private val remoteNode = LocalNode("nodeId2", "localhost", 12346)
    private val remoteReplicatorFactory = ReplicatorTestFactory(remoteNode)

    private val localReplicator = localReplicatorFactory.create(
        groupId = groupId,
        remoteNodes = listOf(RemoteNode(remoteNode.nodeId)))
    private val remoteReplicator = remoteReplicatorFactory.create(
        groupId = groupId,
        remoteNodes = listOf(RemoteNode(localNode.nodeId)))

    private val localLogObserver = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 1)
    private val remoteLogObserver = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 1)

    @Test
    fun replicator_shouldReplicateLogsBetweenNodes() = runBlocking {
        val localSubscription = localReplicator.subscribe(localLogObserver)
        val remoteSubscription = remoteReplicator.subscribe(remoteLogObserver)

        localReplicator.replicate(LocalLog(LOG_1))
        remoteReplicator.replicate(LocalLog(LOG_2))

        assertThat(localLogObserver.getObservedLogs(), containsInAnyOrder(LOG_2))
        assertThat(remoteLogObserver.getObservedLogs(), containsInAnyOrder(LOG_1))

        localSubscription.unsubscribe()
        remoteSubscription.unsubscribe()
    }
}
