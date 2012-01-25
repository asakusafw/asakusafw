/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.hadoop;

import org.apache.hadoop.util.Progressable;
import com.asakusafw.runtime.directio.Counter;

/**
 * Counter which reports a progress using {@link Progressable}.
 * @since 0.2.5
 */
public final class ProgressableCounter extends Counter {

    private final Progressable progressable;

    /**
     * Creates a new instance.
     * @param progressable the progressable object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ProgressableCounter(Progressable progressable) {
        if (progressable == null) {
            throw new IllegalArgumentException("progressable must not be null"); //$NON-NLS-1$
        }
        this.progressable = progressable;
    }

    @Override
    protected void onChanged() {
        progressable.progress();
    }
}
