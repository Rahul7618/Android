/*
 * Copyright (c) 2021 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.privacy.config.store.features.contentblocking

import com.duckduckgo.app.global.DispatcherProvider
import com.duckduckgo.privacy.config.api.ContentBlockingException
import com.duckduckgo.privacy.config.store.ContentBlockingExceptionEntity
import com.duckduckgo.privacy.config.store.PrivacyConfigDatabase
import com.duckduckgo.privacy.config.store.toContentBlockingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface ContentBlockingRepository {
    fun updateAll(exceptions: List<ContentBlockingExceptionEntity>)
    val exceptions: ArrayList<ContentBlockingException>
}

class RealContentBlockingRepository(val database: PrivacyConfigDatabase, coroutineScope: CoroutineScope, dispatcherProvider: DispatcherProvider) :
    ContentBlockingRepository {

    private val contentBlockingDao: ContentBlockingDao = database.contentBlockingDao()
    override val exceptions = ArrayList<ContentBlockingException>()

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            loadToMemory()
        }
    }

    override fun updateAll(exceptions: List<ContentBlockingExceptionEntity>) {
        contentBlockingDao.updateAll(exceptions)
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        contentBlockingDao.getAll().map {
            exceptions.add(it.toContentBlockingException())
        }
    }
}
