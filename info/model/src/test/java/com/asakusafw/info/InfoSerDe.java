/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.info;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Ser/De for DSL information models.
 */
public final class InfoSerDe {

    static final Logger LOG = LoggerFactory.getLogger(InfoSerDe.class);

    private InfoSerDe() {
        return;
    }

    /**
     * Serializes the given object.
     * @param <T> the object type
     * @param type the object type
     * @param object the target object
     * @return the serialized data
     */
    public static <T> byte[] serialize(Class<? extends T> type, T object) {
        ObjectMapper mapper = mapper();
        try {
            byte[] bytes = mapper.writerWithDefaultPrettyPrinter()
                    .forType(type)
                    .writeValueAsBytes(object);
            if (LOG.isTraceEnabled()) {
                LOG.trace(new String(bytes, StandardCharsets.UTF_8));
            }
            return bytes;
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Deserializes the given byte sequence.
     * @param <T> the object type
     * @param type the object type
     * @param bytes the serialized data
     * @return the deserialized object
     */
    public static <T> T deserialize(Class<? extends T> type, byte[] bytes) {
        ObjectMapper mapper = mapper();
        try {
            return mapper.readerFor(type).readValue(bytes);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Validates whether ser/de keeps equivalence of the given object.
     * @param <T> the object type
     * @param type the object type
     * @param object the target object
     * @return the restored object
     */
    public static <T> T checkRestore(Class<? extends T> type, T object) {
        T restored = deserialize(type, serialize(type, object));
        assertThat(object.toString(), restored, equalTo(object));
        assertThat(object.toString(), restored.hashCode(), equalTo(object.hashCode()));
        return restored;
    }

    /**
     * Validates whether ser/de keeps equivalence of the given object.
     * @param <T> the serialization type
     * @param <U> the object type
     * @param type the object type
     * @param object the target object
     * @param eq equivalent tester
     * @return the restored object
     */
    public static <T, U extends T> U checkRestore(
            Class<? extends T> type,
            U object,
            BiPredicate<? super U, ? super U> eq) {
        @SuppressWarnings("unchecked")
        U restored = (U) deserialize(type, serialize(type, object));
        assertThat(object.toString(), eq.test(object, restored), is(true));
        return restored;
    }

    private static ObjectMapper mapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }
}
