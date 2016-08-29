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
package com.asakusafw.runtime.util.lock;

import java.io.IOException;

/**
 * An abstract super interface of {@link LockObject} provider.
 * @param <T> the target type
 * @since 0.7.0
 */
@FunctionalInterface
public interface LockProvider<T> {

    /**
     * Tries to acquire lock for the target.
     * @param target the lock target
     * @return the lock object, or {@code null} if other program has a lock for the same target
     * @throws IOException if failed to acquire lock by I/O exception
     */
    LockObject<T> tryLock(T target) throws IOException;
}
