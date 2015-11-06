/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import org.apache.hadoop.io.Writable;

/**
 * A {@link Writable} object with its segment ID.
 */
public interface SegmentedWritable extends Writable {

    /**
     * The method name of {@link #getSegmentId()}.
     */
    String ID_GETTER = "getSegmentId"; //$NON-NLS-1$

    /**
     * Returns the segment ID of this object.
     * @return the segment ID
     */
    int getSegmentId();
}
