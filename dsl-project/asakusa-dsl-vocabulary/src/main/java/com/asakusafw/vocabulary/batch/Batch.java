/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.batch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * バッチクラスに付与されるべき注釈。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Batch {

    /**
     * このバッチの識別子。
     * <p>
     * この識別子は、同一アプリケーション内で重複してはならない。
     * また、識別子には、下記の形式(Javaのパッケージ名のうち、ASCIIコード表に収まるもののみ)の
     * 名前を利用可能である。
<pre><code>
Name :
    SimpleName
    Name "." SimpleName
SimpleName:
    NameStart NamePart*
NameStart: one of
    A-Z
    a-z
    _
NamePart: one of
    NameStart
    0-9
</code></pre>
     * </p>
     */
    String name();
}
