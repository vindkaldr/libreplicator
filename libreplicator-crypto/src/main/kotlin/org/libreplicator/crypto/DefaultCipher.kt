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

import org.bouncycastle.crypto.BufferedBlockCipher
import org.bouncycastle.crypto.CipherParameters
import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.util.encoders.DecoderException
import org.bouncycastle.util.encoders.Hex
import org.libreplicator.crypto.api.CipherException
import org.libreplicator.crypto.api.Cipher

class DefaultCipher(private val sharedSecret: String) : Cipher {
    private companion object {
        private val SALT = ByteArray(0)
        private val ITERATION_COUNT = 1024

        private val KEY_SIZE = 256
        private val INITIALIZATION_VECTOR_SIZE = 128

        private val ENCRYPTION = true
        private val DECRYPTION = false

        private val ZEROTH_POSITION = 0
    }

    override fun encrypt(content: String): String {
        val cipherParameters = getCipherParameters()
        val cipher = getCipher(ENCRYPTION, cipherParameters)

        return String(Hex.encode(cipherContent(cipher, content.toByteArray())))
    }

    override fun decrypt(encryptedContent: String): String {
        try {
            val cipherParameters = getCipherParameters()
            val cipher = getCipher(DECRYPTION, cipherParameters)

            return String(cipherContent(cipher, Hex.decode(encryptedContent.toByteArray())))
        }
        catch (e: DecoderException) {
            throw CipherException("Invalid content!")
        }
        catch (e: InvalidCipherTextException) {
            throw CipherException("Invalid secret!")
        }
    }

    private fun getCipherParameters(): CipherParameters {
        val parametersGenerator = PKCS5S2ParametersGenerator()
        parametersGenerator.init(sharedSecret.toByteArray(), SALT, ITERATION_COUNT)

        return parametersGenerator.generateDerivedParameters(KEY_SIZE, INITIALIZATION_VECTOR_SIZE)
    }

    private fun getCipher(isEncryption: Boolean, cipherParameters: CipherParameters): BufferedBlockCipher {
        val cipher = getCipher()
        cipher.init(isEncryption, cipherParameters)
        return cipher
    }

    private fun getCipher(): BufferedBlockCipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine()))

    private fun cipherContent(cipher: BufferedBlockCipher, content: ByteArray): ByteArray {
        val buffer = ByteArray(cipher.getOutputSize(content.size))

        val numberOfCopiedBytes = cipher.processBytes(content, ZEROTH_POSITION, content.size, buffer, ZEROTH_POSITION)
        val nextNumberOfCopiedBytes = cipher.doFinal(buffer, numberOfCopiedBytes)

        return buffer.copyOf(numberOfCopiedBytes + nextNumberOfCopiedBytes)
    }
}
