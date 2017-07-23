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

import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.Replicator
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.gateway.module.LibReplicatorGatewayModule
import org.libreplicator.locator.api.NodeLocator
import org.libreplicator.locator.module.LibReplicatorLocatorModule
import org.libreplicator.model.factory.LocalEventLogFactory
import org.libreplicator.model.factory.ReplicatorNodeFactory
import org.libreplicator.testdouble.InternetGatewayDummy
import org.libreplicator.testdouble.NodeLocatorFake

class LibReplicatorTestFactory(
        private val settings: LibReplicatorSettings = LibReplicatorSettings(),
        private val nodeLocator: NodeLocator = NodeLocatorFake()
) {
    fun createReplicator(localNode: ReplicatorNode, remoteNodes: List<ReplicatorNode>): Replicator {
        val component = LibReplicatorComponentBuilder().build(settings, localNode, remoteNodes) {
            libReplicatorGatewayModule(LibReplicatorGatewayModule(InternetGatewayDummy()))
            libReplicatorLocatorModule(LibReplicatorLocatorModule(nodeLocator))
        }
        return component.getReplicator()
    }

    fun createLocalEventLog(log: String): LocalEventLog {
        return LocalEventLogFactory().create(log)
    }

    fun createReplicatorNode(nodeId: String, url: String, port: Int): ReplicatorNode {
        return ReplicatorNodeFactory().create(nodeId, url, port)
    }
}
