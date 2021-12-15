/*
 * Copyright (c) 2019-2020. Crop author or authors.
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

package xyz.vopen.framework.cropdb.rocksdb.formatter;


import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import lombok.extern.slf4j.Slf4j;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static xyz.vopen.framework.cropdb.rocksdb.Constants.DB_NULL;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
@Slf4j
public class KryoObjectFormatter implements ObjectFormatter {
    private static final Kryo kryo = new Kryo();
    private final Map<Class<?>, KryoKeySerializer<?>> keySerializerRegistry;

    public KryoObjectFormatter() {
        this.keySerializerRegistry = new HashMap<>();
        kryo.setRegistrationRequired(false);
        registerInternalSerializers();
    }

    @Override
    public <T> byte[] encode(T object) {
        if (object == null) return DB_NULL;

        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (Output output = new Output(byteArrayOutputStream)) {
                synchronized (kryo) {
                    kryo.writeObject(output, object);
                }
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new CropIOException("failed to close output stream", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> byte[] encodeKey(T object) {
        if (object == null) return DB_NULL;

        Class<?> clazz = object.getClass();
        KryoKeySerializer<T> serializer = (KryoKeySerializer<T>) keySerializerRegistry.get(clazz);
        if (serializer == null) {
            return encode(object);
        }

        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (Output output = new Output(byteArrayOutputStream)) {
                serializer.writeKey(kryo, output, object);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new CropIOException("failed to close output stream", e);
        }
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> type) {
        if (Arrays.equals(bytes, DB_NULL)) return null;

        try (Input input = new Input(bytes, 0, bytes.length)) {
            synchronized (kryo) {
                return kryo.readObject(input, type);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decodeKey(byte[] bytes, Class<T> type) {
        if (Arrays.equals(bytes, DB_NULL)) return null;

        KryoKeySerializer<T> serializer = (KryoKeySerializer<T>) keySerializerRegistry.get(type);
        if (serializer == null) {
            return decode(bytes, type);
        }

        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            try (Input input = new Input(byteArrayInputStream)) {
                return serializer.readKey(kryo, input, type);
            }
        } catch (IOException e) {
            throw new CropIOException("failed to close output stream", e);
        }
    }

    public void registerSerializer(Class<?> type, Serializer<?> serializer) {
        if (serializer instanceof KryoKeySerializer) {
            KryoKeySerializer<?> kryoKeySerializer = (KryoKeySerializer<?>) serializer;
            if (kryoKeySerializer.registerToKryo()) {
                kryo.register(type, serializer);
            }
            keySerializerRegistry.put(type, kryoKeySerializer);
        } else {
            kryo.register(type, serializer);
        }
    }

    private void registerInternalSerializers() {
        try {
            CropSerializers.registerAll(this);
            DefaultJavaSerializers.registerAll(this);
            DefaultTimeKeySerializers.registerAll(this);
        } catch (Exception e) {
            log.error("Error while registering default serializers", e);
            throw new CropIOException("failed to register default serializers", e);
        }
    }
}
