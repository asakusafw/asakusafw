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
package com.asakusafw.utils.java.model.syntax;


/**
 * An interface which represents instance or class initializers.
 * If this is declared with {@code static} modifier, then it will represent a class initializer.
 * Otherwise it will represent an instance initializer.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:8.6] Instance Initializers} </li>
 *     <li> {@code [JLS3:8.7] Static Initializers} </li>
 *   </ul> </li>
 * </ul>
 */
public interface InitializerDeclaration
        extends TypeBodyDeclaration {

    /**
     * Returns the documentation comment.
     * Note that, this documentation comment will be ignored in the current language specification.
     * @return the documentation comment, or {@code null} if it is not specified
     */
    @Override
    Javadoc getJavadoc();

    /**
     * Returns the initializer body block.
     * @return the initializer body block
     */
    Block getBody();
}
