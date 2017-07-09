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

package org.libreplicator.server

import org.libreplicator.api.AlreadySubscribedException
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Observer
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.httpserver.api.HttpServer
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.server.api.ReplicatorServer
import javax.inject.Inject

class DefaultReplicatorServer @Inject constructor(
        private val jsonMapper: JsonMapper,
        private val cipher: Cipher,
        private val httpServer: HttpServer,
        private val localNode: ReplicatorNode) : ReplicatorServer {

    private var hasSubscription = false

    override fun subscribe(messageObserver: Observer<ReplicatorMessage>): Subscription {
        if (hasSubscription) {
            throw AlreadySubscribedException()
        }
        httpServer.startAndWaitUntilStarted(localNode.port, "/sync", ReplicatorSyncServlet(jsonMapper, cipher, messageObserver))
        hasSubscription = true

        return object : Subscription {
            override fun unsubscribe() = synchronized(this) {
                if (!hasSubscription) {
                    throw NotSubscribedException()
                }
                httpServer.stopAndWaitUntilStopped()
                hasSubscription = false
            }
        }
    }

    override fun hasSubscription(): Boolean = hasSubscription
}