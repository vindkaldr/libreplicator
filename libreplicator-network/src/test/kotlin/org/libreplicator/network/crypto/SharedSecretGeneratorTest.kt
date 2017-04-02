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

package org.libreplicator.network.crypto

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class SharedSecretGeneratorTest {
    private lateinit var secretGenerator: SharedSecretGenerator
    private lateinit var remoteSecretGenerator: SharedSecretGenerator

    @Before
    fun setUp() {
        secretGenerator = SharedSecretGenerator()
        remoteSecretGenerator = SharedSecretGenerator()
    }

    @Test
    fun generatesSharedSecret() {
        val keyPair = secretGenerator.generateKeyPair()
        val remoteKeyPair = remoteSecretGenerator.generateKeyPair()

        val secret = secretGenerator.generateSharedSecret(keyPair, remoteKeyPair.publicKey)
        val remoteSecret = remoteSecretGenerator.generateSharedSecret(remoteKeyPair, keyPair.publicKey)

        assertThat(secret, equalTo(remoteSecret))
    }
}
