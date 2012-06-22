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
package com.asakusafw.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;

/**
 * モデルの入出力に関するオブジェクトを生成するファクトリの基底クラス。
 * @param <T> 取り扱うモデルオブジェクトの種類
 */
public abstract class ModelIoFactory<T> {

    static final Log LOG = LogFactory.getLog(ModelIoFactory.class);

    /**
     * モデルクラスの名前のパターン ($1 - ベースパッケージ, $2 - 単純名)。
     */
    static final Pattern MODEL_CLASS_NAME_PATTERN = Pattern.compile(
            "(.*)\\.model\\.([^\\.]+)$");

    /**
     * {@link ModelInput}の実装が配置してある場所の名前 ({0} - ベースパッケージ, {1} - モデルの単純名)。
     */
    public static final String MODEL_INPUT_CLASS_FORMAT = "{0}.io.{1}ModelInput";

    /**
     * {@link ModelOutput}の実装が配置してある場所の名前 ({0} - ベースパッケージ, {1} - モデルの単純名)。
     */
    public static final String MODEL_OUTPUT_CLASS_FORMAT = "{0}.io.{1}ModelOutput";

    private final Class<T> modelClass;

    /**
     * インスタンスを生成する。
     * @param modelClass 取り扱うモデルオブジェクトの種類
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelIoFactory(Class<T> modelClass) {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        this.modelClass = modelClass;
    }

    /**
     * このファクトリが利用するモデルオブジェクトの型を表現するクラスを返す。
     * @return このファクトリが利用するモデルオブジェクトの型を表現するクラス
     */
    protected Class<T> getModelClass() {
        return modelClass;
    }

