/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

/**
 * Represents a retry controller object.
 * @since 0.7.0
 */
public interface RetryObject {

    /**
     * Waits for next retry attempt and returns whether next retry attempt is required or not.
     * @return {@code true} if the next retry attempt is required, or {@code false} if it is not required
     * @throws InterruptedException if interrupted while waiting for next retry attempt
     */
    boolean waitForNextAttempt() throws InterruptedException;
}
