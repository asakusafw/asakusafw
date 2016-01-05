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
package com.asakusafw.modelgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.modelgen.emitter.AnyModelEntityEmitter;
import com.asakusafw.modelgen.emitter.ModelInputEmitter;
import com.asakusafw.modelgen.emitter.ModelOutputEmitter;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.modelgen.model.ModelRepository;
import com.asakusafw.modelgen.source.DatabaseSource;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;

/**
 * プログラムエントリ。
 */
public class Main implements Callable<ModelRepository> {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private final Configuration configuration;

    /**
     * インスタンスを生成する。
     * @param configuration 利用する設定情報
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Main(Configuration configuration) {
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
        LOG.info("{}個のモデルを出力しています: {}",
                total,
                configuration.getOutput());

        ModelFactory factory = Models.getModelFactory();
        AnyModelEntityEmitter modelEmitter = new AnyModelEntityEmitter(
                factory,
                configuration.getOutput(),
                configuration.getBasePackage(),
                configuration.getHeaderComments());

        ModelInputEmitter tsvInEmitter = new ModelInputEmitter(
                factory,
                configuration.getOutput(),
                configuration.getBasePackage(),
                configuration.getHeaderComments());

        ModelOutputEmitter tsvOutEmitter = new ModelOutputEmitter(
                factory,
                configuration.getOutput(),
                configuration.getBasePackage(),
                configuration.getHeaderComments());

        int successCount = 0;
        int failedCount = 0;
        for (ModelDescription model : models) {
            LOG.info("モデル{}を出力しています (残り{}個のモデル)",
                    model.getReference(),
                    (total - successCount - failedCount));
            try {
                modelEmitter.emit(model);
                tsvInEmitter.emit(model);
                tsvOutEmitter.emit(model);
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

    /**
     * このプログラムを実行する。
     * @param args 一つでも指定した場合、利用方法について表示する
     */
    public static void main(String... args) {
        Configuration conf = loadConfigurationFromEnvironment();
        new Main(conf).call();
    }

    /**
     * 環境変数から設定情報を復元する。
     * @return 復元した設定情報
     * @throws IllegalStateException 復元に失敗した場合
     */
    public static Configuration loadConfigurationFromEnvironment() {
        Configuration result = new Configuration();

        String jdbc = findVariable(Constants.ENV_JDBC_PROPERTIES, true);
        try {
            Properties jdbcProps = loadProperties(jdbc);
            result.setJdbcDriver(findProperty(jdbcProps, Constants.K_JDBC_DRIVER));
            result.setJdbcUrl(findProperty(jdbcProps, Constants.K_JDBC_URL));
            result.setJdbcUser(findProperty(jdbcProps, Constants.K_JDBC_USER));
            result.setJdbcPassword(findProperty(jdbcProps, Constants.K_JDBC_PASSWORD));
            result.setDatabaseName(findProperty(jdbcProps, Constants.K_DATABASE_NAME));
            LOG.info("JDBCの設定ファイルを読み出しました: {}", jdbcProps);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "JDBCの設定ファイルの読み出しに失敗しました: {0}={1}",
                    Constants.ENV_JDBC_PROPERTIES,
                    jdbc),
                    e);
        }

        String pkg = findVariable(Constants.ENV_BASE_PACKAGE, true);
        try {
            Models.toName(Models.getModelFactory(), pkg);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "パッケージが正しく指定されていません: {0}={1}",
                    Constants.ENV_BASE_PACKAGE,
                    pkg));
        }
        result.setBasePackage(pkg);
        LOG.info("パッケージ: {}", pkg);

        String output = findVariable(Constants.ENV_OUTPUT, true);
        result.setOutput(new File(output));
        LOG.info("出力先: {}", output);

        String includes = findVariable(Constants.ENV_MODEL_INCLUDES, false);
        if (includes != null && includes.isEmpty() == false) {
            try {
                Pattern pattern = Pattern.compile(includes, Pattern.CASE_INSENSITIVE);
                result.setMatcher(new ModelMatcher.Regex(pattern));
                LOG.info("処理対象: {}", pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "フィルターの正規表現が正しくありません: {0}",
                        includes),
                        e);
            }
        } else {
            result.setMatcher(ModelMatcher.ALL);
        }

        String excludes = findVariable(Constants.ENV_MODEL_EXCLUDES, false);
        if (excludes != null && excludes.isEmpty() == false) {
            try {
                Pattern pattern = Pattern.compile(excludes, Pattern.CASE_INSENSITIVE);
                result.setMatcher(new ModelMatcher.And(
                        result.getMatcher(),
                        new ModelMatcher.Not(new ModelMatcher.Regex(pattern))));
                LOG.info("除外対象: {}", pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "フィルターの正規表現が正しくありません: {0}",
                        excludes),
                        e);
            }
        }

        String comment = findVariable(Constants.ENV_HEADER_COMENT, false);
        if (comment != null) {
            try {
                List<String> commentLines = loadLines(comment);
                result.setHeaderComments(commentLines);
                LOG.info("コメント: {}", commentLines);
            } catch (IOException e) {
                throw new IllegalStateException(MessageFormat.format(
                        "ヘッダコメントの読み出しに失敗しました: {0}={1}",
                        Constants.ENV_HEADER_COMENT,
                        comment),
                        e);
            }
        }

        return result;
    }

    private static String findVariable(List<String> variableNames, boolean mandatory) {
        assert variableNames != null;
        assert variableNames.isEmpty() == false;
        LOG.debug("Finding Variable from Environment: {}", variableNames.get(0));
        String value = null;
        for (String var : variableNames) {
            value = System.getProperty(var);
            if (value == null) {
                value = System.getenv(var);
            }
            if (value != null) {
                break;
            }
        }
        if (mandatory && value == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "環境変数\"{0}\"が設定されていません",
                    variableNames.get(0)));
        }
        LOG.debug("Environment Variable: {}={}", variableNames.get(0), value);
        return value;
    }

    private static Properties loadProperties(String path) throws IOException {
        assert path != null;
        LOG.debug("Loading Properties: {}", path);
        InputStream in = new FileInputStream(path);
        try {
            Properties result = new Properties();
            result.load(in);
            return result;
        } finally {
            in.close();
        }
    }

    private static List<String> loadLines(String path) throws IOException {
        Scanner scanner = new Scanner(new File(path), "UTF-8");
        try {
            List<String> result = new ArrayList<String>();
            while (scanner.hasNextLine()) {
                result.add(scanner.nextLine());
            }
            return result;
        } finally {
            scanner.close();
        }
    }

    private static String findProperty(Properties properties, String key) {
        assert properties != null;
        assert key != null;
        LOG.debug("Finding Property: {}", key);
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "プロパティ\"{0}\"が設定されていません",
                    key));
        }
        return value;
    }
}
