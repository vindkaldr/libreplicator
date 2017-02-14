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

package org.libreplicator

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.timeout
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class ReplicatorJournalingIntegrationTest {
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

        private val TIMEOUT_IN_MILLIS = 3000L
    }

    private lateinit var localReplicatorSettings: ReplicatorSettings
    private lateinit var localReplicatorFactory: ReplicatorFactory
    @Mock private lateinit var mockLocalEventLogObserver: Observer<RemoteEventLog>
    private lateinit var localReplicatorSubscription: Subscription

    private lateinit var remoteReplicatorFactory: ReplicatorFactory
    @Mock private lateinit var mockRemoteEventLogObserver: Observer<RemoteEventLog>
    private lateinit var remoteReplicatorSubscription: Subscription

    private val replicatorNodeFactory = ReplicatorNodeFactory()
    private val localEventLogFactory = LocalEventLogFactory()

    private lateinit var localNode: ReplicatorNode
    private lateinit var remoteNode: ReplicatorNode

    @Before
    fun setUp() {
        localReplicatorSettings = ReplicatorSettings(isJournalingEnabled = true, directoryOfJournals = DIRECTORY_OF_JOURNALS)
        localReplicatorFactory = ReplicatorFactory(localReplicatorSettings)

        remoteReplicatorFactory = ReplicatorFactory()

        localNode = replicatorNodeFactory.create(LOCAL_NODE_ID, NODE_HOST, LOCAL_NODE_PORT)
        remoteNode = replicatorNodeFactory.create(REMOTE_NODE_ID, NODE_HOST, REMOTE_NODE_PORT)
    }

    @After
    fun tearDown() {
        DIRECTORY_OF_JOURNALS.toFile().deleteRecursively()

        localReplicatorSubscription.unsubscribe()
        remoteReplicatorSubscription.unsubscribe()
    }

    @Test
    fun replicator_shouldKeepState_whenJournalingEnabled() {
        var localReplicator = localReplicatorFactory.create(localNode, listOf(remoteNode))
        localReplicatorSubscription = localReplicator.subscribe(mockLocalEventLogObserver)

        localReplicator.replicate(localEventLogFactory.create(FIRST_LOG))
        localReplicator.replicate(localEventLogFactory.create(SECOND_LOG))

        localReplicatorSubscription.unsubscribe()

        localReplicator = localReplicatorFactory.create(localNode, listOf(remoteNode))
        localReplicatorSubscription = localReplicator.subscribe(mockLocalEventLogObserver)

        val remoteReplicator = remoteReplicatorFactory.create(remoteNode, listOf(localNode))
        remoteReplicatorSubscription = remoteReplicator.subscribe(mockRemoteEventLogObserver)

        localReplicator.replicate(localEventLogFactory.create(THIRD_LOG))

        verifyLogObserverAndAssertLogs(mockRemoteEventLogObserver, FIRST_LOG, SECOND_LOG, THIRD_LOG)
    }

    private fun verifyLogObserverAndAssertLogs(mockLogObserver: Observer<RemoteEventLog>, vararg logs: String) {
        val argumentCaptor = argumentCaptor<RemoteEventLog>()

        verify(mockLogObserver, timeout(TIMEOUT_IN_MILLIS).times(logs.size)).observe(argumentCaptor.capture())
        assertThat(argumentCaptor.allValues.map { it.log }, containsInAnyOrder(*logs))

        verifyNoMoreInteractions(mockLogObserver)
    }
}
