/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.info;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

final class Util {

    static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private Util() {
        return;
    }

    static <T> void check(Class<T> valueType, T value) {
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        try {
            JavaType type = mapper.getTypeFactory().constructSimpleType(
                    Container.class,
                    new JavaType[] { mapper.constructType(valueType) });
            String json = mapper.writerFor(type)
                    .writeValueAsString(new Container<>(value));
            LOG.debug("json: {}", json);
            Container<?> restore = mapper.readValue(json, type);
            assertThat(value.toString(), restore.value, is((Object) value));
            assertThat(value.toString(), restore.value.hashCode(), is(value.hashCode()));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    static class Container<T> {

        public T value;

        public Container() {
            return;
        }

        public Container(T value) {
            this.value = value;
        }
    }
}
