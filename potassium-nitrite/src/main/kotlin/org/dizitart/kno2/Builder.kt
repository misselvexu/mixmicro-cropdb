/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.kno2

import org.dizitart.no2.Nitrite
import org.dizitart.no2.NitriteBuilder
import org.dizitart.no2.NitriteConfig
import org.dizitart.no2.common.module.NitriteModule
import org.dizitart.no2.common.module.NitriteModule.module
import org.dizitart.no2.spatial.SpatialIndexer

/**
 * A builder to create a nitrite database.
 *
 * @since 2.1.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
class Builder internal constructor() {
    private val modules = mutableSetOf<NitriteModule>()

    /**
     * Specifies the separator character for embedded field.
     * Default value is `.`
     *
     * */
    var fieldSeparator: String = NitriteConfig.getFieldSeparator()

    /**
     * Loads [NitriteModule] instances.
     * */
    fun loadModule(module: NitriteModule) {
        modules.add(module)
    }

    internal fun createNitriteBuilder(): NitriteBuilder {
        val builder = Nitrite.builder()

        modules.forEach { builder.loadModule(it) }
        loadDefaultPlugins(builder)

        builder.fieldSeparator(fieldSeparator)
        return builder
    }

    private fun loadDefaultPlugins(builder: NitriteBuilder) {
        val mapperFound = modules.any { module -> module.plugins().any { it is KNO2JacksonMapper } }
        val spatialIndexerFound = modules.any { module -> module.plugins().any { it is SpatialIndexer } }

        if (!mapperFound && spatialIndexerFound) {
            builder.loadModule(module(KNO2JacksonMapper()))
        } else if (!spatialIndexerFound && mapperFound) {
            builder.loadModule(module(SpatialIndexer()))
        } else if (!mapperFound && !spatialIndexerFound) {
            builder.loadModule(KNO2Module())
        }
    }
}

/**
 * Opens or creates a new database. If it is an in-memory store, then it
 * will create a new one. If it is a file based store, and if the file does not
 * exists, then it will create a new file store and open; otherwise it will
 * open the existing file store.
 *
 * @param [userId] the user id
 * @param [password] the password
 * @return the nitrite database instance.
 */
fun nitrite(userId: String? = null, password: String? = null,
            op: (Builder.() -> Unit)? = null): Nitrite {
    val builder = Builder()
    op?.invoke(builder)
    val nitriteBuilder = builder.createNitriteBuilder()
    return if (userId.isNullOrEmpty() && password.isNullOrEmpty()) {
        nitriteBuilder.openOrCreate()
    } else {
        nitriteBuilder.openOrCreate(userId, password)
    }
}