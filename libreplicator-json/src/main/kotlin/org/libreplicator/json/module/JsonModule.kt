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

package org.libreplicator.json.module

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.libreplicator.json.DefaultJsonMapper
import org.libreplicator.json.api.JsonMapper
import org.libreplicator.json.api.JsonMixin
import org.libreplicator.json.mixin.ReplicatorStateMixin
import org.libreplicator.json.mixin.TimeTableMixin
import org.libreplicator.model.ReplicatorState
import org.libreplicator.model.TimeTable

@Module
class JsonModule {
    @Provides
    fun provideJsonMapper(jsonMixins: Set<JsonMixin>): JsonMapper {
        return DefaultJsonMapper(jsonMixins)
    }

    @Provides @IntoSet
    fun provideTimeTableMixin(): JsonMixin {
        return JsonMixin(TimeTable::class, TimeTableMixin::class)
    }

    @Provides @IntoSet
    fun provideReplicatorStateMixin(): JsonMixin {
        return JsonMixin(ReplicatorState::class, ReplicatorStateMixin::class)
    }
}
