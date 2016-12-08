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

package hu.dreamsequencer.replicator.model

import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableMap.copyOf
import com.google.common.collect.ImmutableSet.copyOf
import com.google.common.collect.ImmutableTable
import com.google.common.collect.Table
import java.lang.Math.max

data class TimeTable
private constructor(private val table: Table<String, String, Long>) {

    constructor(expectedNumberOfNodes: Int = 10) :
        this(HashBasedTable.create<String, String, Long>(expectedNumberOfNodes, expectedNumberOfNodes))

    operator fun get(sourceNodeId: String, targetNodeId: String) = synchronized(this) {
        table.get(sourceNodeId, targetNodeId) ?: 0
    }

    operator fun set(sourceNodeId: String, targetNodeId: String, time: Long) = synchronized(this) {
        if (time > 0L) {
            table.put(sourceNodeId, targetNodeId, time)
        }
    }

    fun mergeRow(timeTable: TimeTable, sourceNodeId: String) = synchronized(this) {
        val row = copyOf(table.row(sourceNodeId))
        val otherRow = copyOf(timeTable.table.row(sourceNodeId))
        val allColumnKeys = copyOf(row.keys) + copyOf(otherRow.keys)

        allColumnKeys.forEach { columnKey ->
            val maxTime = max(row[columnKey] ?: 0, otherRow[columnKey] ?: 0)

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