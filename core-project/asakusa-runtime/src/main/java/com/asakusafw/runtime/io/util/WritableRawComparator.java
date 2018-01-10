/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.util;

import java.io.IOException;

import org.apache.hadoop.io.WritableComparator;

/**
 * An abstract implementation of raw comparator.
 * @since 0.2.5
 */
public class WritableRawComparator extends WritableComparator {

    private final WritableRawComparable object;

    /**
     * Creates a new instance.
     * @param aClass target class
     */
    protected WritableRawComparator(Class<? extends WritableRawComparable> aClass) {
        super(aClass);
        this.object = (WritableRawComparable) newKey();
    }

    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        try {
            return object.compareInBytes(b1, s1, b2, s2);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
