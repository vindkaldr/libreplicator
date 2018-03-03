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

package org.libreplicator.core.model

import java.lang.Math.max

data class TimeTable(private val table: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()) {
    operator fun get(sourceNodeId: String, targetNodeId: String): Long {
        return table.getOrElse(sourceNodeId, { mapOf<String, Long>() })
                .getOrElse(targetNodeId, { 0 })
    }

    operator fun set(sourceNodeId: String, targetNodeId: String, time: Long) {
        if (time > 0) {
            table.getOrPut(sourceNodeId, { mutableMapOf() })[targetNodeId] = time
        }
    }

    fun merge(sourceNodeId: String, payload: ReplicatorPayload) {
        mergeRow(sourceNodeId, payload)
        merge(payload)
    }

    private fun mergeRow(sourceNodeId: String, payload: ReplicatorPayload) {
        val sourceRow = table.getOrPut(sourceNodeId, { mutableMapOf() })
        val targetRow = payload.timeTable.table.getOrPut(payload.nodeId, { mutableMapOf() })

        sourceRow.keys + targetRow.keys.forEach {
            val maxValue = max(sourceRow.getOrElse(it, { 0 }), targetRow.getOrElse(it, { 0 }))
            sourceRow[it] = maxValue
        }
    }

    private fun merge(payload: ReplicatorPayload) {
        val rowKeys2 = table.keys + payload.timeTable.table.keys
        val columnKeys2 = (table.values.map { it.keys } + payload.timeTable.table.values.map { it.keys }).flatten()

        rowKeys2.forEach { rowKey ->
            columnKeys2.forEach { columnKey ->
                val maxValue = max(get(rowKey, columnKey), payload.timeTable[rowKey, columnKey])
                set(rowKey, columnKey, maxValue)
            }
        }
    }

    // Do not change or call. It's here for serialization/deserialization purposes.
    private fun getTable() = table
}
