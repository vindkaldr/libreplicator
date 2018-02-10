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
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.crypto.api.CipherException
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.log.api.trace
import org.libreplicator.log.api.warn
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.core.server.ReplicatorServer
import javax.inject.Inject

class DefaultMessageRouter @Inject constructor(
    private val jsonMapper: JsonMapper,
    private val cipher: Cipher,
    private val replicatorClient: ReplicatorClient,
    private val replicatorServer: ReplicatorServer
) : MessageRouter {
    override fun routeMessage(remoteNode: RemoteNode, message: ReplicatorMessage) {
        trace("Routing message..")
        replicatorClient.synchronizeWithNode(remoteNode, cipher.encrypt(jsonMapper.write(message)))
    }

    override suspend fun subscribe(observer: Observer<ReplicatorMessage>): Subscription {
        trace("Subscribing to message router..")

        replicatorClient.initialize()
        val subscription = replicatorServer.subscribe(object : Observer<String> {
            override suspend fun observe(observable: String) {
                try {
                    observer.observe(jsonMapper.read(cipher.decrypt(observable), ReplicatorMessage::class))
                }
                catch (e: CipherException) {
                    warn("Failed to deserialize corrupt message!")
                }
                catch (e: JsonReadException) {
                    warn("Failed to deserialize invalid message!")
                }
            }
        })

        return object : Subscription {
            override suspend fun unsubscribe() {
                trace("Unsubscribing from message router..")

                replicatorClient.close()
                subscription.unsubscribe()
            }
        }
    }
}
