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

package org.libreplicator.model.time.testdouble

import org.libreplicator.model.time.TimeProvider

class MockTimeProvider private constructor(val times: List<Long>) : TimeProvider {
    private val timesIterator = times.iterator()

    constructor(timesToReturn: LongArray) : this(timesToReturn.toList())
    constructor(lastTimeToReturn: Long) : this(LongRange(1, lastTimeToReturn).toList())

    override suspend fun getTime() = timesIterator.next()
}
