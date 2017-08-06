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

import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteNode
import org.libreplicator.api.Subscription
import org.libreplicator.client.api.ReplicatorClient
import org.libreplicator.crypto.api.Cipher
import org.libreplicator.crypto.api.CipherException
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.server.api.ReplicatorServer
import org.slf4j.LoggerFactory
import javax.inject.Inject

class DefaultMessageRouter @Inject constructor(
        private val jsonMapper: JsonMapper,
        private val cipher: Cipher,
        private val replicatorClient: ReplicatorClient,
        private val replicatorServer: ReplicatorServer
) : MessageRouter {
    private companion object {
        private val logger = LoggerFactory.getLogger(DefaultMessageRouter::class.java)
    }

    override fun routeMessage(remoteNode: RemoteNode, message: ReplicatorMessage) {
        logger.trace("Routing message..")
        replicatorClient.synchronizeWithNode(remoteNode, cipher.encrypt(jsonMapper.write(message)))
    }

    override suspend fun subscribe(observer: Observer<ReplicatorMessage>): Subscription {
        logger.trace("Subscribing to message router..")

        replicatorClient.initialize()
        val subscription = replicatorServer.subscribe(object : Observer<String> {
            suspend override fun observe(observable: String) {
                try {
                    observer.observe(jsonMapper.read(cipher.decrypt(observable), ReplicatorMessage::class))
                }
                catch (e: CipherException) {
                    logger.warn("Failed to deserialize corrupt message!")
                }
                catch (e: JsonReadException) {
                    logger.warn("Failed to deserialize invalid message!")
                }
            }
        })

        return object : Subscription {
            override suspend fun unsubscribe() {
                logger.trace("Unsubscribing from message router..")

                replicatorClient.close()
                subscription.unsubscribe()
            }
        }
    }
}
