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

package org.libreplicator.common.test

import org.junit.Assert
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class ObjectObserver<T> private constructor(private val expectedObjectCount: Int) {
    companion object {
        private val TIMEOUT_IN_SECONDS = 3L

        fun <T> createWithExpectedObjectCount(expectedObjectCount: Int): ObjectObserver<T> =
                ObjectObserver(expectedObjectCount)
    }

    private val observedObjects: MutableList<T> = mutableListOf()
    private val semaphore = Semaphore(0)

    fun observe(observable: T) {
        observedObjects.add(observable)
        semaphore.release()
    }

    fun getObservedObjects(): List<T> {
        if (!semaphore.tryAcquire(expectedObjectCount, TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
            Assert.fail("Timeout reached!")
        }
        return observedObjects
    }
}
