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

package hu.dreamsequencer.replicator.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import hu.dreamsequencer.replicator.json.api.JsonMapper
import hu.dreamsequencer.replicator.json.api.JsonReadException
import hu.dreamsequencer.replicator.json.api.JsonWriteException
import hu.dreamsequencer.replicator.json.deserializer.TimeTableDeserializer
import hu.dreamsequencer.replicator.json.mixin.EventLogMixin
import hu.dreamsequencer.replicator.json.mixin.ReplicatorMessageMixin
import hu.dreamsequencer.replicator.json.serializer.TimeTableSerializer
import hu.dreamsequencer.replicator.model.EventLog
import hu.dreamsequencer.replicator.model.ReplicatorMessage
import hu.dreamsequencer.replicator.model.TimeTable
import javax.inject.Inject
import kotlin.reflect.KClass

class DefaultJsonMapper @Inject constructor() : JsonMapper {
    private val objectMapper = ObjectMapper()
            .registerModule(
                    SimpleModule()
                            .addSerializer(TimeTableSerializer())
                            .addDeserializer(TimeTable::class.java, TimeTableDeserializer()))
            .addMixIn(ReplicatorMessage::class.java, ReplicatorMessageMixin::class.java)
            .addMixIn(EventLog::class.java, EventLogMixin::class.java)

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
