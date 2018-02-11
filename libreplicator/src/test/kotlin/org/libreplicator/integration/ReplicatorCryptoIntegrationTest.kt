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
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.ReplicatorSettings
import org.libreplicator.ReplicatorTestFactory
import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.module.replicator.setting.ReplicatorCryptoSettings
import org.libreplicator.testdouble.RemoteEventLogObserverMock

private const val groupId = "groupId"

class ReplicatorCryptoIntegrationTest {
    private companion object {
        private val LOCAL_NODE_ID = "localNodeId"
        private val LOCAL_NODE_PORT = 12348

        private val REMOTE_NODE_ID = "remoteNodeId"
        private val REMOTE_NODE_PORT = 12349

        private val NODE_HOST = "localhost"

        private val FIRST_LOG = "log1"
        private val SECOND_LOG = "log2"
        private val THIRD_LOG = "log3"

        private val SHARED_SECRET = "sharedSecret"
    }

    private val localNode = LocalNode(LOCAL_NODE_ID, NODE_HOST, LOCAL_NODE_PORT)
    private val localReplicatorFactory = ReplicatorTestFactory(localNode)

    @Test
    fun replicator_shouldBeAbleToDecryptMessages_withSharedSecret() = runBlocking {
        val remoteNode = LocalNode(REMOTE_NODE_ID, NODE_HOST, REMOTE_NODE_PORT)
        val remoteReplicatorFactory = ReplicatorTestFactory(remoteNode)

        val remoteReplicator = remoteReplicatorFactory.create(
            groupId = groupId,
            remoteNodes = listOf(RemoteNode(localNode.nodeId, localNode.hostname, localNode.port)),
            settings = ReplicatorSettings(
                ReplicatorCryptoSettings(
                    isEncryptionEnabled = true,
                    sharedSecret = SHARED_SECRET
                )
            )
        )
        val remoteEventLogObserverMock = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 3)
        val remoteSubscription = remoteReplicator.subscribe(remoteEventLogObserverMock)

        val localReplicator = localReplicatorFactory.create(
            groupId = groupId,
            remoteNodes = listOf(RemoteNode(remoteNode.nodeId, remoteNode.hostname, remoteNode.port)),
            settings = ReplicatorSettings(
                ReplicatorCryptoSettings(
                    isEncryptionEnabled = true,
                    sharedSecret = SHARED_SECRET
                )
            )
        )
        val localSubscription = localReplicator.subscribe(RemoteEventLogObserverMock())
        localReplicator.replicate(LocalLog(FIRST_LOG))
        localReplicator.replicate(LocalLog(SECOND_LOG))
        localReplicator.replicate(LocalLog(THIRD_LOG))

        assertThat(remoteEventLogObserverMock.getObservedLogs(), containsInAnyOrder(FIRST_LOG, SECOND_LOG, THIRD_LOG))

        localSubscription.unsubscribe()
        remoteSubscription.unsubscribe()
    }

    @Test
    fun replicator_shouldNotBeAbleToDecryptMessages_withoutSharedSecret() = runBlocking {
        val remoteNode = LocalNode(
            REMOTE_NODE_ID,
            NODE_HOST,
            REMOTE_NODE_PORT
        )
        val remoteReplicatorFactory = ReplicatorTestFactory(remoteNode)

        val remoteReplicator = remoteReplicatorFactory.create(
            groupId = groupId,
            remoteNodes = listOf(RemoteNode(localNode.nodeId, localNode.hostname, localNode.port))
        )
        val remoteEventLogObserverMock = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 3)
        val remoteSubscription = remoteReplicator.subscribe(remoteEventLogObserverMock)

        val localReplicator = localReplicatorFactory.create(
            groupId = groupId,
            remoteNodes = listOf(RemoteNode(remoteNode.nodeId, remoteNode.hostname, remoteNode.port)),
            settings = ReplicatorSettings(
                ReplicatorCryptoSettings(
                    isEncryptionEnabled = true,
                    sharedSecret = SHARED_SECRET
                )
            )
        )
        val localSubscription = localReplicator.subscribe(RemoteEventLogObserverMock())
        localReplicator.replicate(LocalLog(FIRST_LOG))
        localReplicator.replicate(LocalLog(SECOND_LOG))
        localReplicator.replicate(LocalLog(THIRD_LOG))

        assertFalse(remoteEventLogObserverMock.observedAnyLogs())

        localSubscription.unsubscribe()
        remoteSubscription.unsubscribe()
    }
}
