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

package org.libreplicator.boundary.testdouble

import org.libreplicator.api.LocalEventLog
import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.api.Subscription
import org.libreplicator.common.test.ObjectObserver
import org.libreplicator.interactor.api.LogDispatcher

class SubscribedLogDispatcherMock constructor(numberOfExpectedLocalEventLogs: Int = 0): LogDispatcher {
    private val objectObserver: ObjectObserver<LocalEventLog> =
            ObjectObserver(numberOfExpectedObjects = numberOfExpectedLocalEventLogs)

    override suspend fun dispatch(localEventLog: LocalEventLog) {
        objectObserver.observe(localEventLog)
    }

    override suspend fun subscribe(observer: Observer<RemoteEventLog>): Subscription {
        return SubscriptionDummy()
    }

    override fun hasSubscription(): Boolean = true

    fun getObservedLocalEventLogs(): List<LocalEventLog> {
        return objectObserver.getObservedObjects()
    }
}
