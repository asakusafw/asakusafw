/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.operator.processor.UpdateOperatorProcessor;

/**
 * Test for {@link OperatorClassCollector}.
 */
public class OperatorClassCollectorTest extends OperatorCompilerTestRoot {

    /**
     * simple case.
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
     * w/ operator helper methods.
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
     * Private methods can have a name as same as other operator methods.
     */
    @Test
    public void withPrivateOverload() {
        add("com.example.PrivateOverload");
        start(new Collector(new MockOperatorProcessor()) {
            @Override
            protected void onCollected(List<OperatorClass> classes) {
                assertThat(classes.size(), is(1));
            }
        });
    }

    /**
     * not a method.
     */
    @Test
    public void methodValidate_notMethod() {
        add("com.example.NotMethod");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * not public method.
     */
    @Test
    public void methodValidate_notPublic() {
        add("com.example.NotPublic");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * static method.
     */
    @Test
    public void methodValidate_Static() {
        add("com.example.Static");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * conflict operator name.
     */
    @Test
    public void methodValidate_duplicateOperator() {
        add("com.example.DuplicateOperator");
        error(new Collector(new MockOperatorProcessor(), new UpdateOperatorProcessor()));
    }

    /**
     * not a class.
     */
    @Test
    public void classValidate_notClass() {
        add("com.example.NotClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * w/o simple constructor.
     */
    @Test
    public void classValidate_noSimpleConstructor() {
        add("com.example.NoSimpleConstructor");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * not public class.
     */
    @Test
    public void classValidate_notPublic() {
        add("com.example.NotPublicClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * not abstract class.
     */
    @Test
    public void classValidate_notAbstract() {
        add("com.example.NotAbstractClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * generic class.
     */
    @Test
    public void classValidate_generic() {
        add("com.example.GenericClass");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * not top-level class.
     */
    @Test
    public void classValidate_enclosing() {
        add("com.example.Enclosing");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * public method w/o operator annotations.
     */
    @Test
    public void classValidate_notCovered() {
        add("com.example.NotCovered");
        error(new Collector(new MockOperatorProcessor()));
    }

    /**
     * conflict method name.
     */
    @Test
    public void classValidate_methodConflicted() {
        add("com.example.MethodConflicted");
        error(new Collector(new MockOperatorProcessor(), new UpdateOperatorProcessor()));
    }

    /**
     * conflict member name.
     */
    @Test
    public void classValidate_memberConflicted() {
        add("com.example.MemberConflicted");
        error(new Collector(new MockOperatorProcessor(), new UpdateOperatorProcessor()));
    }

    /**
     * not public operator helper methods.
     */
    @Test
    public void classValidate_withHelper() {
        add("com.example.NotPublicHelper");
        error(new Collector(new MockOperatorProcessor(), new MockOperatorProcessor()));
    }

    private static class Collector extends Callback {

        List<OperatorProcessor> processors;

        Collector(OperatorProcessor...processors) {
            this.processors = new ArrayList<>();
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
                // ignore exception
            }
        }

        /**
         * invoked with the collected classes.
         * @param classes the collected classes
         */
        protected void onCollected(List<OperatorClass> classes) {
            return;
        }
    }
}
