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
package com.asakusafw.vocabulary.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that represents <em>table models</em>.
 * @deprecated moved to asakusa-thundergate-vocabulary
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableModel {

    /**
     * The original table name.
     */
    String name();

    /**
     * The column names which are used as data model properties.
     */
    String[] columns() default "*";

    /**
     * The set of primary key column names.
     */
    String[] primary();

    /**
     * An interface that provides skeletal information of table models.
     * <p>
     * Each data model class may or may not implement this interface,
     * but that class must provide methods in the interface.
     * </p>
     * @param <T> data model type
     */
    interface Interface<T> extends DataModel.Interface<T> {
        // no members
    }
}
