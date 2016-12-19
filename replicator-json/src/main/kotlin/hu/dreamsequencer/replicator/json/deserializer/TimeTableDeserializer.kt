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

package hu.dreamsequencer.replicator.json.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import hu.dreamsequencer.replicator.json.serializer.TimeTableSerializer
import hu.dreamsequencer.replicator.model.TimeTable

class TimeTableDeserializer : StdDeserializer<TimeTable>(TimeTable::class.java) {

    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): TimeTable {
        val node = parser?.readValueAsTree<JsonNode>()
        val timeTable = TimeTable()

        if (node != null && node.isArray) {
            node.forEach { innerNode ->
                val row = getString(innerNode, TimeTableSerializer.PROPERTY_ROW)
                val column = getString(innerNode, TimeTableSerializer.PROPERTY_COLUMN)
                val value = getLong(innerNode, TimeTableSerializer.PROPERTY_VALUE)

                timeTable[row, column] = value
            }
            return timeTable
        }
        throw ExpectedJsonArrayException("Not a json array! $node")
    }

    private fun getString(node: JsonNode, fieldName: String): String {
        val innerNode = getNode(node, fieldName)
        if (innerNode.isTextual) {
            return innerNode.textValue()
        }
        throw ExpectedStringException("Not a string! $innerNode")
    }

    private fun getLong(node: JsonNode, fieldName: String): Long {
        val innerNode = getNode(node, fieldName)
        if (innerNode.canConvertToLong()) {
            return innerNode.longValue()
        }
        throw ExpectedLongException("Not an integer! $innerNode")
    }

    private fun getNode(node: JsonNode, fieldName: String): JsonNode {
        if (!node.has(fieldName)) {
            throw ExpectedPropertyException("Expected property not found! $fieldName in $node")
        }
        return node.get(fieldName)
    }
}
