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

package org.libreplicator.core.time

import com.natpryce.hamkrest.assertion.assert
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.libreplicator.core.time.testdouble.timeProviderOf
import org.libreplicator.core.time.testdouble.timeProviderUntil

object SynchronizedTimeProviderSpec : Spek({
    given("a synchronized time provider") {
        on("getting time") {
            it("forwards time") { runBlocking {
                SynchronizedTimeProvider(timeProviderOf(2L)).apply {
                    assert.that(getTime(), equalTo(2L))
                }
            }}
        }
        on("getting times") {
            it("forwards times") { runBlocking {
                SynchronizedTimeProvider(timeProviderOf(2L, 7L, 9L)).apply {
                    assert.that(getTime(), equalTo(2L))
                    assert.that(getTime(), equalTo(7L))
                    assert.that(getTime(), equalTo(9L))
                }
            }}
        }
        on("getting times in a concurrent environment") {
            it("forwards times safely") { runBlocking {
                SynchronizedTimeProvider(timeProviderUntil(250_000L)).apply {
                    assert.that(timesUntil(250_000L).toSet(), equalTo((1..250_000L).toSet()))
                }
            }}
        }
    }
})

private suspend fun SynchronizedTimeProvider.timesUntil(lastTime: Long): Iterable<Long> {
    return (1..lastTime).map { async { getTime() } }
            .map { it.await() }
}
