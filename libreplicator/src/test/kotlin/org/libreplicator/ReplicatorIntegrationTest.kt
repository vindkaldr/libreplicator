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
import org.libreplicator.api.Replicator
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ReplicatorIntegrationTest {
    private companion object {
        private val LOG_1_1 = "log11"
        private val LOG_1_2 = "log12"
        private val LOG_2_1 = "log21"
        private val LOG_2_2 = "log22"
        private val LOG_3_1 = "log31"
        private val LOG_3_2 = "log32"

        private val TIMEOUT_IN_MILLIS = 3000L
    }

    private lateinit var libReplicatorFactory: LibReplicatorFactory

    private lateinit var node1: ReplicatorNode
    private lateinit var node2: ReplicatorNode
    private lateinit var node3: ReplicatorNode

    private lateinit var replicator1: Replicator
    private lateinit var replicator2: Replicator
    private lateinit var replicator3: Replicator

    @Mock private lateinit var mockLogObserver1: Observer<RemoteEventLog>
    @Mock private lateinit var mockLogObserver2: Observer<RemoteEventLog>
    @Mock private lateinit var mockLogObserver3: Observer<RemoteEventLog>

    private lateinit var subscription1: Subscription
    private lateinit var subscription2: Subscription
    private lateinit var subscription3: Subscription

    @Before
    fun setUp() {
        libReplicatorFactory = LibReplicatorFactory()

        node1 = libReplicatorFactory.createReplicatorNode("nodeId1", "localhost", 12345)
        node2 = libReplicatorFactory.createReplicatorNode("nodeId2", "localhost", 12346)
        node3 = libReplicatorFactory.createReplicatorNode("nodeId3", "localhost", 12347)

        replicator1 = libReplicatorFactory.createReplicator(node1, listOf(node2, node3))
        subscription1 = replicator1.subscribe(mockLogObserver1)

        replicator2 = libReplicatorFactory.createReplicator(node2, listOf(node1, node3))
        subscription2 = replicator2.subscribe(mockLogObserver2)

        replicator3 = libReplicatorFactory.createReplicator(node3, listOf(node1, node2))
        subscription3 = replicator3.subscribe(mockLogObserver3)
    }

    @After
    fun tearDown() {
        subscription1.unsubscribe()
        subscription2.unsubscribe()
        subscription3.unsubscribe()
    }

    @Test
    fun replicator_shouldReplicateLogsBetweenNodes() {
        replicate(replicator1, LOG_1_1)
        replicate(replicator2, LOG_2_1)
        replicate(replicator3, LOG_3_1)

        subscription1.unsubscribe()
        subscription1 = replicator1.subscribe(mockLogObserver1)

        subscription2.unsubscribe()
        subscription2 = replicator2.subscribe(mockLogObserver2)

        subscription3.unsubscribe()
        subscription3 = replicator3.subscribe(mockLogObserver3)

        replicate(replicator1, LOG_1_2)
        replicate(replicator2, LOG_2_2)
        replicate(replicator3, LOG_3_2)

        verifyLogObserverAndAssertLogs(mockLogObserver1, LOG_2_1, LOG_3_1, LOG_2_2, LOG_3_2)
        verifyLogObserverAndAssertLogs(mockLogObserver2, LOG_1_1, LOG_3_1, LOG_1_2, LOG_3_2)
        verifyLogObserverAndAssertLogs(mockLogObserver3, LOG_1_1, LOG_2_1, LOG_1_2, LOG_2_2)
    }

    private fun replicate(replicator: Replicator, vararg logs: String) {
        logs.forEach { replicator.replicate(libReplicatorFactory.createLocalEventLog(it)) }
    }

    private fun verifyLogObserverAndAssertLogs(mockLogObserver: Observer<RemoteEventLog>, vararg logs: String) {
        val argumentCaptor = argumentCaptor<RemoteEventLog>()

        verify(mockLogObserver, timeout(TIMEOUT_IN_MILLIS).times(logs.size)).observe(argumentCaptor.capture())
        assertThat(argumentCaptor.allValues.map { it.log }, containsInAnyOrder(*logs))

        verifyNoMoreInteractions(mockLogObserver)
    }
}
