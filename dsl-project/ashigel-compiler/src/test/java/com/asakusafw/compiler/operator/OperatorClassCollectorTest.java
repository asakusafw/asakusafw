/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.compiler.operator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.operator.processor.UpdateOperatorProcessor;
import com.asakusafw.utils.collections.Lists;

/**
 * Test for {@link OperatorClassCollector}.
 */
public class OperatorClassCollectorTest extends OperatorCompilerTestRoot {

    /**
     * 単純な例。
     */
    @Test
    public void simple() {
        add("com.example.Simple");
        start(new Collector(new MockOperatorProcessor()) {
            @Override
            protected void onCollected(List<OperatorClass> classes) {
                assertThat(classes.size(), is(1));

                OperatorClass aClass = classes.get(0);
                assertThat(
                        aClass.getElement().getQualifiedName().toString(),
                        is("com.example.Simple"));

                List<OperatorMethod> methods = aClass.getMethods();
                assertThat(methods.size(), is(1));

                OperatorMethod method = methods.get(0);
                assertThat(
                        method.getElement().getSimpleName().toString(),
                        is("method"));
                assertThat(
                        method.getProcessor().getTargetAnnotationType(),
                        is((Object) MockOperator.class));
            }
        });
    }

    /**
     * すべての公開メソッドは演算子メソッドだが、補助演算子注釈が付与されていれば問題ない。
     */
    @Test
    public void withHelper() {
        add("com.example.WithHelper");
        start(new Collector(new MockOperatorProcessor()) {
            @Override
            protected void onCollected(List<OperatorClass> classes) {
                assertThat(classes.size(), is(1));
            }
        });
    }

    /**
     * 演算子メソッドはそもそもメソッド。
     */
    @Test
    public void methodValidate_notMethod() {
        add("com.example.NotMethod");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * publicでないメソッドはエラー。
     */
    @Test
    public void methodValidate_notPublic() {
        add("com.example.NotPublic");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * staticなメソッドはエラー。
     */
    @Test
    public void methodValidate_Static() {
        add("com.example.Static");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * 重複するメソッドはエラー。
     */
    @Test
    public void methodValidate_duplicateOperator() {
        add("com.example.DuplicateOperator");
        error(new Collector(new MockOperatorProcessor(), new UpdateOperatorProcessor()));
    }

    /**
     * 演算子クラスはそもそもクラス。
     */
    @Test
    public void classValidate_notClass() {
        add("com.example.NotClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * 明示的なコンストラクタは禁止。
     */
    @Test
    public void classValidate_noSimpleConstructor() {
        add("com.example.NoSimpleConstructor");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * publicでないクラスはエラー。
     */
    @Test
    public void classValidate_notPublic() {
        add("com.example.NotPublicClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * abstractでないクラスはエラー。
     */
    @Test
    public void classValidate_notAbstract() {
        add("com.example.NotAbstractClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * 総称クラスはエラー。
     */
    @Test
    public void classValidate_generic() {
        add("com.example.GenericClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * トップレベルでないクラスはエラー。
     */
    @Test
    public void classValidate_enclosing() {
        add("com.example.Enclosing");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * すべての公開メソッドは演算子メソッド。
     */
    @Test
    public void classValidate_notCovered() {
        add("com.example.NotCovered");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * メソッド名の衝突。
     */
    @Test
    public void classValidate_methodConflicted() {
        add("com.example.MethodConflicted");
        error(new Collector(new MockOperatorProcessor(), new UpdateOperatorProcessor()));
    }

    /**
     * メンバ名の衝突。
     */
    @Test
    public void classValidate_memberConflicted() {
        add("com.example.MemberConflicted");
        error(new Collector(new MockOperatorProcessor(), new UpdateOperatorProcessor()));
    }

    /**
     * 補助演算子注釈が付与されたメソッドはpublicで宣言されなければならない。
     */
    @Test
    public void classValidate_withHelper() {
        add("com.example.NotPublicHelper");
        error(new Collector(new MockOperatorProcessor(), new MockOperatorProcessor()));
    }

    private static class Collector extends Callback {

        List<OperatorProcessor> processors;

        Collector(OperatorProcessor...processors) {
            this.processors = Lists.create();
            Collections.addAll(this.processors, processors);
        }

        @Override
        protected final void test() {
            if (round.getRootElements().isEmpty()) {
                return;
            }
            try {
                OperatorClassCollector collector = new OperatorClassCollector(env, round);
                for (OperatorProcessor proc : processors) {
                    proc.initialize(env);
                    collector.add(proc);
                }
                List<OperatorClass> results = collector.collect();
                onCollected(results);
            } catch (OperatorCompilerException e) {
                // 大域脱出のためだけなのでスキップ
            }
        }

        /**
         * {@link OperatorClassCollector}によって回収されたメソッドとそのクラス一覧が渡される。
         * @param classes クラス一覧
         */
        protected void onCollected(List<OperatorClass> classes) {
            return;
        }
    }
}
