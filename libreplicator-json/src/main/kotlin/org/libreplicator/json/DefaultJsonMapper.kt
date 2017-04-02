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

package org.libreplicator.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonMixin
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.json.api.JsonWriteException
import javax.inject.Inject
import kotlin.reflect.KClass

class DefaultJsonMapper @Inject constructor(private val jsonMixins: Set<JsonMixin>) : JsonMapper {
    private val objectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .apply {
                jsonMixins.forEach { addMixIn(it.targetClass.java, it.sourceClass.java) }
            }

    override fun write(any: Any): String {
        try {
            return objectMapper.writeValueAsString(any)
        }
        catch (throwable: Throwable) {
            throw JsonWriteException(throwable)
        }
    }

    override fun <T: Any> read(string: String, kotlinType: KClass<T>): T {
        try {
            return objectMapper.readValue(string, kotlinType.javaObjectType)
        }
        catch (throwable: Throwable) {
            throw JsonReadException(throwable)
        }
    }
}
