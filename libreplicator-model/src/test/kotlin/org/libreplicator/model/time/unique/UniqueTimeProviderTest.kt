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

package org.libreplicator.model.time.unique

import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.model.time.testdouble.DummyTimeProvider
import org.libreplicator.model.time.testdouble.MockTimeProvider

class UniqueTimeProviderTest {
    @Test
    fun getTime_forwardsTime() = runBlocking {
        val timeProvider = UniqueTimeProvider(DummyTimeProvider(timeToReturn = 2L))
        assertThat(timeProvider.getTime(), equalTo(2L))
    }

    @Test
    fun getTime_forwardsTimes_throughoutTime() = runBlocking {
        val timeProvider = UniqueTimeProvider(MockTimeProvider(timesToReturn = longArrayOf(2L, 7L, 9L)))
        assertThat(timeProvider.getTime(), equalTo(2L))
        assertThat(timeProvider.getTime(), equalTo(7L))
        assertThat(timeProvider.getTime(), equalTo(9L))
    }

    @Test
    fun getTime_returnsUniqueTimes() = runBlocking {
        val timeProvider = UniqueTimeProvider(DummyTimeProvider(timeToReturn = 2L))
        assertThat(timeProvider.getTime(), equalTo(2L))
        assertThat(timeProvider.getTime(), equalTo(3L))
        assertThat(timeProvider.getTime(), equalTo(4L))
    }

    @Test
    fun getTime_returnsUniqueTimes_throughoutTime() = runBlocking {
        val timeProvider = UniqueTimeProvider(MockTimeProvider(timesToReturn = longArrayOf(2L, 2L, 7L, 7L, 9L)))
        assertThat(timeProvider.getTime(), equalTo(2L))
        assertThat(timeProvider.getTime(), equalTo(3L))
        assertThat(timeProvider.getTime(), equalTo(7L))
        assertThat(timeProvider.getTime(), equalTo(8L))
        assertThat(timeProvider.getTime(), equalTo(9L))
    }
}
