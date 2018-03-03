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

package org.libreplicator.core.time.testdouble

import org.libreplicator.core.time.api.TimeProvider

fun constantTimeProviderOf(time: Long) = TimeProviderStub(generateSequence { time })
fun timeProviderOf(vararg times: Long) = TimeProviderStub(times.asSequence())
fun timeProviderUntil(lastTime: Long) = TimeProviderStub((1..lastTime).asSequence())

class TimeProviderStub constructor(val times: Sequence<Long>) : TimeProvider {
    private val timesIterator = times.iterator()
    override suspend fun getTime() = timesIterator.next()
}
