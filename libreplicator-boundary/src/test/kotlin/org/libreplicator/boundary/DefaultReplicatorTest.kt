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

package org.libreplicator.boundary

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test
import org.libreplicator.api.AlreadySubscribedException
import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Subscription
import org.libreplicator.boundary.testdouble.LocalEventLogDummy
import org.libreplicator.boundary.testdouble.NotSubscribedLogDispatcherDummy
import org.libreplicator.boundary.testdouble.NotSubscribedLogDispatcherMock
import org.libreplicator.boundary.testdouble.RemoteEventLogObserverDummy
import org.libreplicator.boundary.testdouble.SubscribedLogDispatcherDummy
import org.libreplicator.boundary.testdouble.SubscribedLogDispatcherMock
import org.libreplicator.boundary.testdouble.SubscriptionDummy

class DefaultReplicatorTest {
    private val localEventLogDummy: LocalEventLog = LocalEventLogDummy()
    private val remoteEventLogObserverDummy = RemoteEventLogObserverDummy()
    private var subscriptionDummy: Subscription = SubscriptionDummy()

    @Ignore
    @Test(expected = NotSubscribedException::class)
    fun replicate_shouldThrowException_whenNotSubscribed() = runBlocking {
        DefaultReplicator(NotSubscribedLogDispatcherDummy()).replicate(localEventLogDummy)
    }

    @Test
    fun replicate_shouldPassLocalEventLogToDispatcher() = runBlocking {
        val subscribedLogDispatcherMock = SubscribedLogDispatcherMock()
        val replicator = DefaultReplicator(subscribedLogDispatcherMock)

        replicator.replicate(localEventLogDummy)

        assertThat(subscribedLogDispatcherMock.getObservedLocalEventLogs(), equalTo(listOf(localEventLogDummy)))
    }

    @Ignore
    @Test(expected = AlreadySubscribedException::class)
    fun subscribe_shouldThrowException_whenAlreadySubscribed() = runBlocking<Unit> {
        DefaultReplicator(SubscribedLogDispatcherDummy()).subscribe(remoteEventLogObserverDummy)
    }

    @Test
    fun subscribe_shouldPassRemoteEventLogObserverToDispatcher_thenReturnSubscription() = runBlocking {
        val notSubscribedLogDispatcherMock = NotSubscribedLogDispatcherMock(remoteEventLogObserverDummy, subscriptionDummy)
        val replicator = DefaultReplicator(notSubscribedLogDispatcherMock)

        val subscription = replicator.subscribe(remoteEventLogObserverDummy)

        assertThat(subscription, equalTo(subscriptionDummy))
    }
}
