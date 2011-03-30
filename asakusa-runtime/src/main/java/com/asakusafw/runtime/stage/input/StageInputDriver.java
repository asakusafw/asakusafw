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
package com.asakusafw.runtime.stage.input;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * ステージ入力を設定するためのドライバ。
 * <p>
 * 現在のところ、{@link FileInputFormat}およびそのサブクラスに関するもののみを取り扱う。
 * </p>
 */
public final class StageInputDriver {

    /**
     * ステージ入力の一覧を保持する設定名。
<pre><code>
Sequence:
    Sequence ";" Input
    Input

Input:
    Path "#" InputFormatClassName "#" MapperClassName
</code></pre>
     */
    private static final String K_SEQUENCE = "com.asakusafw.stage.input.sequence";

    private static final String DELIM_INPUT = ";";

    private static final String DELIM_FIELD = "#";

    private static final int INPUT_FIELD_LENGTH = 3;

    private static final int PATH_FIELD_INDEX = 0;

    private static final int FORMAT_FIELD_INDEX = 1;

    private static final int MAPPER_FIELD_INDEX = 2;

    /**
     * 指定のジョブに入力の情報を追加する。
     * @param job 対象のジョブ
     * @param path 対象のパス
     * @param formatClass 入力フォーマットクラス
     * @param mapperClass 起動するMapperクラス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    @SuppressWarnings("rawtypes")
    public static void add(
            Job job,
            Path path,
            Class<? extends InputFormat> formatClass,
            Class<? extends Mapper> mapperClass) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (mapperClass == null) {
            throw new IllegalArgumentException("mapperClass must not be null"); //$NON-NLS-1$
        }
        String encoded = new Input(path, formatClass, mapperClass).encode();
        String sequence = job.getConfiguration().get(K_SEQUENCE);
        if (sequence == null) {
            sequence = encoded;
            job.setInputFormatClass(StageInputFormat.class);
            job.setMapperClass(StageInputMapper.class);
        } else {
            sequence += DELIM_INPUT + encoded;
        }
        job.getConfiguration().set(K_SEQUENCE, sequence);
    }

    /**
     * ジョブに設定されたステージ入力の一覧を返す。
     * @param conf 設定情報
     * @return ジョブに設定されたステージ入力の一覧、未設定の場合は空のリスト
     * @throws IOException ステージ一覧の情報を復元できなかった場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    static List<Input> getInputs(Configuration conf) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        String sequence = conf.get(K_SEQUENCE);
        if (sequence == null) {
            return Collections.emptyList();
        }
        String[] inputs = sequence.split(DELIM_INPUT);
        List<Input> results = new ArrayList<Input>();
        for (String input : inputs) {
            Input decoded = Input.decode(input, conf);
            results.add(decoded);
        }
        return results;
    }

    private StageInputDriver() {
        return;
    }

    /**
     * ステージ入力のひとつ分を表す。
     */
    public static class Input {

        private final Path path;

        private final Class<? extends InputFormat<?, ?>> formatClass;

        private final Class<? extends Mapper<?, ?, ?, ?>> mapperClass;

        /**
         * 指定のジョブに入力の情報を追加する。
         * @param path 対象のパス
         * @param formatClass 入力フォーマットクラス
         * @param mapperClass 起動するMapperクラス
         * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Input(
                Path path,
                Class<? extends InputFormat> formatClass,
                Class<? extends Mapper> mapperClass) {
            if (path == null) {
                throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
            }
            if (formatClass == null) {
                throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
            }
            if (mapperClass == null) {
                throw new IllegalArgumentException("mapperClass must not be null"); //$NON-NLS-1$
            }
            this.path = path;
            this.mapperClass = (Class<? extends Mapper<?, ?, ?, ?>>) mapperClass;
            this.formatClass = (Class<? extends InputFormat<?, ?>>) formatClass;
        }

        /**
         * 入力対象のパスを返す。
         * @return 入力対象のパス
         */
        public Path getPath() {
            return path;
        }

        /**
         * 入力フォーマットクラスを返す。
         * @return 入力フォーマットクラス
         */
        public Class<? extends InputFormat<?, ?>> getFormatClass() {
            return formatClass;
        }

        /**
         * 起動するマッパークラスを返す。
         * @return 起動するマッパークラス
         */
        public Class<? extends Mapper<?, ?, ?, ?>> getMapperClass() {
            return mapperClass;
        }

        String encode() {
            StringBuilder buf = new StringBuilder();
            buf.append(path.toString());
            buf.append(DELIM_FIELD);
            buf.append(formatClass.getName());
            buf.append(DELIM_FIELD);
            buf.append(mapperClass.getName());
            return buf.toString();
        }

        @SuppressWarnings("unchecked")
        static Input decode(String encoded, Configuration conf) throws IOException {
            if (encoded == null) {
                throw new IllegalArgumentException("encoded must not be null"); //$NON-NLS-1$
            }
            if (conf == null) {
                throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
            }
            String[] splitted = encoded.split(DELIM_FIELD);
            if (splitted.length != INPUT_FIELD_LENGTH) {
                throw new IOException(MessageFormat.format(
                        "Cannot resolve stage input: {0}",
                        encoded));
            }
            try {
                Path path = new Path(splitted[PATH_FIELD_INDEX]);
                Class<? extends InputFormat<?, ?>> formatClass =
                    (Class<? extends InputFormat<?, ?>>) conf.getClassByName(splitted[FORMAT_FIELD_INDEX]);
                Class<? extends Mapper<?, ?, ?, ?>> mapperClass =
                    (Class<? extends Mapper<?, ?, ?, ?>>) conf.getClassByName(splitted[MAPPER_FIELD_INDEX]);
                return new Input(path, formatClass, mapperClass);
            } catch (ClassNotFoundException e) {
                throw new IOException(MessageFormat.format(
                        "Cannot resolve stage input: {0}",
                        encoded), e);
            }
        }
    }

    /**
     * パスとそれに関連する型情報。
     * @param <T> 型情報に関連する型
     */
    public static class PathAndType<T> {

        private final Path path;

        private final Class<? extends T> type;

        /**
         * インスタンスを生成する。
         * @param path 対象のパス
         * @param type 対象のクラス
         * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
         */
        public PathAndType(Path path, Class<? extends T> type) {
            if (path == null) {
                throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
            }
            if (type == null) {
                throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
            }
            this.path = path;
            this.type = type;
        }

        /**
         * 関連するパスを返す。
         * @return 関連するパス
         */
        public Path getPath() {
            return path;
        }

        /**
         * 関連する型を返す。
         * @return 関連する型
         */
        public Class<? extends T> getType() {
            return type;
        }
    }
}
