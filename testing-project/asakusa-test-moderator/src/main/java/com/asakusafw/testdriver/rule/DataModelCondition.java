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
package com.asakusafw.testdriver.rule;

/**
 * Condition of data model objects.
 * @since 0.2.0
 */
public enum DataModelCondition {

    /**
     * Passes if both expected and actual is present.
     */
    IGNORE_MATCHED,

    /**
     * Passes if actual result is missing.
     */
    IGNORE_ABSENT,

    /**
     * Passes if expected data is missing.
     */
    IGNORE_UNEXPECTED,
}
