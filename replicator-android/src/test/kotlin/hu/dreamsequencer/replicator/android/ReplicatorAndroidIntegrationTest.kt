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

package hu.dreamsequencer.replicator.android

import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.timeout
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import hu.dreamsequencer.replicator.api.RemoteEventLogObserver
import hu.dreamsequencer.replicator.api.Replicator
import hu.dreamsequencer.replicator.api.ReplicatorNode
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ReplicatorAndroidIntegrationTest {
    private companion object {
        private val LOG = "log"
    }

    private lateinit var node1: ReplicatorNode
    private lateinit var node2: ReplicatorNode
    private lateinit var node3: ReplicatorNode

    private lateinit var replicator1: Replicator
    private lateinit var replicator2: Replicator
    private lateinit var replicator3: Replicator

    @Mock private lateinit var mockObserver1: RemoteEventLogObserver
    @Mock private lateinit var mockObserver2: RemoteEventLogObserver
    @Mock private lateinit var mockObserver3: RemoteEventLogObserver

    private lateinit var replicatorClient1: ReplicatorClient
    private lateinit var replicatorClient2: ReplicatorClient
    private lateinit var replicatorClient3: ReplicatorClient

    @Before
    fun setUp() {
        replicatorClient1 = ReplicatorClient()
        replicatorClient2 = ReplicatorClient()
        replicatorClient3 = ReplicatorClient()

        DaggerReplicatorComponent.create().inject(replicatorClient1)
        DaggerReplicatorComponent.create().inject(replicatorClient2)
        DaggerReplicatorComponent.create().inject(replicatorClient3)

        node1 = replicatorClient1.replicatorNodeFactory.create("nodeId1", "localhost", 12345)
        node2 = replicatorClient2.replicatorNodeFactory.create("nodeId2", "localhost", 12346)
        node3 = replicatorClient3.replicatorNodeFactory.create("nodeId3", "localhost", 12347)

        replicator1 = replicatorClient1.replicatorFactory.create(node1, listOf(node2, node3), mockObserver1)
        replicator2 = replicatorClient2.replicatorFactory.create(node2, listOf(node1, node3), mockObserver2)
        replicator3 = replicatorClient3.replicatorFactory.create(node3, listOf(node1, node2), mockObserver3)
    }

    @Test
    fun replicator_shouldReplicateLogsBetweenNodes() {
        replicator1.replicate(replicatorClient1.localEventLogFactory.create(LOG))
        replicator2.replicate(replicatorClient2.localEventLogFactory.create(LOG))
        replicator3.replicate(replicatorClient3.localEventLogFactory.create(LOG))

        verify(mockObserver1, timeout(1000).times(2)).observe(check { remoteEventLog ->
            assertThat(remoteEventLog.log, equalTo(LOG))
        })

        verify(mockObserver2, timeout(1000).times(2)).observe(check { remoteEventLog ->
            assertThat(remoteEventLog.log, equalTo(LOG))
        })

        verify(mockObserver3, timeout(1000).times(2)).observe(check { remoteEventLog ->
            assertThat(remoteEventLog.log, equalTo(LOG))
        })

        verifyNoMoreInteractions(mockObserver1)
        verifyNoMoreInteractions(mockObserver2)
        verifyNoMoreInteractions(mockObserver3)
    }
}
