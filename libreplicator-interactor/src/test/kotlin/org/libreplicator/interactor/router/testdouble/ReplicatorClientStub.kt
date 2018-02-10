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

package org.libreplicator.interactor.router.testdouble

import org.junit.Assert.fail
import org.libreplicator.api.RemoteNode
import org.libreplicator.interactor.client.ReplicatorClient

class ReplicatorClientStub : ReplicatorClient {
    private var observedInitialize: Boolean = false

    private var observedRemoteNode: RemoteNode? = null
    private var observedMessage: String? = null

    private var observedClose: Boolean = false

    override fun initialize() {
        if (observedInitialize) {
            fail("Unexpected call!")
        }
        observedInitialize = true
    }

    override fun synchronizeWithNode(remoteNode: RemoteNode, message: String) {
        if (observedRemoteNode != null && observedMessage != null) {
            fail("Unexpected call!")
        }
        observedRemoteNode = remoteNode
        observedMessage = message
    }

    override fun close() {
        if (observedClose) {
            fail("Unexpected call!")
        }
        observedClose = true
    }

    fun wasInitialized(): Boolean {
        return observedInitialize
    }

    fun sentMessage(remoteNode: RemoteNode, message: String): Boolean {
        return observedRemoteNode == remoteNode && observedMessage == message
    }

    fun wasClosed(): Boolean {
        return observedClose
    }
}
