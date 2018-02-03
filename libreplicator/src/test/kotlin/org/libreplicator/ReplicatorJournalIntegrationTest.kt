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

package org.libreplicator

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.api.Replicator
import org.libreplicator.api.Subscription
import org.libreplicator.module.setting.ReplicatorJournalSettings
import org.libreplicator.testdouble.RemoteEventLogObserverMock
import java.nio.file.Files

class ReplicatorJournalIntegrationTest {
    private companion object {
        private val DIRECTORY_OF_JOURNALS = Files.createTempDirectory("libreplicator-journals-")

        private val LOCAL_NODE_ID = "localNodeId"
        private val LOCAL_NODE_PORT = 12348

        private val REMOTE_NODE_ID = "remoteNodeId"
        private val REMOTE_NODE_PORT = 12349

        private val NODE_HOST = "localhost"

        private val FIRST_LOG = "log1"
        private val SECOND_LOG = "log2"
        private val THIRD_LOG = "log3"
    }

    private val localLibReplicatorSettings = ReplicatorSettings(
            journalSettings = ReplicatorJournalSettings(
                isJournalingEnabled = true,
                directoryOfJournals = DIRECTORY_OF_JOURNALS
            )
    )

    private val localReplicatorFactory = ReplicatorTestFactory(localLibReplicatorSettings)
    private val localEventLogObserverMock = RemoteEventLogObserverMock()
    private val localNode = LocalNode(LOCAL_NODE_ID, NODE_HOST, LOCAL_NODE_PORT)
    private lateinit var localReplicator: Replicator
    private lateinit var localReplicatorSubscription: Subscription

    private val remoteReplicatorFactory = ReplicatorTestFactory()
    private val remoteEventLogObserverMock = RemoteEventLogObserverMock(numberOfExpectedEventLogs = 3)
    private val remoteNode = LocalNode(REMOTE_NODE_ID, NODE_HOST, REMOTE_NODE_PORT)
    private val remoteReplicator = remoteReplicatorFactory.create(localNode = remoteNode,
            remoteNodes = listOf(RemoteNode(localNode.nodeId, localNode.hostname, localNode.port)))
    private lateinit var remoteReplicatorSubscription: Subscription

    @After
    fun tearDown() = runBlocking {
        DIRECTORY_OF_JOURNALS.toFile().deleteRecursively()

        localReplicatorSubscription.unsubscribe()
        remoteReplicatorSubscription.unsubscribe()
    }

    @Test
    fun replicator_shouldKeepState_whenJournalingEnabled() = runBlocking {
        localReplicator = localReplicatorFactory.create(localNode = localNode,
                remoteNodes = listOf(RemoteNode(remoteNode.nodeId, remoteNode.hostname, remoteNode.port)))
        localReplicatorSubscription = localReplicator.subscribe(localEventLogObserverMock)

        localReplicator.replicate(LocalLog(FIRST_LOG))
        localReplicator.replicate(LocalLog(SECOND_LOG))
        localReplicatorSubscription.unsubscribe()

        remoteReplicatorSubscription = remoteReplicator.subscribe(remoteEventLogObserverMock)

        localReplicator = localReplicatorFactory.create(localNode = localNode,
                remoteNodes = listOf(RemoteNode(remoteNode.nodeId, remoteNode.hostname, remoteNode.port)))
        localReplicatorSubscription = localReplicator.subscribe(localEventLogObserverMock)
        localReplicator.replicate(LocalLog(THIRD_LOG))

        assertThat(remoteEventLogObserverMock.getObservedLogs(), containsInAnyOrder(FIRST_LOG, SECOND_LOG, THIRD_LOG))
    }
}
