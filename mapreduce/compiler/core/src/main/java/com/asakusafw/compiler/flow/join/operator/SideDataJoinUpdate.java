/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.join.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.operator.MasterJoinUpdate;

/**
 * Process {@link MasterJoinUpdate} using side data.
 * Application developers should not attach this to operator methods directly.
 * @see MasterJoinUpdate
 */
@Target({ })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SideDataJoinUpdate {

    /**
     * The input port number of transaction input.
     */
    int ID_INPUT_TRANSACTION = 0;

    /**
     * As {@link MasterJoinUpdate#ID_OUTPUT_UPDATED}.
     */
    int ID_OUTPUT_UPDATED = 0;

    /**
     * As {@link MasterJoinUpdate#ID_OUTPUT_MISSED}.
     */
    int ID_OUTPUT_MISSED = 1;

    /**
     * The external resource number of master input.
     */
    int ID_RESOURCE_MASTER = 0;
}
