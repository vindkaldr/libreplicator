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

package org.libreplicator.core.server

import org.libreplicator.api.LocalNode
import org.libreplicator.api.Observer
import org.libreplicator.api.Subscription
import org.libreplicator.core.locator.api.NodeLocator
import org.libreplicator.core.server.api.ReplicatorServer
import org.libreplicator.httpserver.api.HttpServer
import org.libreplicator.log.api.trace
import javax.inject.Inject

class DefaultReplicatorServer @Inject constructor(
    private val httpServer: HttpServer,
    private val nodeLocator: NodeLocator,
    private val localNode: LocalNode
) : ReplicatorServer {
    override suspend fun subscribe(observer: Observer<String>): Subscription {
        trace("Subscribing to server..")

        httpServer.start(localNode.port, "/sync", observer)
        nodeLocator.acquire()

        return object : Subscription {
            override suspend fun unsubscribe() {
                trace("Unsubscribing from server..")

                httpServer.stop()
                nodeLocator.release()
            }
        }
    }
}
