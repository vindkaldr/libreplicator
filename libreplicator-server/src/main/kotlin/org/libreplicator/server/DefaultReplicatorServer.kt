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

package org.libreplicator.server

import org.libreplicator.api.LocalNode
import org.libreplicator.api.Observer
import org.libreplicator.api.Subscription
import org.libreplicator.gateway.api.InternetGateway
import org.libreplicator.gateway.api.model.AddPortMapping
import org.libreplicator.gateway.api.model.DeletePortMapping
import org.libreplicator.gateway.api.model.InternetProtocol
import org.libreplicator.httpserver.api.HttpServer
import org.libreplicator.locator.api.NodeLocator
import org.libreplicator.log.api.trace
import org.libreplicator.server.api.ReplicatorServer
import javax.inject.Inject

const val LIBREPLICATOR_DESCRIPTION = "libreplicator"

class DefaultReplicatorServer @Inject constructor(
        private val httpServer: HttpServer,
        private val internetGateway: InternetGateway,
        private val nodeLocator: NodeLocator,
        private val localNode: LocalNode
) : ReplicatorServer {
    override suspend fun subscribe(observer: Observer<String>): Subscription {
        trace("Subscribing to server..")

        httpServer.start(localNode.port, "/sync", observer)
        internetGateway.addPortMapping(AddPortMapping(localNode.port, InternetProtocol.TCP, localNode.port, LIBREPLICATOR_DESCRIPTION))
        nodeLocator.addNode(localNode)

        return object : Subscription {
            override suspend fun unsubscribe() {
                trace("Unsubscribing from server..")

                httpServer.stop()
                internetGateway.deletePortMapping(DeletePortMapping(localNode.port, InternetProtocol.TCP))
                nodeLocator.removeNode(localNode.nodeId)
            }
        }
    }
}
