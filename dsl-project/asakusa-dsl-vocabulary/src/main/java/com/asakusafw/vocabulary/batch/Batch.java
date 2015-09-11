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
package com.asakusafw.vocabulary.batch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * バッチクラスに付与されるべき注釈。
 * TODO i18n
 * @since 0.1.0
 * @version 0.5.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Batch {

    /**
     * The default parameter value pattern.
     */
    String DEFAULT_PARAMETER_VALUE_PATTERN = ".*"; //$NON-NLS-1$

    /**
     * このバッチの識別子。
     * <p>
     * この識別子は、同一アプリケーション内で重複してはならない。
     * また、識別子には、下記の形式(Javaのパッケージ名のうち、ASCIIコード表に収まるもののみ)の
     * 名前を利用可能である。
     * </p>
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
     */
    String name();

    /**
     * このバッチに対するコメント。
     * @since 0.5.0
     */
    String comment() default "";

    /**
     * このバッチに利用可能な引数の一覧。
     * 未指定の場合はなし。
     * @since 0.5.0
     * @see #strict()
     */
    Parameter[] parameters() default { };

    /**
     * {@link #parameters()}で指定していない引数を利用できるかどうか。
     * {@code true}の場合は利用できず、{@code false}の場合は利用できる。
     * 未指定の場合は{@code false}。
     * @since 0.5.0
     */
    boolean strict() default false;

    /**
     * バッチ引数の情報。
     * @since 0.5.0
     */
    public @interface Parameter {

        /**
         * 引数のキー名。
         */
        String key();

        /**
         * 引数のコメント。
         * 未指定の場合はなし。
         */
        String comment() default "";

        /**
         * 必須引数であれば{@code true}, そうでなければ{@code false}。
         * 未指定の場合は {@code true}。
         */
        boolean required() default true;

        /**
         * 引数に指定可能な文字列の正規表現パターン。
         * 未指定の場合は任意の文字列を許可。
         */
        String pattern() default DEFAULT_PARAMETER_VALUE_PATTERN;
    }
}
