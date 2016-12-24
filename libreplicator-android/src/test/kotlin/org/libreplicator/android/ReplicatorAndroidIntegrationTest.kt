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

package org.libreplicator.android

import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.timeout
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.libreplicator.api.RemoteEventLogObserver
import org.libreplicator.api.Replicator
import org.libreplicator.api.ReplicatorNode
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

    private lateinit var replicatorAndroidClient1: ReplicatorAndroidClient
    private lateinit var replicatorAndroidClient2: ReplicatorAndroidClient
    private lateinit var replicatorAndroidClient3: ReplicatorAndroidClient

    private lateinit var node1: ReplicatorNode
    private lateinit var node2: ReplicatorNode
    private lateinit var node3: ReplicatorNode

    private lateinit var replicator1: Replicator
    private lateinit var replicator2: Replicator
    private lateinit var replicator3: Replicator

    @Mock private lateinit var mockObserver1: RemoteEventLogObserver
    @Mock private lateinit var mockObserver2: RemoteEventLogObserver
    @Mock private lateinit var mockObserver3: RemoteEventLogObserver

    @Before
    fun setUp() {
        replicatorAndroidClient1 = ReplicatorAndroidClient()
        replicatorAndroidClient2 = ReplicatorAndroidClient()
        replicatorAndroidClient3 = ReplicatorAndroidClient()

        DaggerReplicatorAndroidComponent.create().inject(replicatorAndroidClient1)
        DaggerReplicatorAndroidComponent.create().inject(replicatorAndroidClient2)
        DaggerReplicatorAndroidComponent.create().inject(replicatorAndroidClient3)

        node1 = replicatorAndroidClient1.replicatorNodeFactory.create("nodeId1", "localhost", 12345)
        node2 = replicatorAndroidClient2.replicatorNodeFactory.create("nodeId2", "localhost", 12346)
        node3 = replicatorAndroidClient3.replicatorNodeFactory.create("nodeId3", "localhost", 12347)

        replicator1 = replicatorAndroidClient1.replicatorFactory.create(node1, listOf(node2, node3), mockObserver1)
        replicator2 = replicatorAndroidClient2.replicatorFactory.create(node2, listOf(node1, node3), mockObserver2)
        replicator3 = replicatorAndroidClient3.replicatorFactory.create(node3, listOf(node1, node2), mockObserver3)
    }

    @Test
    fun replicator_shouldReplicateLogsBetweenNodes() {
        replicator1.replicate(replicatorAndroidClient1.localEventLogFactory.create(LOG))
        replicator2.replicate(replicatorAndroidClient2.localEventLogFactory.create(LOG))
        replicator3.replicate(replicatorAndroidClient3.localEventLogFactory.create(LOG))

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
