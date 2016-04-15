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
package com.asakusafw.runtime.trace;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a trace location for classes.
 * @since 0.5.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceLocation {

    /**
     * Represents the unknown ID.
     */
    String UNKNOWN = "?"; //$NON-NLS-1$

    /**
     * Represents the computation module ID.
     */
    String COMPUTATION_MODULE = "computation"; //$NON-NLS-1$

    /**
     * The batch ID.
     */
    String batchId();

    /**
     * The flow ID.
     */
    String flowId();

    /**
     * The stage ID.
     */
    String stageId();

    /**
     * The stage unit ID.
     */
    String stageUnitId() default UNKNOWN;

    /**
     * The fragment ID.
     * This is equivalent to the fragment serial number.
     */
    String fragmentId() default UNKNOWN;
}
