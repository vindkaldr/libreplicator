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

package org.libreplicator.android

import dagger.Module
import org.libreplicator.android.module.ReplicatorBoundaryAndroidModule
import org.libreplicator.android.module.ReplicatorInteractorAndroidModule
import org.libreplicator.android.module.ReplicatorJsonAndroidModule
import org.libreplicator.android.module.ReplicatorModelAndroidModule
import org.libreplicator.android.module.ReplicatorNetworkAndroidModule

@Module(includes= arrayOf(ReplicatorBoundaryAndroidModule::class, ReplicatorInteractorAndroidModule::class,
        ReplicatorJsonAndroidModule::class, ReplicatorModelAndroidModule::class, ReplicatorNetworkAndroidModule::class))
class ReplicatorAndroidModule
