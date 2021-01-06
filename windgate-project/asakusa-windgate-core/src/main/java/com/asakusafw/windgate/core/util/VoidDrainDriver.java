/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.util;

import com.asakusafw.windgate.core.resource.DrainDriver;

/**
 * A void implementation of {@link DrainDriver} which drops any inputs.
 * @param <T> the data model type.
 * @since 0.8.1
 */
public class VoidDrainDriver<T> implements DrainDriver<T> {

    @Override
    public void prepare() {
        return;
    }

    @Override
    public void put(T object) {
        return;
    }

    @Override
    public void close() {
        return;
    }
}
