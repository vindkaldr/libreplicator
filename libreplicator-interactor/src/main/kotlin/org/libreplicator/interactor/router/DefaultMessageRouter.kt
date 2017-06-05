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

package org.libreplicator.interactor.router

import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Observer
import org.libreplicator.api.ReplicatorNode
import org.libreplicator.api.Subscription
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.server.api.ReplicatorServer
import javax.inject.Inject
import javax.inject.Provider

class DefaultMessageRouter @Inject constructor(
        private val replicatorClientProvider: Provider<ReplicatorClient>,
        private val replicatorServerProvider: Provider<ReplicatorServer>) : MessageRouter {

    private lateinit var replicatorClient: ReplicatorClient
    private var replicatorServer = replicatorServerProvider.get()

    override fun routeMessage(remoteNode: ReplicatorNode, message: ReplicatorMessage) = synchronized(this) {
        if (!hasSubscription()) {
            throw NotSubscribedException()
        }
        replicatorClient.synchronizeWithNode(remoteNode, message)
    }

    override fun subscribe(messageObserver: Observer<ReplicatorMessage>): Subscription = synchronized(this) {
        replicatorClient = replicatorClientProvider.get()
        replicatorServer = replicatorServerProvider.get()

        val subscription = replicatorServer.subscribe(messageObserver)

        return object : Subscription {
            override fun unsubscribe() = synchronized(this) {
                replicatorClient.close()
                subscription.unsubscribe()
            }
        }
    }

    override fun hasSubscription(): Boolean = replicatorServer.hasSubscription()
}
