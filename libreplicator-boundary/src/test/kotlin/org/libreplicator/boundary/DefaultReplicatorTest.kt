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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.libreplicator.api.AlreadySubscribedException
import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.NotSubscribedException
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.api.Replicator
import org.libreplicator.api.Subscription
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.api.LogDispatcherFactory
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DefaultReplicatorTest {
    @Mock private lateinit var mockLogDispatcherFactory: LogDispatcherFactory
    @Mock private lateinit var mockLogDispatcher: LogDispatcher

    @Mock private lateinit var mockLocalEventLog: LocalEventLog
    @Mock private lateinit var mockEventLogObserver: Observer<RemoteEventLog>
    @Mock private lateinit var mockSubscription: Subscription

    private lateinit var replicator: Replicator

    @Before
    fun setUp() {
        replicator = DefaultReplicator(mockLogDispatcher)
    }

    @Test(expected = NotSubscribedException::class)
    fun replicate_shouldThrowException_whenNotSubscribed() {
        replicator.replicate(mockLocalEventLog)
    }

    @Test
    fun replicate_shouldPassEventLogToDispatcher() {
        whenever(mockLogDispatcher.hasSubscription()).thenReturn(true)

        replicator.replicate(mockLocalEventLog)

        verify(mockLogDispatcher).dispatch(mockLocalEventLog)
    }

    @Test(expected = AlreadySubscribedException::class)
    fun subscribe_shouldThrowException_whenAlreadySubscribed() {
        whenever(mockLogDispatcher.hasSubscription()).thenReturn(true)

        replicator.subscribe(mock())
    }

    @Test
    fun subscribe_shouldPassEventLogObserverToDispatcher_thenReturnSubscription() {
        whenever(mockLogDispatcher.subscribe(mockEventLogObserver)).thenReturn(mockSubscription)

        val subscription = replicator.subscribe(mockEventLogObserver)

        verify(mockLogDispatcher).subscribe(mockEventLogObserver)
        assertThat(subscription, equalTo(mockSubscription))
    }
}
