package hu.dreamsequencer.replicator.boundary

import hu.dreamsequencer.replicator.api.RemoteEventLogObserver
import hu.dreamsequencer.replicator.api.Replicator
import hu.dreamsequencer.replicator.api.ReplicatorFactory
import hu.dreamsequencer.replicator.api.ReplicatorNode
import hu.dreamsequencer.replicator.interactor.api.LogDispatcherFactory
import javax.inject.Inject
import javax.inject.Provider

class DefaultReplicatorFactory
@Inject constructor(private val logDispatcherFactoryProvider: Provider<LogDispatcherFactory>) : ReplicatorFactory {
    override fun create(localNode: ReplicatorNode,
                        remoteNodes: List<ReplicatorNode>,
                        remoteEventLogObserver: RemoteEventLogObserver): Replicator =
            DefaultReplicator(logDispatcherFactoryProvider.get(), localNode, remoteNodes, remoteEventLogObserver)
}
