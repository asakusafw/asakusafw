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
package com.asakusafw.modelgen.emitter;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.utils.java.model.syntax.ModelFactory;

/**
 * 利用可能な任意のモデルを出力する。
 */
public class AnyModelEntityEmitter {

    private Map<Class<? extends ModelDescription>, ModelEntityEmitter<?>> emitters =
        new HashMap<Class<? extends ModelDescription>, ModelEntityEmitter<?>>();

    /**
     * インスタンスを生成する。
     * @param factory ソースコードを生成するファクトリ
     * @param output 出力先のベースディレクトリ
     * @param packageName 出力先のパッケージ名
     * @param headerComment ファイルのヘッダコメント、不要の場合は{@code null}
     */
    public AnyModelEntityEmitter(
            ModelFactory factory,
            File output,
            String packageName,
            List<String> headerComment) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (packageName == null) {
            throw new IllegalArgumentException("packageName must not be null"); //$NON-NLS-1$
        }
        add(new TableModelEntityEmitter(factory, output, packageName, headerComment));
        add(new JoinedModelEntityEmitter(factory, output, packageName, headerComment));
        add(new SummarizedModelEntityEmitter(factory, output, packageName, headerComment));
    }

    private void add(ModelEntityEmitter<?> emitter) {
        assert emitter != null;
        assert emitters.containsKey(emitter.getEmitTargetType()) == false;
        emitters.put(emitter.getEmitTargetType(), emitter);
    }

    /**
     * 指定のモデルの内容を出力する。
     * @param target 出力するモデル
     * @throws IOException 出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合、
     *     または出力に利用可能なエミッタが存在しない場合
     */
    public void emit(ModelDescription target) throws IOException {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        ModelEntityEmitter<?> emitter = emitters.get(target.getClass());
        if (emitter == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "モデル{0} ({1})に対するエミッタが登録されていません",
                    target.getReference(),
                    target.getClass()));
        }
        emit(target, emitter);
    }

    private <T extends ModelDescription> void emit(
            ModelDescription target,
            ModelEntityEmitter<T> emitter) throws IOException {
        assert target != null;
        assert emitter != null;

        Class<T> type = emitter.getEmitTargetType();
        assert type.isInstance(target);

        T model = type.cast(target);
        emitter.emit(model);
    }
}
