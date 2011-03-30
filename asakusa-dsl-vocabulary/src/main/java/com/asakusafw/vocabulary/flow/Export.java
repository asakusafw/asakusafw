/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.asakusafw.vocabulary.external.ExporterDescription;


/**
 * 利用するエクスポーターの内容を指定する注釈。
 * <p>
 * ジョブフロークラスのコンストラクターで{@link Out}型の引数を利用する場合、
 * 引数にこの注釈を付与してエクスポーターの動作を指定する必要がある。
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Export {

    /**
     * この出力を識別する名前。
     * <p>
     * 出力の名前は、同一のフロー記述における出力内で重複してはならない。
     * また、識別子には、下記の形式の名前 (Javaの変数名のうち、ASCIIコード表に収まるもののみ)
     * を利用可能である。
     * </p>
<pre><code>
Name :
    NameStart NamePart*
NameStart: one of
    A-Z
    a-z
    _
NamePart: one of
    NameStart
    0-9
</code></pre>
     */
    String name();

    /**
     * 利用するエクスポーターに対するエクスポーター記述クラス。
     */
    Class<? extends ExporterDescription> description();
}
