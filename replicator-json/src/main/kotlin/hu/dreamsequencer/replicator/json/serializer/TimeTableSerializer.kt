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

package hu.dreamsequencer.replicator.json.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.common.collect.Table
import hu.dreamsequencer.replicator.model.TimeTable

internal class TimeTableSerializer : StdSerializer<TimeTable>(TimeTable::class.java) {

    companion object {
        val PROPERTY_ROW = "rowKey"
        val PROPERTY_COLUMN = "columnKey"
        val PROPERTY_VALUE = "value"
    }

    override fun serialize(timeTable: TimeTable?, generator: JsonGenerator?, provider: SerializerProvider?) {
        generator?.writeStartArray()

        timeTable?.copy()?.cellSet()?.filter { cell -> getCellValue(cell) > 0 }
                ?.forEach { cell ->
                    generator?.writeStartObject()

                    generator?.writeStringField(PROPERTY_ROW, cell.rowKey)
                    generator?.writeStringField(PROPERTY_COLUMN, cell.columnKey)
                    generator?.writeNumberField(PROPERTY_VALUE, getCellValue(cell))

                    generator?.writeEndObject()
                }

        generator?.writeEndArray()
    }

    private fun getCellValue(cell: Table.Cell<String, String, Long>) = cell.value ?: 0L
}
