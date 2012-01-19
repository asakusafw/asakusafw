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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * ステージ入力に対するそれぞれの{@link InputSplit}。
 * <p>
 * 全体の{@code InputFormat, Mapper}に関する設定を無視して、スプリットごとにそれぞれを指定できる。
 * </p>
 */
public class StageInputSplit extends InputSplit implements Writable, Configurable {

    private InputSplit original;

    private Class<? extends InputFormat<?, ?>> formatClass;

    private Class<? extends Mapper<?, ?, ?, ?>> mapperClass;

    private Configuration configuration;

    /**
     * {@link Writable}の初期化用にインスタンスを生成する。
     */
    public StageInputSplit() {
        return;
    }

    /**
     * インスタンスを生成する。
     * @param original 実際のデータに関する{@link InputSplit}
     * @param formatClass このスプリットの元になった{@link InputFormat}
     * @param mapperClass このスプリットを処理する{@link Mapper}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public StageInputSplit(
            InputSplit original,
            Class<? extends InputFormat<?, ?>> formatClass,
            Class<? extends Mapper<?, ?, ?, ?>> mapperClass) {
        if (original == null) {
            throw new IllegalArgumentException("original must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (mapperClass == null) {
            throw new IllegalArgumentException("mapperClass must not be null"); //$NON-NLS-1$
        }
        this.original = original;
        this.formatClass = formatClass;
        this.mapperClass = mapperClass;
    }

    @Override
    public long getLength() throws IOException, InterruptedException {
        return original.getLength();
    }

    @Override
    public String[] getLocations() throws IOException, InterruptedException {
        return original.getLocations();
    }

    /**
     * 実際のデータに関するスプリットの情報を返す。
     * @return 実際のデータに関するスプリットの情報
     */
    public InputSplit getOriginal() {
        return original;
    }

    /**
     * このスプリットの元になったフォーマットクラスを返す。
     * @return このスプリットの元になったフォーマットクラス
     */
    public Class<? extends InputFormat<?, ?>> getFormatClass() {
        return formatClass;
    }

    /**
     * このスプリットを処理するMapperクラスを返す。
     * @return このスプリットを処理するMapperクラス
     */
    public Class<? extends Mapper<?, ?, ?, ?>> getMapperClass() {
        return mapperClass;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Class<? extends InputSplit> splitClass = original.getClass();
        writeClassByName(out, splitClass);
        writeClassByName(out, formatClass);
        writeClassByName(out, mapperClass);
        ((Writable) original).write(out);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFields(DataInput in) throws IOException {
        Class<? extends InputSplit> splitClass = readClassByName(InputSplit.class, in);
        this.formatClass = (Class<? extends InputFormat<?, ?>>) readClassByName(InputFormat.class, in);
        this.mapperClass = (Class<? extends Mapper<?, ?, ?, ?>>) readClassByName(Mapper.class, in);
        this.original = ReflectionUtils.newInstance(splitClass, configuration);
        ((Writable) original).readFields(in);
    }

    private void writeClassByName(DataOutput out, Class<?> aClass) throws IOException {
        assert out != null;
        assert aClass != null;
        out.writeUTF(aClass.getName());
    }

    private <T> Class<? extends T> readClassByName(
            Class<T> baseClass,
            DataInput in) throws IOException {
        assert baseClass != null;
        assert in != null;
        String className = in.readUTF();
        try {
            Class<?> loaded = getConf().getClassByName(className);
            return loaded.asSubclass(baseClass);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to resolve a class name: {0}",
                    className), e);
        }
    }

    @Override
    public void setConf(Configuration conf) {
        this.configuration = conf;
    }

    @Override
    public Configuration getConf() {
        return configuration;
    }
}
