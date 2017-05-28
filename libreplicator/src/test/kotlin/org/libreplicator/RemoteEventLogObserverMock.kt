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

package org.libreplicator

import org.libreplicator.api.Observer
import org.libreplicator.api.RemoteEventLog
import org.libreplicator.common.test.ObjectObserver

class RemoteEventLogObserverMock private constructor(expectedEventLogCount: Int) : Observer<RemoteEventLog> {
    companion object {
        fun create() = createWithExpectedEventLogCount(0)

        fun createWithExpectedEventLogCount(expectedEventLogCount: Int) =
                RemoteEventLogObserverMock(expectedEventLogCount)
    }

    private val eventLogObserver = ObjectObserver.createWithExpectedObjectCount<RemoteEventLog>(expectedEventLogCount)

    override fun observe(observable: RemoteEventLog) {
        eventLogObserver.observe(observable)
    }

    fun getObservedLogs(): List<String> {
        return eventLogObserver.getObservedObjects().map { it.log }
    }
}