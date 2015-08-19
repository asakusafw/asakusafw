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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.asakusafw.vocabulary.flow.FlowDescription;


/**
 * バッチを記述するための基底クラス。
 * <p>
 * サブクラスでは、{@link #describe()}を継承し、次のようにジョブフロー間の関係を記述する。
 * </p>
<pre><code>
&#64;Batch(name = "hoge")
public class HogeBatch extends BatchDescription {
    &#64;Override
    public void describe() {
        Work first = run(FirstFlow.class).soon();
        Work second = run(SecondFlow.class).after(first);
        Work para = run(ParallelFlow.class).after(first);
        Work join = run(JoinFlow.class).after(second, para);
        ...
    }
}
</code></pre>
 * <p>
 * 上記の例では、まず{@code FirstFlow}が実行され、その後に
 * {@code SecondFlow, ParallelFlow}が実行され、
 * いずれも完了したのちに{@code JoinFlow}が実行されるようなバッチを表す。
 * </p>
 */
public abstract class BatchDescription {

    static final Work[] NOTHING = new Work[0];

    private Map<String, Work> works = new LinkedHashMap<String, Work>();

    private DependencyBuilder adding;

    private final AtomicBoolean described = new AtomicBoolean(false);

    /**
     * バッチ記述メソッドを起動する。
     * @throws IllegalStateException 別のジョブフローを登録している最中であった場合
     */
    public final void start() {
        if (described.compareAndSet(false, true) == false) {
            return;
        }
        describe();
        checkFlushed();
    }

    /**
     * バッチ記述メソッド。
     */
    protected abstract void describe();

    /**
     * このバッチで実行するスクリプトを定義したプロパティファイルを指定する。
     * <p>
     * プロパティファイルはこのクラスを継承したソースファイルが含まれるパッケージからの
     * 相対パスとして指定する。
     * たとえば、{@code com.example}以下にソースファイルを配置して
     * {@code com.example.script}以下にプロパティファイル{@code hoge.properties}を
     * 配置した場合、{@code run(script/hoge.properties)}のように指定する必要がある。
     * </p>
     * <p>
     * それぞれのプロパティファイルに記載すべき内容については、{@link ScriptWorkDescription}
     * のドキュメントを参照すること。
     * </p>
     * @param scriptDefinition このクラスを継承したクラスのパッケージから、
     *      スクリプトを定義したプロパティファイルへの相対パス
     * @return 依存関係を追加するためのビルダー
     * @throws IllegalArgumentException 引数に不正なプロパティファイルが指定された場合
     * @throws IllegalStateException 別の処理を登録している最中であった場合
     */
    protected DependencyBuilder run(String scriptDefinition) {
        if (scriptDefinition == null) {
            throw new IllegalArgumentException("scriptDefinition must not be null"); //$NON-NLS-1$
        }
        ScriptWorkDescription desc;
        try {
            desc = ScriptWorkDescription.load(getClass(), scriptDefinition);
        } catch (IOException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchDescription.errorFailedToLoadScript"), //$NON-NLS-1$
                    scriptDefinition,
                    getClass().getName()),
                    e);
        }
        return run0(desc);
    }

    /**
     * このバッチで実行するジョブフロークラスを指定する。
     * <p>
     * このメソッドだけではジョブフローが登録されたことにはならず、
     * 返されるオブジェクトの次のいずれかを指定する必要がある。
     * </p>
     * <ul>
     * <li> {@link DependencyBuilder#soon() run(...).soon()} </li>
     * <li> {@link DependencyBuilder#after(Work, Work...) run(...).after(...)} </li>
     * </ul>
     * <p>
     * 後者のメソッドは、現在のメソッドに指定したジョブフローを実行する際に、
     * 前提となるジョブフローの一覧を表す。
     * </p>
     * @param jobflow バッチで実行するジョブフロークラス
     * @return 依存関係を追加するためのビルダー
     * @throws IllegalArgumentException 引数にジョブフロークラス以外が指定された場合
     * @throws IllegalStateException 別の処理を登録している最中であった場合
     */
    protected DependencyBuilder run(Class<? extends FlowDescription> jobflow) {
        if (jobflow == null) {
            throw new IllegalArgumentException("jobFlow must not be null"); //$NON-NLS-1$
        }
        return run0(new JobFlowWorkDescription(jobflow));
    }

    private DependencyBuilder run0(WorkDescription description) {
        assert description != null;
        checkFlushed();
        DependencyBuilder builder = new DependencyBuilder(description);
        adding = builder;
        return builder;
    }

    private void checkFlushed() {
        if (adding != null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("BatchDescription.errorIncomplete"), //$NON-NLS-1$
                    adding.description));
        }
    }

    /**
     * このバッチで記述された作業の一覧を返す。
     * @return このバッチで記述された作業の一覧
     */
    public Collection<Work> getWorks() {
        return new ArrayList<Work>(works.values());
    }

    /**
     * このバッチ記述に指定の処理を追加する。
     * @param work 追加する処理
     * @return 追加した処理
     * @throws IllegalStateException すでにこの名前の処理が存在する場合
     */
    Work register(Work work) {
        assert work != null;
        String name = work.getDescription().getName();
        if (works.containsKey(name)) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("BatchDescription.errorDuplicateDescription"), //$NON-NLS-1$
                    name,
                    work.getDescription(),
                    works.get(name).getDescription()));
        }
        works.put(name, work);
        adding = null;
        return work;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}'{'works={1}'}'", //$NON-NLS-1$
                getClass().getName(),
                works);
    }

    /**
     * 依存関係を指定してバッチ内での処理を記述する。
     */
    protected class DependencyBuilder {

        final WorkDescription description;

        DependencyBuilder(WorkDescription description) {
            assert description != null;
            this.description = description;
        }

        /**
         * 現在の処理を前提となる処理が存在しないものとして構成する。
         * @return 構成した処理
         */
        public Work soon() {
            return register(new Work(
                    BatchDescription.this,
                    description,
                    Collections.<Work>emptyList()));
        }

        /**
         * 前提とされる処理を指定して、現在の処理を構成する。
         * @param dependency 前提とされる処理
         * @param rest 前提とされる処理の残りの一覧
         * @return 構成した処理
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public Work after(Work dependency, Work... rest) {
            if (dependency == null) {
                throw new IllegalArgumentException("dependency must not be null"); //$NON-NLS-1$
            }
            if (rest == null) {
                throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
            }
            List<Work> dependencies = new ArrayList<Work>();
            dependencies.add(dependency);
            Collections.addAll(dependencies, rest);
            for (Work p : dependencies) {
                if (dependency.getDeclaring() != BatchDescription.this) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            Messages.getString("BatchDescription.errorInvalidDependency"), //$NON-NLS-1$
                            p));
                }
            }
            return register(new Work(
                    BatchDescription.this,
                    description,
                    dependencies));
        }
    }
}
