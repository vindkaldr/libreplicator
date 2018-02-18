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

package org.libreplicator.core.test

import org.junit.Assert
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun withTimeout(assertions: () -> Unit) {
    with(Semaphore(0)) {
        thread {
            while (!allAssertionPassed(assertions)) Thread.sleep(10)
            release()
        }
        if (!tryAcquire(1, TimeUnit.SECONDS)) Assert.fail()
    }
}

private fun allAssertionPassed(assertions: () -> Unit): Boolean {
    return try {
        assertions()
        true
    } catch (e: AssertionError) {
        false
    }
}
