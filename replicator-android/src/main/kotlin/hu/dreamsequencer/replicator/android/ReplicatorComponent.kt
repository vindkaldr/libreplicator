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

package hu.dreamsequencer.replicator.android

import dagger.Component
import hu.dreamsequencer.replicator.android.module.ReplicatorBoundaryAndroidModule
import hu.dreamsequencer.replicator.android.module.ReplicatorInteractorAndroidModule
import hu.dreamsequencer.replicator.android.module.ReplicatorJsonAndroidModule
import hu.dreamsequencer.replicator.android.module.ReplicatorModelAndroidModule
import hu.dreamsequencer.replicator.android.module.ReplicatorNetworkAndroidModule
import javax.inject.Singleton

@Singleton
@Component(modules= arrayOf(ReplicatorBoundaryAndroidModule::class, ReplicatorInteractorAndroidModule::class,
        ReplicatorJsonAndroidModule::class, ReplicatorModelAndroidModule::class, ReplicatorNetworkAndroidModule::class))
interface ReplicatorComponent {
    fun inject(replicatorClient: ReplicatorClient)
}
