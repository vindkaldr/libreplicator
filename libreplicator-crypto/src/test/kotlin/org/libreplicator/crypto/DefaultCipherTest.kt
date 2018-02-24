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

package org.libreplicator.crypto

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.assertThat
import org.junit.Test
import org.libreplicator.crypto.api.CipherException
import org.libreplicator.crypto.api.Cipher

private const val SHARED_SECRET = "sharedSecret"
private const val CORRUPTED_SHARED_SECRET = "corruptedSharedSecret"

private const val CONTENT = "content"
private const val ENCRYPTED_CONTENT = "3e6870c7402fd311a09b8cbcdb9b03c2"
private const val CORRUPTED_ENCRYPTED_CONTENT = "corruptedEncryptedContent"

class DefaultCipherTest {
    @Test
    fun encrypt_encryptsContent() {
        val cipher: Cipher = DefaultCipher(SHARED_SECRET)
        assertThat(cipher.encrypt(CONTENT), not(equalTo(CONTENT)))
    }

    @Test
    fun decrypt_decryptsContent() {
        val cipher: Cipher = DefaultCipher(SHARED_SECRET)
        assertThat(cipher.decrypt(ENCRYPTED_CONTENT), not(equalTo(ENCRYPTED_CONTENT)))
    }

    @Test
    fun cipher_encryptsThenDecryptsContent_withSameSharedSecret() {
        val cipher: Cipher = DefaultCipher(SHARED_SECRET)

        val encryptedContent = cipher.encrypt(CONTENT)
        val decryptedContent = cipher.decrypt(encryptedContent)

        assertThat(decryptedContent, equalTo(CONTENT))
    }

    @Test(expected = CipherException::class)
    fun decrypt_throwsException_forCorruptedSharedSecret() {
        val cipher: Cipher = DefaultCipher(CORRUPTED_SHARED_SECRET)
        cipher.decrypt(ENCRYPTED_CONTENT)
    }

    @Test(expected = CipherException::class)
    fun decrypt_throwsException_forCorruptedEncryptedContent() {
        val cipher: Cipher = DefaultCipher(SHARED_SECRET)
        cipher.decrypt(CORRUPTED_ENCRYPTED_CONTENT)
    }
}
