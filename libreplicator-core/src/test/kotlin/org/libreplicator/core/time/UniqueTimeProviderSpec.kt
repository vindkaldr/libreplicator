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
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.libreplicator.core.time.testdouble.constantTimeProviderOf
import org.libreplicator.core.time.testdouble.timeProviderOf

object UniqueTimeProviderSpec : Spek({
    given("a unique time provider") {
        given("a wrapped constant time provider") {
            on("getting constant time") {
                it("forwards constant time") { runBlocking {
                    UniqueTimeProvider(constantTimeProviderOf(2L)).apply {
                        assert.that(getTime(), equalTo(2L))
                    }
                }}
            }
            on("getting constant times") {
                it("returns unique times") { runBlocking {
                    UniqueTimeProvider(constantTimeProviderOf(2L)).apply {
                        assert.that(getTime(), equalTo(2L))
                        assert.that(getTime(), equalTo(3L))
                        assert.that(getTime(), equalTo(4L))
                    }
                }}
            }
        }
        given("a wrapped times provider") {
            on("getting unique times") {
                it("forwards unique times") { runBlocking {
                    UniqueTimeProvider(timeProviderOf(2L, 7L, 9L)).apply {
                        assert.that(getTime(), equalTo(2L))
                        assert.that(getTime(), equalTo(7L))
                        assert.that(getTime(), equalTo(9L))
                    }
                }}
            }
            on("getting not unique times") {
                it("returns unique times") { runBlocking {
                    UniqueTimeProvider(timeProviderOf(2L, 2L, 7L, 7L, 9L)).apply {
                        assert.that(getTime(), equalTo(2L))
                        assert.that(getTime(), equalTo(3L))
                        assert.that(getTime(), equalTo(7L))
                        assert.that(getTime(), equalTo(8L))
                        assert.that(getTime(), equalTo(9L))
                    }
                }}
            }
        }
    }
})
