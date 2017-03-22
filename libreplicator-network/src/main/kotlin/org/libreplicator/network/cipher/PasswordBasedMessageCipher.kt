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

package org.libreplicator.network.cipher

import org.bouncycastle.crypto.PBEParametersGenerator
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.util.encoders.Hex
import java.nio.ByteBuffer

class PasswordBasedMessageCipher {
    fun encrypt(password: ByteArray, message: String): String {
        val pbeDerivedKeyGenerator = PKCS5S2ParametersGenerator()
        val pkcs5Password = PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(Charsets.UTF_8.decode(ByteBuffer.wrap(password)).array())
        pbeDerivedKeyGenerator.init(pkcs5Password, ByteArray(0), 1024)

        val cipherParameters = pbeDerivedKeyGenerator.generateDerivedParameters(256, 128)

        val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine()))
        cipher.init(true, cipherParameters)

        return String(Hex.encode(cipherData(cipher, message.toByteArray())))
    }

    fun decrypt(password: ByteArray, encryptedMessage: String): String {
        try {
            val pbeDerivedKeyGenerator = PKCS5S2ParametersGenerator()
            val pkcs5Password = PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(Charsets.UTF_8.decode(ByteBuffer.wrap(password)).array())
            pbeDerivedKeyGenerator.init(pkcs5Password, ByteArray(0), 1024)

            val cipherParameters = pbeDerivedKeyGenerator.generateDerivedParameters(256, 128)

            val cipher = PaddedBufferedBlockCipher(CBCBlockCipher(AESEngine()))
            cipher.init(false, cipherParameters)

            return String(cipherData(cipher, Hex.decode(encryptedMessage.toByteArray())))
        }
        catch (throwable: Throwable) {
            throw CipherException()
        }
    }

    private fun cipherData(cipher: PaddedBufferedBlockCipher, data: ByteArray): ByteArray {
        val outputBuffer = ByteArray(cipher.getOutputSize(data.size))

        val length1 = cipher.processBytes(data, 0, data.size, outputBuffer, 0)
        val length2 = cipher.doFinal(outputBuffer, length1)

        val result = ByteArray(length1 + length2)

        System.arraycopy(outputBuffer, 0, result, 0, result.size)

        return result
    }
}
