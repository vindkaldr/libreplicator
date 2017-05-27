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

import org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.libreplicator.api.Subscription

class ReplicatorIntegrationTest {
    private companion object {
        private val LOG_1_1 = "log11"
        private val LOG_1_2 = "log12"
        private val LOG_2_1 = "log21"
        private val LOG_2_2 = "log22"
        private val LOG_3_1 = "log31"
        private val LOG_3_2 = "log32"
    }

    private val libReplicatorFactory = LibReplicatorFactory()

    private val node1 = libReplicatorFactory.createReplicatorNode("nodeId1", "localhost", 12345)
    private val node2 = libReplicatorFactory.createReplicatorNode("nodeId2", "localhost", 12346)
    private val node3 = libReplicatorFactory.createReplicatorNode("nodeId3", "localhost", 12347)

    private val replicator1 = libReplicatorFactory.createReplicator(node1, listOf(node2, node3))
    private val replicator2 = libReplicatorFactory.createReplicator(node2, listOf(node1, node3))
    private val replicator3 = libReplicatorFactory.createReplicator(node3, listOf(node1, node2))

    private val eventLogObserverMock1 = EventLogObserverMock.createWithExpectedEventLogCount(4)
    private val eventLogObserverMock2 = EventLogObserverMock.createWithExpectedEventLogCount(4)
    private val eventLogObserverMock3 = EventLogObserverMock.createWithExpectedEventLogCount(4)

    private lateinit var subscription1: Subscription
    private lateinit var subscription2: Subscription
    private lateinit var subscription3: Subscription

    @Before
    fun setUp() {
        subscription1 = replicator1.subscribe(eventLogObserverMock1)
        subscription2 = replicator2.subscribe(eventLogObserverMock2)
        subscription3 = replicator3.subscribe(eventLogObserverMock3)
    }

    @After
    fun tearDown() {
        subscription1.unsubscribe()
        subscription2.unsubscribe()
        subscription3.unsubscribe()
    }

    @Test
    fun replicator_shouldReplicateLogsBetweenNodes() {
        replicator1.replicate(libReplicatorFactory.createLocalEventLog(LOG_1_1))
        replicator2.replicate(libReplicatorFactory.createLocalEventLog(LOG_2_1))
        replicator3.replicate(libReplicatorFactory.createLocalEventLog(LOG_3_1))

        subscription1.unsubscribe()
        subscription1 = replicator1.subscribe(eventLogObserverMock1)

        subscription2.unsubscribe()
        subscription2 = replicator2.subscribe(eventLogObserverMock2)

        subscription3.unsubscribe()
        subscription3 = replicator3.subscribe(eventLogObserverMock3)

        replicator1.replicate(libReplicatorFactory.createLocalEventLog(LOG_1_2))
        replicator2.replicate(libReplicatorFactory.createLocalEventLog(LOG_2_2))
        replicator3.replicate(libReplicatorFactory.createLocalEventLog(LOG_3_2))

        assertThat(eventLogObserverMock1.getObservedLogs(), containsInAnyOrder(LOG_2_1, LOG_3_1, LOG_2_2, LOG_3_2))
        assertThat(eventLogObserverMock2.getObservedLogs(), containsInAnyOrder(LOG_1_1, LOG_3_1, LOG_1_2, LOG_3_2))
        assertThat(eventLogObserverMock3.getObservedLogs(), containsInAnyOrder(LOG_1_1, LOG_2_1, LOG_1_2, LOG_2_2))
    }
}
