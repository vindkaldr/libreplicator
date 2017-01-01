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

package org.libreplicator.dagger

import dagger.Module
import org.libreplicator.dagger.module.LibReplicatorBoundaryModule
import org.libreplicator.dagger.module.LibReplicatorInteractorModule
import org.libreplicator.dagger.module.LibReplicatorJournalModule
import org.libreplicator.dagger.module.LibReplicatorJournalProviderModule
import org.libreplicator.dagger.module.LibReplicatorJsonModule
import org.libreplicator.dagger.module.LibReplicatorModelModule
import org.libreplicator.dagger.module.LibReplicatorNetworkModule

@Module(includes= arrayOf(LibReplicatorBoundaryModule::class, LibReplicatorInteractorModule::class,
        LibReplicatorJournalProviderModule::class, LibReplicatorJournalModule::class,
        LibReplicatorJsonModule::class, LibReplicatorModelModule::class,
        LibReplicatorNetworkModule::class))
class LibReplicatorModule
