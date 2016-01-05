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
package com.asakusafw.modelgen;

import java.util.regex.Pattern;

/**
 * 解析対象のモデルが特定のパターンにマッチするかどうか判定する。
 */
public interface ModelMatcher {

    /**
     * 全て。
     */
    ModelMatcher ALL = new ModelMatcher() {
        @Override
        public boolean acceptModel(String name) {
            return true;
        }
    };

    /**
     * 全て。
     */
    ModelMatcher NOTHING = new ModelMatcher() {
        @Override
        public boolean acceptModel(String name) {
            return false;
        }
    };

    /**
     * 対象の名前を持つモデルがこのパターンにマッチする場合のみ{@code true}を返す。
     * @param name 対象のモデルが持つ名前
     * @return パターンにマッチする場合のみ{@code true}
     */
    boolean acceptModel(String name);

    /**
     * 論理積を返すマッチャ。
     */
    public class And implements ModelMatcher {

        private final ModelMatcher[] matchers;

        /**
         * インスタンスを生成する。
         * @param matchers 論理積を取るそれぞれの項
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public And(ModelMatcher... matchers) {
            if (matchers == null) {
                throw new IllegalArgumentException("matchers must not be null"); //$NON-NLS-1$
            }
            this.matchers = matchers.clone();
        }

        @Override
        public boolean acceptModel(String name) {
            for (ModelMatcher m : matchers) {
                if (m.acceptModel(name) == false) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 論理否定を行うフィルタ。
     */
    public class Not implements ModelMatcher {

        private final ModelMatcher term;

        /**
         * インスタンスを生成する。
         * @param term 否定対象のフィルタ
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Not(ModelMatcher term) {
            if (term == null) {
                throw new IllegalArgumentException("term must not be null"); //$NON-NLS-1$
            }
            this.term = term;
        }

        @Override
        public boolean acceptModel(String name) {
            return term.acceptModel(name) == false;
        }
    }

    /**
     * 正規表現によるフィルタ。
     */
    public class Regex implements ModelMatcher {

        private final Pattern pattern;

        /**
         * インスタンスを生成する。
         * @param pattern 許可するパターン
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Regex(Pattern pattern) {
            if (pattern == null) {
                throw new IllegalArgumentException("pattern must not be null"); //$NON-NLS-1$
            }
            this.pattern = pattern;
        }

        @Override
        public boolean acceptModel(String name) {
            return pattern.matcher(name).matches();
        }
    }
}
