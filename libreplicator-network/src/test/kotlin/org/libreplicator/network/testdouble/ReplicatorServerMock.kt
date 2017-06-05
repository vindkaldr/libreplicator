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

package org.libreplicator.network.testdouble

import org.junit.Assert
import org.libreplicator.api.Observer
import org.libreplicator.api.Subscription
import org.libreplicator.model.ReplicatorMessage
import org.libreplicator.server.api.ReplicatorServer

class ReplicatorServerMock constructor(private val subscription: Subscription) : ReplicatorServer {
    private var observedMessageObserver: Observer<ReplicatorMessage>? = null

    override fun subscribe(messageObserver: Observer<ReplicatorMessage>): Subscription {
        if (hasSubscription()) {
            Assert.fail("Unexpected call!")
        }
        observedMessageObserver = messageObserver
        return subscription
    }

    override fun hasSubscription(): Boolean = observedMessageObserver != null

    fun hasBeenSubscribedTo(messageObserver: Observer<ReplicatorMessage>): Boolean {
        return observedMessageObserver == messageObserver
    }
}
