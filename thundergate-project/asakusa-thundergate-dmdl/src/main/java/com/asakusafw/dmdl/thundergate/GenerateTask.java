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
package com.asakusafw.dmdl.thundergate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.thundergate.emitter.RecordLockDdlEmitter;
import com.asakusafw.dmdl.thundergate.emitter.ThunderGateModelEmitter;
import com.asakusafw.dmdl.thundergate.model.ModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelRepository;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;
import com.asakusafw.dmdl.thundergate.source.DatabaseSource;

/**
 * プログラムエントリ。
 * @since 0.2.0
 * @version 0.6.1
 */
public class GenerateTask implements Callable<ModelRepository> {

    static final Logger LOG = LoggerFactory.getLogger(GenerateTask.class);

    private final Configuration configuration;

    /**
     * インスタンスを生成する。
     * @param configuration 利用する設定情報
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public GenerateTask(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.configuration = configuration;
    }

    @Override
    public ModelRepository call() {
        ModelRepository repository = new ModelRepository();
        try {
            collectFromDb(repository);
        } catch (IOException e) {
            LOG.error("データベースからテーブルモデルの定義を読み出す際にエラーが発生しました", e);
            return null;
        } catch (SQLException e) {
            LOG.error("データベースからモデルの定義を読み出す際にSQL例外が発生しました", e);
            e.printStackTrace();
        }
        try {
            collectFromViews(repository);
        } catch (IOException e) {
            LOG.error("データベースからビューモデルの定義を読み出す際にエラーが発生しました", e);
            return null;
        } catch (SQLException e) {
            LOG.error("ビューディレクトリからモデルの定義を読み出す際にSQL例外が発生しました", e);
            e.printStackTrace();
        }
        emit(repository);
        generateRecordLockDdl(repository);
        return repository;
    }

    private void collectFromDb(ModelRepository repository) throws IOException, SQLException {
        LOG.info("データベース\"{}\"からテーブルの定義を読み込んでいます",
                configuration.getJdbcUrl());

        DatabaseSource source = new DatabaseSource(
                configuration.getJdbcDriver(),
                configuration.getJdbcUrl(),
                configuration.getJdbcUser(),
                configuration.getJdbcPassword(),
                configuration.getDatabaseName());
        try {
            List<ModelDescription> collected = source.collectTables(
                    configuration.getMatcher());

            for (ModelDescription model : collected) {
                LOG.info("データベースから読み込んだテーブルモデル{}を登録しています",
                        model.getReference());
                repository.add(model);
            }

            LOG.info("データベースから{}個のテーブルモデルを登録しました", collected.size());
        } finally {
            source.close();
        }
    }

    private void collectFromViews(ModelRepository repository) throws IOException, SQLException {
        LOG.info("データベース\"{}\"からビューの定義を読み込んでいます",
                configuration.getJdbcUrl());

        DatabaseSource source = new DatabaseSource(
                configuration.getJdbcDriver(),
                configuration.getJdbcUrl(),
                configuration.getJdbcUser(),
                configuration.getJdbcPassword(),
                configuration.getDatabaseName());
        try {
            List<ModelDescription> collected = source.collectViews(
                    repository,
                    configuration.getMatcher());

            for (ModelDescription model : collected) {
                LOG.info("データベースから読み込んだビューモデル{}を登録しています",
                        model.getReference());
                repository.add(model);
            }

            LOG.info("データベースから{}個のビューモデルを登録しました", collected.size());
        } finally {
            source.close();
        }
    }

    private void emit(ModelRepository repository) {
        List<ModelDescription> models = repository.all();
        int total = models.size();
        LOG.info("{}個のモデルをDMDLとして出力しています: {}",
                total,
                configuration.getOutput());

        ThunderGateModelEmitter emitter = new ThunderGateModelEmitter(configuration);
        int successCount = 0;
        int failedCount = 0;
        for (ModelDescription model : models) {
            LOG.info("モデル{}を出力しています (残り{}個のモデル)",
                    model.getReference(),
                    (total - successCount - failedCount));
            try {
                emitter.emit(model);
                successCount++;
            } catch (Exception e) {
                LOG.error(
                        MessageFormat.format(
                                "モデル{0}の出力に失敗しました",
                                model.getReference()),
                        e);
                failedCount++;
            }
        }

        if (failedCount >= 1) {
            LOG.error("{}個のモデルを正しく出力できませんでした", failedCount);
        } else {
            LOG.info("{}個のモデルを出力しました", total);
        }
    }

    private void generateRecordLockDdl(ModelRepository repository) {
        File output = configuration.getRecordLockDdlOutput();
        if (output == null) {
            return;
        }
        int count = 0;
        RecordLockDdlEmitter generator = new RecordLockDdlEmitter();
        for (TableModelDescription model : repository.allTables()) {
            generator.addTable(model.getReference().getSimpleName());
            count++;
        }
        if (count == 0) {
            LOG.warn("レコードロック用のDDLを生成する対象のテーブルが一つもありません: {}", output);
            return;
        }
        LOG.info("レコードロック用のDDLを生成しています: {}", output);
        try {
            FileOutputStream stream = FileUtils.openOutputStream(output);
            try {
                generator.appendTo(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            LOG.error("レコードロック用のDDL生成に失敗しました", e);
        }
    }
}
