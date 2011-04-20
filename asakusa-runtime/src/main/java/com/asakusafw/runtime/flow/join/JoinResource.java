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
package com.asakusafw.runtime.flow.join;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.flow.FlowResource;
import com.asakusafw.runtime.stage.resource.StageResourceDriver;

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
        LOG.info(MessageFormat.format(
                "{0}から結合表を構築します",
                getCacheName()));
        StageResourceDriver driver = new StageResourceDriver(configuration);
        try {
            Path path = driver.findCache(getCacheName());
            if (path == null) {
                throw new FileNotFoundException(MessageFormat.format(
                        "分散キャッシュ\"{0}\"が見つかりません",
                        getCacheName()));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "{0}の結合表は{1}から構築します",
                        getCacheName(),
                        path));
            }
            try {
                table = createTable(driver, path);
            } catch (IOException e) {
                throw new IOException(MessageFormat.format(
                        "分散キャッシュ\"{0}\"からハッシュ表を作成できませんでした",
                        getCacheName()), e);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "{0}の結合表を作成しました",
                        getCacheName()));
            }
        } finally {
            driver.close();
        }
    }
    private LookUpTable<L> createTable(
            StageResourceDriver driver,
            Path resourcePath) throws IOException {
        assert driver != null;
        assert resourcePath != null;
        LookUpTable.Builder<L> builder = createLookUpTable();
        SequenceFile.Reader reader = new SequenceFile.Reader(
                driver.getResourceFileSystem(),
                resourcePath,
                driver.getConfiguration());
        try {
            Writable key = createLeftKeyObject();
            L value = createLeftValueObject();
            while (reader.next(key, value)) {
                lookupKeyBuffer.reset();
                LookUpKey k = buildLeftKey(value, lookupKeyBuffer);
                builder.add(k, value);
                value = createLeftValueObject();
            }
        } finally {
            reader.close();
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
     * 結合先のキーオブジェクトを返す。
     * @return 結合先のキーオブジェクト
     */
    protected Writable createLeftKeyObject() {
        return NullWritable.get();
    }

    /**
     * 結合先の値オブジェクトを返す。
     * @return 結合先の値オブジェクト
     */
    protected abstract L createLeftValueObject();

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
                    "{0}に対する結合先を探す際にエラーが発生しました",
                    value), e);
        }
    }
}
