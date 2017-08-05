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

package org.libreplicator.interactor.module

import dagger.Binds
import dagger.Module
import org.libreplicator.interactor.api.LogDispatcher
import org.libreplicator.interactor.dispatcher.DefaultLogDispatcher
import org.libreplicator.interactor.router.DefaultMessageRouter
import org.libreplicator.interactor.router.MessageRouter
import org.libreplicator.interactor.state.DefaultStateInteractor
import org.libreplicator.interactor.state.StateInteractor
import javax.inject.Singleton

@Module
interface InteractorModule {
    @Binds @Singleton fun bindStateInteractor(defaultStateInteractor: DefaultStateInteractor): StateInteractor
    @Binds fun bindLogDispatcher(defaultLogDispatcher: DefaultLogDispatcher): LogDispatcher
    @Binds fun bindMessageRouter(defaultMessageRouter: DefaultMessageRouter): MessageRouter
}
