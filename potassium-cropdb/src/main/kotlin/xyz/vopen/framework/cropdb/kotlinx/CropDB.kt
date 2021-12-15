/*
 * Copyright (c) 2021-2022. CropDB author or authors.
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

package xyz.vopen.framework.cropdb.kotlinx

import xyz.vopen.framework.cropdb.CropDB
import xyz.vopen.framework.cropdb.collection.CropCollection
import xyz.vopen.framework.cropdb.index.IndexOptions
import xyz.vopen.framework.cropdb.index.IndexType
import xyz.vopen.framework.cropdb.repository.ObjectRepository

/**
 * @since 2.1.0
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */

/**
 * Opens a named collection from the store. If the collections does not
 * exist it will be created automatically and returned. If a collection
 * is already opened, it is returned as is. Returned collection is thread-safe
 * for concurrent use.
 *
 * @param [name] name of the collection
 * @param [op] collection builder block
 * @return the collection
 */
fun CropDB.getCollection(name: String, op: (CropCollection.() -> Unit)? = null): CropCollection {
    val collection = this.getCollection(name)
    op?.invoke(collection)
    return collection
}

/**
 * Opens a type-safe object repository from the store. If the repository
 * does not exist it will be created automatically and returned. If a
 * repository is already opened, it is returned as is.
 *
 * @param [T] type parameter
 * @param [op] repository builder block
 * @return the object repository of type [T]
 */
inline fun <reified T : Any> CropDB.getRepository(
    noinline op: (ObjectRepository<T>.() -> Unit)? = null
): ObjectRepository<T> {
    val repository = this.getRepository(T::class.java)
    op?.invoke(repository)
    return repository
}

/**
 * Opens a type-safe object repository with a key identifier from the store. If the repository
 * does not exist it will be created automatically and returned. If a
 * repository is already opened, it is returned as is.
 *
 * @param [T] type parameter
 * @param key  the key that will be appended to the repositories name
 * @param [op] repository builder block
 * @return the object repository of type [T]
 */
inline fun <reified T : Any> CropDB.getRepository(
    key: String,
    noinline op: (ObjectRepository<T>.() -> Unit)? = null
): ObjectRepository<T> {
    val repository = this.getRepository(T::class.java, key)
    op?.invoke(repository)
    return repository
}

/**
 * Creates an [IndexOptions] with the specified [indexType].
 *
 * @param [indexType] the type of index to be created.
 * @return a new [IndexOptions]
 */
fun option(indexType: String = IndexType.UNIQUE): IndexOptions = IndexOptions.indexOptions(indexType)