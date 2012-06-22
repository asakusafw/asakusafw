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
package com.asakusafw.runtime.flow.join;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.flow.FlowResource;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.stage.resource.StageResourceDriver;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;

/**
 * 結合を行うためのリソース。
 * @param <L> 結合表を構成する結合先の型、通常はマスタ
 * @param <R> 結合する型、通常はトランザクション
 */
public abstract class JoinResource<L extends Writable, R> implements FlowResource {

    static final Log LOG = LogFactory.getLog(JoinResource.class);

    private final LookUpKey lookupKeyBuffer = new LookUpKey();

    private LookUpTable<L> table;

    @Override
    public void setup(Configuration configuration) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Building join-table from \"{0}\" on distributed cache",
                    getCacheName()));
        }
        StageResourceDriver driver = new StageResourceDriver(configuration);
        try {
            List<Path> paths = driver.findCache(getCacheName());
            if (paths.isEmpty()) {
                throw new FileNotFoundException(MessageFormat.format(
                        "Missing resource \"{0}\" in distributed cache",
                        getCacheName()));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Building join table \"{0}\" using \"{1}\"",
                        getCacheName(),
                        paths));
            }
            try {
                table = createTable(driver, paths);
            } catch (IOException e) {
                throw new IOException(MessageFormat.format(
                        "Failed to build a join table from \"{0}\"",
                        getCacheName()), e);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Built join-table from \"{0}\"",
                        getCacheName()));
            }
        } finally {
            driver.close();
        }
    }
    private LookUpTable<L> createTable(
            StageResourceDriver driver,
            List<Path> paths) throws IOException {
        assert driver != null;
        assert paths != null;
        LookUpTable.Builder<L> builder = createLookUpTable();
        L value = createValueObject();
        for (Path path : paths) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Reading local cache fragment \"{1}\" for join table {0}",
                        getCacheName(),
                        path));
            }
            @SuppressWarnings("unchecked")
            ModelInput<L> input = (ModelInput<L>) TemporaryStorage.openInput(
                    driver.getConfiguration(),
                    value.getClass(),
                    path);
            try {
                while (input.readTo(value)) {
                    lookupKeyBuffer.reset();
                    LookUpKey k = buildLeftKey(value, lookupKeyBuffer);
                    builder.add(k, value);
                    value = createValueObject();
                }
            } finally {
                input.close();
            }
        }
        return builder.build();
    }

    @Override
    public void cleanup(Configuration configuration) throws IOException, InterruptedException {
        return;
    }

    /**
     * 検索表を構築するためのオブジェクトを返す。
     * @return 検索表を構築するためのオブジェクト
     */
    protected LookUpTable.Builder<L> createLookUpTable() {
        return new VolatileLookUpTable.Builder<L>();
    }

    /**
     * キャッシュのファイル名を返す。
     * @return キャッシュのファイル名
     */
    protected abstract String getCacheName();

    /**
     * 結合先の値オブジェクトを返す。
     * @return 結合先の値オブジェクト
     */
    protected abstract L createValueObject();

    /**
     * 結合先の値から検索キーを作成する。
     * @param value 結合先の値
     * @param buffer 検索キーバッファ
     * @return 検索キー
     * @throws IOException キーの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected abstract LookUpKey buildLeftKey(L value, LookUpKey buffer) throws IOException;

    /**
     * 結合元の値から検索キーを作成する。
     * @param value 結合元の値
     * @param buffer 検索キーバッファ
     * @return 検索キー
     * @throws IOException キーの生成に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected abstract LookUpKey buildRightKey(R value, LookUpKey buffer) throws IOException;

    /**
     * 指定の値に対する結合先を探す。
     * @param value 結合する値
     * @return 結合先
     * @throws LookUpException 結合に失敗した場合
     */
    public List<L> find(R value) {
        try {
            lookupKeyBuffer.reset();
            LookUpKey k = buildRightKey(value, lookupKeyBuffer);
            List<L> found = table.get(k);
            return found;
        } catch (IOException e) {
            throw new LookUpException(MessageFormat.format(
                    "Failed to lookup join target for \"{0}\"",
                    value), e);
        }
    }
}
