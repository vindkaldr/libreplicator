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

package org.libreplicator.core.router

import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteNode
import org.libreplicator.api.Subscription
import org.libreplicator.core.client.api.ReplicatorClient
import org.libreplicator.core.router.api.MessageRouter
import org.libreplicator.core.server.api.ReplicatorServer
import org.libreplicator.crypto.api.CipherException
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.log.api.warn
import org.libreplicator.core.model.ReplicatorMessage
import javax.inject.Inject

class DefaultMessageRouter @Inject constructor(
    private val jsonMapper: JsonMapper,
    private val replicatorClient: ReplicatorClient,
    private val replicatorServer: ReplicatorServer
) : MessageRouter {
    private val subscribedObservers = mutableMapOf<String, Observer<ReplicatorMessage>>()
    private lateinit var serverSubscription: Subscription

    override fun routeMessage(remoteNode: RemoteNode, message: ReplicatorMessage) {
        replicatorClient.synchronizeWithNode(remoteNode, jsonMapper.write(message))
    }

    override suspend fun subscribe(scope: String, observer: Observer<ReplicatorMessage>): Subscription {
        check(isUnsubscribed(scope))
        storeObserver(scope, observer)
        if (isFirstSubscription()) {
            setUpRouter()
        }
        return MessageRouterSubscription(scope, this)
    }

    private fun isUnsubscribed(scope: String) = !subscribedObservers.containsKey(scope)

    private fun storeObserver(scope: String, observer: Observer<ReplicatorMessage>) {
        subscribedObservers[scope] = observer
    }

    private fun isFirstSubscription() = subscribedObservers.size == 1

    private suspend fun setUpRouter() {
        replicatorClient.initialize()
        serverSubscription = replicatorServer.subscribe(ReplicatorServerObserver(this))
    }

    suspend fun observeMessage(deserializedMessage: String) {
        try {
            notifyObserver(deserializedMessage)
        } catch (e: CipherException) {
            warn("Failed to deserialize corrupt message!")
        } catch (e: JsonReadException) {
            warn("Failed to deserialize invalid message!")
        }
    }

    private suspend fun notifyObserver(deserializedMessage: String) {
        val message = jsonMapper.read(deserializedMessage, ReplicatorMessage::class)
        subscribedObservers[message.groupId]?.observe(message)
    }

    suspend fun unsubscribe(scope: String) {
        check(isSubscribed(scope))
        removeObserver(scope)
        if (isLastSubscription()) {
            tearDownRouter()
        }
    }

    private fun isSubscribed(scope: String) = subscribedObservers.containsKey(scope)

    private fun removeObserver(scope: String) {
        subscribedObservers.remove(scope)
    }

    private fun isLastSubscription() = subscribedObservers.isEmpty()

    private suspend fun tearDownRouter() {
        replicatorClient.close()
        serverSubscription.unsubscribe()
    }
}
