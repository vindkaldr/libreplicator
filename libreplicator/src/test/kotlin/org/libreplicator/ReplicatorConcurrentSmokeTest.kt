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

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.libreplicator.api.LocalLog
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.testdouble.RemoteEventLogObserverDummy

class ReplicatorConcurrentSmokeTest {
    private companion object {
        private val LOG = "log"
    }

    private val replicatorFactory = ReplicatorTestFactory()

    private val node1 = LocalNode("nodeId1", "localhost", 12345)
    private val node2 = LocalNode("nodeId2", "localhost", 12346)
    private val node3 = LocalNode("nodeId3", "localhost", 12347)

    @Test
    fun replicator_shouldHandleUnavailabilityOfNodes() = runBlocking {
        listOf(
                launch(CommonPool) {
                    testRunReplicator(
                            localNode = node1,
                            remoteNodes = listOf(RemoteNode(node2.nodeId, node2.hostname, node2.port),
                                    RemoteNode(node3.nodeId, node3.hostname, node3.port))) },
                launch(CommonPool) {
                    testRunReplicator(
                            localNode = node2,
                            remoteNodes = listOf(RemoteNode(node1.nodeId, node1.hostname, node1.port),
                                    RemoteNode(node3.nodeId, node3.hostname, node3.port))) },
                launch(CommonPool) {
                    testRunReplicator(
                            localNode = node3,
                            remoteNodes = listOf(RemoteNode(node1.nodeId, node1.hostname, node1.port),
                                    RemoteNode(node2.nodeId, node2.hostname, node2.port))) })
                .forEach { it.join() }
    }

    private suspend fun testRunReplicator(localNode: LocalNode, remoteNodes: List<RemoteNode>) {
        val replicator = replicatorFactory.create(localNode, remoteNodes)

        var subscription = replicator.subscribe(RemoteEventLogObserverDummy())
        replicator.replicate(LocalLog(LOG))
        subscription.unsubscribe()

        subscription = replicator.subscribe(RemoteEventLogObserverDummy())
        replicator.replicate(LocalLog(LOG))
        subscription.unsubscribe()
    }
}
