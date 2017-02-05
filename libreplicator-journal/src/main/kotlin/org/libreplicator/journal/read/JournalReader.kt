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

package org.libreplicator.journal.read

import org.libreplicator.interactor.api.ReplicatorStateProvider
import org.libreplicator.journal.file.FileReader
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonReadException
import org.libreplicator.model.ReplicatorState
import java.nio.file.Path

class JournalReader
constructor(private val journal: Path,
            private val fileReader: FileReader,
            private val jsonMapper: JsonMapper) : ReplicatorStateProvider {

    override fun getInitialState(): ReplicatorState {
        fileReader.readAllLines(journal).reversed().forEach { line ->
            try {
                return jsonMapper.read(line, ReplicatorState::class)
            }
            catch (jsonReadException: JsonReadException) {
                // Go to the next line.
            }
        }
        return ReplicatorState.EMPTY
    }
}