    /**
     * このファクトリが生成する {@link ModelInput}, {@link ModelOutput}
     * の入出力に利用可能なモデルオブジェクトを新しく生成して返す。
     * @return 生成したオブジェクト
     * @throws IOException インスタンスの生成に失敗した場合
     */
    public T createModelObject() throws IOException {
        try {
            return modelClass.newInstance();
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Cannot create a new model object for {0}",
                    getClass().getName()),
                    e);
        }
    }

    /**
     * このファクトリが対象とするモデルに対する{@link ModelInput}を新しく生成して返す。
     * @param in モデルの情報を取り出す元の入力ストリーム
     * @return 生成した{@code ModelInput}のインスタンス
     * @throws IOException {@code ModelInput}の生成または初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelInput<T> createModelInput(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        RecordParser parser = createRecordParser(in);
        return createModelInput(parser);
    }

    /**
     * このファクトリが対象とするモデルに対する{@link ModelInput}を新しく生成して返す。
     * @param parser モデルの情報を取り出すパーサー
     * @return 生成した{@code ModelInput}のインスタンス
     * @throws IOException {@code ModelInput}の生成または初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelInput<T> createModelInput(RecordParser parser) throws IOException {
        if (parser == null) {
            throw new IllegalArgumentException("parser must not be null"); //$NON-NLS-1$
        }
        Class<?> inputClass;
        try {
            inputClass = findModelInputClass();
        } catch (ClassNotFoundException e) {
            throw new IOException(MessageFormat.format(
                    "Cannot find a model input for {0}",
                    modelClass.getName()),
                    e);
        }
        try {
            Constructor<?> ctor = inputClass.getConstructor(RecordParser.class);
            @SuppressWarnings("unchecked")
            ModelInput<T> instance = (ModelInput<T>) ctor.newInstance(parser);
            return instance;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Cannot initialize a model input for {0}",
                    modelClass.getName()),
                    e);
        }
    }

    /**
     * このファクトリが対象とするモデルに対する{@link ModelOutput}を新しく生成して返す。
     * @param out モデルの情報を書き出す先の出力ストリーム
     * @return 生成した{@code ModelOutput}のインスタンス
     * @throws IOException {@code ModelOutput}の生成または初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelOutput<T> createModelOutput(OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out must not be null"); //$NON-NLS-1$
        }
        RecordEmitter emitter = createRecordEmitter(out);
        return createModelOutput(emitter);
    }

    /**
     * このファクトリが対象とするモデルに対する{@link ModelOutput}を新しく生成して返す。
     * @param emitter モデルの情報を書き出す先のエミッター
     * @return 生成した{@code ModelOutput}のインスタンス
     * @throws IOException {@code ModelOutput}の生成または初期化に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelOutput<T> createModelOutput(RecordEmitter emitter) throws IOException {
        if (emitter == null) {
            throw new IllegalArgumentException("emitter must not be null"); //$NON-NLS-1$
        }
        Class<?> outputClass;
        try {
            outputClass = findModelOutputClass();
        } catch (ClassNotFoundException e) {
            throw new IOException(MessageFormat.format(
                    "Cannot find a model output for {0}",
                    modelClass.getName()),
                    e);
        }
        try {
            Constructor<?> ctor = outputClass.getConstructor(RecordEmitter.class);
            @SuppressWarnings("unchecked")
            ModelOutput<T> instance = (ModelOutput<T>) ctor.newInstance(emitter);
            return instance;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Cannot initialize a model output for {0}",
                    modelClass.getName()),
                    e);
        }
    }

    /**
     * このファクトリが{@link #createModelInput(InputStream)}で利用するレコードパーサーを返す。
     * @param in 対象の入力
     * @return 利用するレコードパーサー
     * @throws IOException パーサーの作成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected abstract RecordParser createRecordParser(InputStream in) throws IOException;

    /**
     * このファクトリが{@link #createModelOutput(OutputStream)}で利用するレコードエミッターを返す。
     * @param out 対象の出力
     * @return 利用するレコードエミッター
     * @throws IOException エミッターの作成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected abstract RecordEmitter createRecordEmitter(OutputStream out) throws IOException;

    /**
     * このファクトリが利用する{@link ModelInput}クラスを返す。
     * <p>
     * この実装では、{@code ...model.Hoge}に対して{@code ...io.HogeModelInput}を返す。
     * </p>
     * @return このファクトリが利用する{@link ModelInput}クラス
     * @throws ClassNotFoundException クラスが見つからない場合
     */
    protected Class<?> findModelInputClass() throws ClassNotFoundException {
        ModelInputLocation annotation = modelClass.getAnnotation(ModelInputLocation.class);
        if (annotation != null) {
            return annotation.value();
        }
        LOG.warn(MessageFormat.format(
                "Data model class \"{0}\" does not have annotation \"{1}\"",
                modelClass.getName(),
                ModelInputLocation.class.getName(),
                ModelInput.class.getSimpleName()));
        return findClassFromModel(MODEL_INPUT_CLASS_FORMAT);
    }

    /**
     * このファクトリが利用する{@link ModelOutput}クラスを返す。
     * <p>
     * この実装では、{@code ...model.Hoge}に対して{@code ...io.HogeModelOutput}を返す。
     * </p>
     * @return このファクトリが利用する{@link ModelOutput}クラス
     * @throws ClassNotFoundException クラスが見つからない場合
     */
    protected Class<?> findModelOutputClass() throws ClassNotFoundException {
        ModelOutputLocation annotation = modelClass.getAnnotation(ModelOutputLocation.class);
        if (annotation != null) {
            return annotation.value();
        }
        LOG.warn(MessageFormat.format(
                "Data model class \"{0}\" does not have annotation \"{1}\"",
                modelClass.getName(),
                ModelOutputLocation.class.getName(),
                ModelOutput.class.getSimpleName()));
        return findClassFromModel(MODEL_OUTPUT_CLASS_FORMAT);
    }

    private Class<?> findClassFromModel(String format) throws ClassNotFoundException {
        Matcher m = MODEL_CLASS_NAME_PATTERN.matcher(modelClass.getName());
        if (m.matches() == false) {
            throw new ClassNotFoundException(MessageFormat.format(
                    "Invalid model class name pattern: {0}",
                    modelClass.getName()));
        }
        String qualifier = m.group(1);
        String simpleName = m.group(2);

        String result = MessageFormat.format(format, qualifier, simpleName);

        return Class.forName(result, false, modelClass.getClassLoader());
    }
}
