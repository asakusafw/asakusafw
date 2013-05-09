/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio;

import java.util.concurrent.atomic.AtomicLong;

import com.asakusafw.runtime.directio.util.CountInputStream;
import com.asakusafw.runtime.directio.util.CountOutputStream;

/**
 * Receives count.
 * @since 0.2.5
 * @see CountInputStream
 * @see CountOutputStream
 */
public class Counter {

    private final AtomicLong entity = new AtomicLong();

    /**
     * Adds count.
     * @param delta count delta
     */
    public void add(long delta) {
        entity.addAndGet(delta);
        onChanged();
    }

    /**
     * Invoked when counter was changed.
     */
    protected void onChanged() {
        return;
    }

    /**
     * Returns current count.
     * @return count
     */
    public long get() {
        return entity.get();
    }
}
