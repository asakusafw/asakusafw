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
package com.asakusafw.vocabulary.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to tell how groups/sorts each record.
 * <h3> examples </h3>
<pre><code>
// grouping by 'name'
&#64;Key(group = "name")

// grouping by 'name' and 'sex'
&#64;Key(group = { "name", "sex" })

// grouping by 'name', and sort each record by 'age' in ascending order
&#64;Key(group = "name", order = "age ASC")

// grouping by 'name', and sort each record by 'income' in ascending order and 'age' in descending order
&#64;Key(group = "name", order = { "income ASC", "age DESC" })

// creates the total group, and sort each record by 'count' in descending order
&#64;Key(group = {}, order = "count DESC")
</code></pre>
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Key {

    /**
     * The grouping properties.
     * Specifying property names into this element, this will divide all records into groups for each of the property
     * values. Or if it is an empty array (<code>group = {}</code>), only a single group will be organized.
     */
    String[] group();

    /**
     * The sorting properties.
     * Specifying property names into this element, this will sort the records in each group
     * by the property values in each ascending order.
     * Each property name can follow with {@code ASC} (ascending order) or {@code DESC} (descending order).
     * For details, each array element must be form of the following {@code Order}, where the {@code ID} means
     * the target property name:
<pre><code>
Order:
    ID
    ID 'ASC'
    ID 'DESC'
</code></pre>
     * If this element is omitted or an empty array, the sorting order will be undefined.
     */
    String[] order() default { };
}
