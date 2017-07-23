package org.libreplicator.server.api

import org.libreplicator.api.Observer
import org.libreplicator.api.Subscribable
import org.libreplicator.api.Subscription
import org.libreplicator.model.ReplicatorMessage

interface ReplicatorServer : Subscribable<ReplicatorMessage> {
    override suspend fun subscribe(observer: Observer<ReplicatorMessage>): Subscription
}
