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
package com.asakusafw.windgate.core.util;

/**
 * Holds occurred exceptions.
 * @param <T> the target exception type
 * @since 0.8.0
 */
public class ExceptionHolder<T extends Exception> {

    private T occurred;

    /**
     * Records the target exception.
     * @param exception the target exception
     */
    public void record(T exception) {
        if (occurred == null) {
            occurred = exception;
        } else {
            occurred.addSuppressed(exception);
        }
    }

    /**
     * Throws the previously recorded exception.
     * @throws T if some exceptions have been {@link #record(Exception) recorded}
     */
    public void throwRecorded() throws T {
        if (occurred != null) {
            throw occurred;
        }
    }
}
