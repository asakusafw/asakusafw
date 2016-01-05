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
package com.asakusafw.cleaner.bean;

import java.io.File;

/**
 * LocalFileCleanerの設定値を保持するBean。
 * @author yuta.shirai
 *
 */
public class LocalFileCleanerBean {
    /** クリーニングディレクトリ。 */
    private File cleanDir = null;
    /** クリーニングファイルのパターン。 */
    private String pattern = null;
    /**
     * クリーニングディレクトリを返す。
     * @return cleanDir
     */
    public File getCleanDir() {
        return cleanDir;
    }
    /**
     * クリーニングディレクトリをセットする。
     * @param cleanDir セットする cleanDir
     */
    public void setCleanDir(File cleanDir) {
        this.cleanDir = cleanDir;
    }
    /**
     * クリーニングファイルのパターンを返す。
     * @return pattern
     */
    public String getPattern() {
        return pattern;
    }
    /**
     * クリーニングファイルのパターンをセットする。
     * パターンは正企表現で記述する。
     * @param pattern セットする pattern
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

}
