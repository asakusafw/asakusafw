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
package com.asakusafw.vocabulary.directio;

import java.util.Collections;
import java.util.List;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * An abstract super class that describes a direct output target.
 * Each subclass must satisfy following requirements:
 * <ul>
 * <li> declared as {@code public} </li>
 * <li> not declared as {@code abstract} </li>
 * <li> not declared type parameters </li>
 * <li> not declared any explicit constructors </li>
 * </ul>
 * @since 0.2.5
 * @version 0.4.0
 */
public abstract class DirectFileOutputDescription implements ExporterDescription {

    /**
     * Returns the base path of target output.
     * <em>On testing, the target path will be deleted by the framework.</em>
     * In the same jobflow,
     * other {@link DirectFileInputDescription#getBasePath() inputs}
     * or {@link #getBasePath() output} must not contain this path.
     * This path can include variables as <code>${&lt;variable-name&gt;}</code>.
     * @return target base path
     */
    public abstract String getBasePath();

    /**
     * Returns the resource path pattern of target output.
     * The framework will output files using this pattern on {@link #getBasePath() the base path}.
     * The pattern can include following characters:
     * <ul>
     * <li> normal characters
     *   <ul>
     *   <li> just represents a path </li>
     *   <li> excepts <code>"\", "$", "*", "?", "#", "|", "{", "}", "[", "]"</code> </li>
     *   </ul>
     * </li>
     * <li> <code>${variable-name}</code> (variables)
     *   <ul>
     *   <li> replaced in runtime with barch arguments </li>
     *   </ul>
     * </li>
     * <li> <code>{&lt;property-name&gt;}</code> (placeholders)
     *   <ul>
     *   <li> generates path fragment from the specified property </li>
     *   <li> each property name should be represented in {@code snake_case} </li>
     *   </ul>
     * </li>
     * <li> <code>[&lt;lower-bound&gt;..&lt;upper-bound&gt;] </code> (random-number)
     *   <ul>
     *   <li> generates a random number between lower-bound and upper-bound (inclusive) </li>
     *   <li> this muset be {@code 0 <= lower-bound < upper-bound} </li>
     *   </ul>
     * </li>
     * <li> <code>*</code> (wildcard)
     *   <ul>
     *   <li> generates a random string from execution context </li>
     *   <li>
     *      <em>IMPORTANT</em>:
     *      If there are wildcard in resource pattern, you CANNOT use neither placeholders nor random-numbers.
     *      Additionally {@link #getOrder()} must be returns empty list.
     *   </li>
     *   </ul>
     * </li>
     * </ul>
     * This path can include variables as <code>${&lt;variable-name&gt;}</code>,
     * but replacement can only contains normal characters.
     * @return the resource path pattern
     */
    public abstract String getResourcePattern();

    /**
     * Returns record order in each output file.
     * Each element is in form of {@code +<property-name>} or {@code -<property-name>},
     * and each property name should be represented in {@code snake_case}.
     * @return record order
     */
    public List<String> getOrder() {
        return Collections.emptyList();
    }

    /**
     * Returns the resource path pattern to delete before output.
     * The framework will delete files using this pattern on {@link #getBasePath() the base path}.
     * Each pattern can include following characters:
     * <ul>
     * <li> normal characters
     *   <ul>
     *   <li> just represents a path </li>
     *   <li> excepts <code>"\", "$", "*", "?", "#", "|", "{", "}", "[", "]"</code> </li>
     *   </ul>
     * </li>
     * <li> <code>${variable-name}</code> (variables)
     *   <ul>
     *   <li> replaced in runtime with barch arguments </li>
     *   </ul>
     * </li>
     * <li> {@code *} (wildcard character)
     *   <ul>
     *   <li> represents wildcard of file/directory name </li>
     *   </ul>
     * </li>
     * <li> {@code **} (wildcard segment)
     *   <ul>
     *   <li> represents all directories and files includes sub directories and files </li>
     *   <li> this must be an alternative of file/directory name;
     *        for example, {@code **.csv} is <em>NOT</em> accepted (<code>&#42;&#42;/&#42;.csv</code> instead)
     *   </li>
     *   </ul>
     * </li>
     * </ul>
     * @return the list of delete patterns
     * @since 0.4.0
     */
    public List<String> getDeletePatterns() {
        return Collections.emptyList();
    }

    /**
     * Returns an implementation of {@link DataFormat} class.
     * @return the class of {@link DataFormat}
     */
    public abstract Class<? extends DataFormat<?>> getFormat();
}
