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
package com.asakusafw.utils.java.jsr269.bridge;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * {@link DelegateProcessor}から呼び戻される。
 */
public abstract class Callback {

    private RuntimeException runtimeException;

    private Error error;

    /**
     * 環境オブジェクト。
     */
    protected ProcessingEnvironment env;

    /**
     * 型環境。
     */
    protected Types types;

    /**
     * 要素環境。
     */
    protected Elements elements;

    /**
     * ラウンドオブジェクト。
     */
    protected RoundEnvironment round;

    /**
     * {@link #test()}を安全な環境で実行する。
     * <p>
     * </p>
     * @param env 環境
     * @param round ラウンド環境
     */
    @SuppressWarnings("hiding")
    public void run(ProcessingEnvironment env, RoundEnvironment round) {
        this.env = env;
        this.round = round;
        this.types = env.getTypeUtils();
        this.elements = env.getElementUtils();
        try {
            test();
        } catch (RuntimeException e) {
            this.runtimeException = e;
        } catch (Error e) {
            this.error = e;
        }
    }

    /**
     * {@link #test()}で実行されたエラーをスローする。
     */
    public void rethrow() {
        if (runtimeException != null) {
            throw runtimeException;
        } else if (error != null) {
            throw error;
        }
    }

    /**
     * 実際のテストを実行する。
     */
    protected abstract void test();

    /**
     * 宣言型を返す。
     * @param klass 対象のクラス
     * @param arguments 型引数の一覧
     * @return 宣言型
     */
    protected TypeMirror getType(Class<?> klass, TypeMirror...arguments) {
        TypeElement type = elements.getTypeElement(klass.getName());
        assertThat(klass.getName(), type, not(nullValue()));
        if (arguments.length == 0) {
            return types.erasure(type.asType());
        } else {
            return types.getDeclaredType(type, arguments);
        }
    }
}
