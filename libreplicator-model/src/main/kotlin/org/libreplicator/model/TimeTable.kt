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

import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableMap.copyOf
import com.google.common.collect.ImmutableSet.copyOf
import com.google.common.collect.ImmutableTable
import com.google.common.collect.Table
import java.lang.Math.max

data class TimeTable(private val table: Table<String, String, Long> = HashBasedTable.create()) {
    operator fun get(sourceNodeId: String, targetNodeId: String) = synchronized(this) {
        table.get(sourceNodeId, targetNodeId) ?: 0
    }

    operator fun set(sourceNodeId: String, targetNodeId: String, time: Long) = synchronized(this) {
        if (time > 0L) {
            table.put(sourceNodeId, targetNodeId, time)
        }
    }

    fun mergeRow(sourceNodeId: String, timeTable: TimeTable, targetNodeId: String) = synchronized(this) {
        val sourceRow = copyOf(table.row(sourceNodeId))
        val targetRow = copyOf(timeTable.table.row(targetNodeId))
        val allColumnKeys = copyOf(sourceRow.keys) + copyOf(targetRow.keys)

        allColumnKeys.forEach { columnKey ->
            val maxTime = max(sourceRow[columnKey] ?: 0, targetRow[columnKey] ?: 0)

            if (maxTime > 0) {
                table.put(sourceNodeId, columnKey, maxTime)
            }
        }
    }

    fun merge(timeTable: TimeTable) = synchronized(this) {
        val rowKeys = copyOf(table.rowKeySet()) + copyOf(timeTable.table.rowKeySet())
        val columnKeys = copyOf(table.columnKeySet()) + copyOf(timeTable.table.columnKeySet())

        rowKeys.forEach { rowKey ->
            columnKeys.forEach { columnKey ->
                val maxTime = max(table.get(rowKey, columnKey) ?: 0, timeTable.table.get(rowKey, columnKey) ?: 0)

                if (maxTime > 0) {
                    table.put(rowKey, columnKey, maxTime)
                }
            }
        }
    }

    fun copy(): Table<String, String, Long> = synchronized(this) {
        ImmutableTable.copyOf(table)
    }
}
