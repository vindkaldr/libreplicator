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
import org.libreplicator.core.router.api.MessageRouter
import org.libreplicator.log.api.trace
import org.libreplicator.core.model.ReplicatorMessage

class TracingMessageRouter(private val messageRouter: MessageRouter) : MessageRouter {
    override fun routeMessage(remoteNode: RemoteNode, message: ReplicatorMessage) {
        trace("Routing message..")
        messageRouter.routeMessage(remoteNode, message)
    }

    override suspend fun subscribe(scope: String, observer: Observer<ReplicatorMessage>): Subscription {
        trace("Subscribing to message router..")
        return messageRouter.subscribe(scope, observer)
    }
}
