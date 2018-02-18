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

package org.libreplicator.core.locator

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.api.LocalNode
import org.libreplicator.api.RemoteNode
import org.libreplicator.core.locator.api.NodeLocatorSettings
import org.libreplicator.core.test.withTimeout
import org.libreplicator.core.testdouble.JsonMapperStub

private val localNode = LocalNode("localNodeId", "localAddress", 12345)
private val remoteNode = LocalNode("remoteNodeId", "remoteAddress", 12346)

private val sentLocalNode = RemoteNode(localNode.nodeId, localNode.hostname, localNode.port)
private val sentRemoteNode = RemoteNode(remoteNode.nodeId, remoteNode.hostname, remoteNode.port)

private val addLocalNodeSyncMessage = NodeSyncMessage(addedNodes = listOf(sentLocalNode))
private const val serializedAddLocalNodeSyncMessage = "serializedAddLocalNodeSyncMessage"
private val removeLocalNodeSyncMessage = NodeSyncMessage(removedNodeIds = listOf(sentLocalNode.nodeId))
private const val serializedRemoveLocalNodeSyncMessage = "serializedRemoveLocalNodeSyncMessage"

private val addRemoteNodeSyncMessage = NodeSyncMessage(addedNodes = listOf(sentRemoteNode))
private const val serializedAddRemoteNodeSyncMessage = "serializedAddRemoteNodeSyncMessage"
private val removeRemoteNodeSyncMessage = NodeSyncMessage(removedNodeIds = listOf(sentRemoteNode.nodeId))
private const val serializedRemoveRemoteNodeSyncMessage = "serializedRemoteLocalNodeSyncMessage"

private val jsonMapper = JsonMapperStub(
    addLocalNodeSyncMessage to serializedAddLocalNodeSyncMessage,
    removeLocalNodeSyncMessage to serializedRemoveLocalNodeSyncMessage,
    addRemoteNodeSyncMessage to serializedAddRemoteNodeSyncMessage,
    removeRemoteNodeSyncMessage to serializedRemoveRemoteNodeSyncMessage
)

class DefaultNodeLocatorTest {
    @Test(expected = IllegalStateException::class)
    fun `getNode throws exception when called before acquire`() {
        locator(localNode).getNode(remoteNode.nodeId)
    }

    @Test(expected = IllegalStateException::class)
    fun `release throws exception when called before acquire`() {
        locator(localNode).release()
    }

    @Test(expected = IllegalStateException::class)
    fun `acquire throws exception when called twice`() {
        with(locator(localNode)) {
            acquire()
            acquire()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `release throws exception when called twice`() {
        with(locator(localNode)) {
            acquire()
            release()
            release()
        }
    }

    @Test
    fun `acquire broadcasts joining node in network`() {
        val localLocator = locator(localNode)
        val remoteLocator = locator(remoteNode).acquire()

        localLocator.acquire()

        withTimeout { assertThat(remoteLocator.getNode(localNode.nodeId), equalTo(sentLocalNode)) }

        localLocator.release()
        remoteLocator.release()
    }

    @Test
    fun `release broadcasts leaving node in network`() {
        val localLocator = locator(localNode).acquire()
        val remoteLocator = locator(remoteNode).acquire()
        withTimeout { assertThat(remoteLocator.getNode(localNode.nodeId), equalTo(sentLocalNode)) }

        localLocator.release()

        withTimeout { assertThat(remoteLocator.getNode(localNode.nodeId), nullValue()) }

        remoteLocator.release()
    }

    @Test
    fun `acquire periodically broadcasts joined node in network`() {
        val localLocator = locator(localNode)
        var remoteLocator = locator(remoteNode).acquire()

        localLocator.acquire()

        withTimeout { assertThat(remoteLocator.getNode(localNode.nodeId), equalTo(sentLocalNode)) }
        remoteLocator.release()
        remoteLocator = locator(remoteNode).acquire()
        withTimeout { assertThat(remoteLocator.getNode(localNode.nodeId), equalTo(sentLocalNode)) }

        localLocator.release()
        remoteLocator.release()
    }

    private fun locator(node: LocalNode) = DefaultNodeLocator(node, locatorSettings(), jsonMapper)
    private fun locatorSettings() = NodeLocatorSettings(multicastPeriodInMilliseconds = 100)
}
