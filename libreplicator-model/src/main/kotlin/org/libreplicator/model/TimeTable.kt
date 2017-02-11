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

package org.libreplicator.model

import java.lang.Math.max

data class TimeTable(private val table: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()) {
    operator fun get(sourceNodeId: String, targetNodeId: String): Long = synchronized(this) {
        return table.getOrElse(sourceNodeId, { mapOf<String, Long>() })
                .getOrElse(targetNodeId, { 0 })
    }

    operator fun set(sourceNodeId: String, targetNodeId: String, time: Long) = synchronized(this) {
        if (time > 0) {
            table.getOrPut(sourceNodeId, { mutableMapOf() }).put(targetNodeId, time)
        }
    }

    fun mergeRow(sourceNodeId: String, timeTable: TimeTable, targetNodeId: String) = synchronized(this) {
        val sourceRow = table.getOrElse(sourceNodeId, { mutableMapOf() })
        val targetRow = timeTable.table.getOrElse(targetNodeId, { mutableMapOf() })

        sourceRow.keys + targetRow.keys.forEach {
            val maxValue = max(sourceRow.getOrElse(it, { 0 }), targetRow.getOrElse(it, { 0 }))
            sourceRow.put(it, maxValue)
        }
    }

    fun merge(timeTable: TimeTable) = synchronized(this) {
        val rowKeys2 = table.keys + timeTable.table.keys
        val columnKeys2 = (table.values.map { it.keys } + timeTable.table.values.map { it.keys }).flatten()

        rowKeys2.forEach { rowKey ->
            columnKeys2.forEach { columnKey ->
                val maxValue = Math.max(get(rowKey, columnKey), timeTable[rowKey, columnKey])
                set(rowKey, columnKey, maxValue)
            }
        }
    }

    // Do not change or call. It's here for serialization/deserialization purposes.
    private fun getTable() = table
}
