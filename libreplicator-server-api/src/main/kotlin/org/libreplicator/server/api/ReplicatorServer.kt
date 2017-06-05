package org.libreplicator.server.api

import org.libreplicator.api.Observer
import org.libreplicator.api.Subscription
import org.libreplicator.model.ReplicatorMessage

interface ReplicatorServer {
    fun subscribe(messageObserver: Observer<ReplicatorMessage>): Subscription
    fun hasSubscription(): Boolean
}
