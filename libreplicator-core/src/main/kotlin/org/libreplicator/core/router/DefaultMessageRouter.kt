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
import org.libreplicator.core.client.ReplicatorClient
import org.libreplicator.core.server.ReplicatorServer
import org.libreplicator.crypto.api.CipherException
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.log.api.trace
import org.libreplicator.log.api.warn
import org.libreplicator.model.ReplicatorMessage
import javax.inject.Inject

class DefaultMessageRouter @Inject constructor(
    private val jsonMapper: JsonMapper,
    private val replicatorClient: ReplicatorClient,
    private val replicatorServer: ReplicatorServer
) : MessageRouter {
    private val subscriptions = mutableMapOf<String, Observer<ReplicatorMessage>>()
    private lateinit var serverSubscription: Subscription

    override fun routeMessage(remoteNode: RemoteNode, message: ReplicatorMessage) {
        trace("Routing message..")
        replicatorClient.synchronizeWithNode(remoteNode, jsonMapper.write(message))
    }

    override suspend fun subscribe(topic: String, observer: Observer<ReplicatorMessage>): Subscription {
        trace("Subscribing to message router..")

        if (subscriptions.containsKey(topic)) {
            throw AlreadySubscribedException()
        }
        subscriptions[topic] = observer

        if (subscriptions.size == 1) {
            replicatorClient.initialize()
            serverSubscription = replicatorServer.subscribe(object : Observer<String> {
                override suspend fun observe(observable: String) {
                    try {
                        val message = jsonMapper.read(observable, ReplicatorMessage::class)
                        subscriptions[message.groupId]?.observe(message)
                    }
                    catch (e: CipherException) {
                        warn("Failed to deserialize corrupt message!")
                    }
                    catch (e: JsonReadException) {
                        warn("Failed to deserialize invalid message!")
                    }
                }
            })
        }

        return object : Subscription {
            private var isUnsubscribed = false

            override suspend fun unsubscribe() {
                if (isUnsubscribed) throw NotSubscribedException()

                trace("Unsubscribing from message router..")

                subscriptions.remove(topic)
                if (subscriptions.isEmpty()) {
                    replicatorClient.close()
                    serverSubscription.unsubscribe()
                }
                isUnsubscribed = true
            }
        }
    }
}
