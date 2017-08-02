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

package org.libreplicator.model.time

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.model.time.testdouble.DummyTimeProvider
import org.libreplicator.model.time.testdouble.MockTimeProvider

class TimeProviderInteractorTest {
    @Test
    fun getTime_forwardsTime() = runBlocking {
        val interactor = TimeProviderInteractor(DummyTimeProvider(timeToReturn = 2L))
        assertThat(interactor.getTime(), equalTo(2L))
    }

    @Test
    fun getTime_forwardsTimes_throughoutTime() = runBlocking {
        val interactor = TimeProviderInteractor(MockTimeProvider(timesToReturn = longArrayOf(2L, 7L, 9L)))
        assertThat(interactor.getTime(), equalTo(2L))
        assertThat(interactor.getTime(), equalTo(7L))
        assertThat(interactor.getTime(), equalTo(9L))
    }

    @Test
    fun getTime_isThreadSafe() = runBlocking {
        val timeProvider = MockTimeProvider(lastTimeToReturn = 250_000L)
        val interactor = TimeProviderInteractor(timeProvider)

        val returnedTimes = timeProvider.times
                .map { async(CommonPool) { interactor.getTime() } }
                .map { it.await() }
                .toSet()

        assertThat(returnedTimes.size, equalTo(timeProvider.times.size))
    }
}
