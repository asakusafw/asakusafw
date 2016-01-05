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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.parser.DmdlParser;
import com.asakusafw.dmdl.parser.DmdlSyntaxException;

/**
 * プログラムエントリ。
 * @since 0.2.0
 * @version 0.6.1
 */
public final class Main {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final Option OPT_OUTPUT;
    private static final Option OPT_JDBC_CONFIG;
    private static final Option OPT_ENCODING;
    private static final Option OPT_INCLUDES;
    private static final Option OPT_EXCLUDES;
    private static final Option OPT_SID_COLUMN;
    private static final Option OPT_TIMESTAMP_COLUMN;
    private static final Option OPT_DELETE_FLAG_COLUMN;
    private static final Option OPT_DELETE_FLAG_VALUE;
    private static final Option OPT_RECORD_LOCK_DDL_OUTPUT;

    private static final Options OPTIONS;
    static {
        OPT_JDBC_CONFIG = new Option("jdbc", true, "リバース対象のJDBC接続先情報");
        OPT_JDBC_CONFIG.setArgName("/path/to/jdbc-config.properties");
        OPT_JDBC_CONFIG.setRequired(true);

        OPT_OUTPUT = new Option("output", true, "リバース結果を出力する先のディレクトリ");
        OPT_OUTPUT.setArgName("/path/to/output");
        OPT_OUTPUT.setRequired(true);

        OPT_ENCODING = new Option("encoding", true, "出力ファイルの文字エンコーディング");
        OPT_ENCODING.setArgName("encoding");
        OPT_ENCODING.setRequired(false);

        OPT_INCLUDES = new Option("includes", true, "対象とするテーブル/ビュー名の正規表現");
        OPT_INCLUDES.setArgName("inclusion-regex");
        OPT_INCLUDES.setRequired(false);

        OPT_EXCLUDES = new Option("excludes", true, "対象から除外するテーブル/ビュー名の正規表現");
        OPT_EXCLUDES.setArgName("exclusion-regex");
        OPT_EXCLUDES.setRequired(false);

        OPT_SID_COLUMN = new Option("sid_column", true, "System IDのカラム名");
        OPT_SID_COLUMN.setArgName("SID");
        OPT_SID_COLUMN.setRequired(false);

        OPT_TIMESTAMP_COLUMN = new Option("timestamp_column", true, "最終更新時刻のカラム名");
        OPT_TIMESTAMP_COLUMN.setArgName("LAST_UPDATED_DATETIME");
        OPT_TIMESTAMP_COLUMN.setRequired(false);

        OPT_DELETE_FLAG_COLUMN = new Option("delete_flag_column", true, "論理削除フラグのカラム名");
        OPT_DELETE_FLAG_COLUMN.setArgName("LOGICAL_DELETE_FLAG");
        OPT_DELETE_FLAG_COLUMN.setRequired(false);

        OPT_DELETE_FLAG_VALUE = new Option("delete_flag_value", true, "論理削除フラグが真(TRUE)となる値 (Javaの定数)");
        OPT_DELETE_FLAG_VALUE.setArgName("1");
        OPT_DELETE_FLAG_VALUE.setRequired(false);

        OPT_RECORD_LOCK_DDL_OUTPUT = new Option("record_lock_ddl_output", true,
                "レコードロック用のシステムテーブルを生成するDDLの生成先");
        OPT_RECORD_LOCK_DDL_OUTPUT.setArgName("/path/to/output.sql");
        OPT_RECORD_LOCK_DDL_OUTPUT.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_JDBC_CONFIG);
        OPTIONS.addOption(OPT_ENCODING);
        OPTIONS.addOption(OPT_INCLUDES);
        OPTIONS.addOption(OPT_EXCLUDES);
        OPTIONS.addOption(OPT_SID_COLUMN);
        OPTIONS.addOption(OPT_TIMESTAMP_COLUMN);
        OPTIONS.addOption(OPT_DELETE_FLAG_COLUMN);
        OPTIONS.addOption(OPT_DELETE_FLAG_VALUE);
        OPTIONS.addOption(OPT_RECORD_LOCK_DDL_OUTPUT);
    }

    private Main() {
        return;
    }

    /**
     * このプログラムを実行する。
     * @param args 一つでも指定した場合、利用方法について表示する
     */
    public static void main(String... args) {
        GenerateTask task;
        try {
            Configuration conf = loadConfigurationFromArguments(args);
            task = new GenerateTask(conf);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            Main.class.getName()),
                    OPTIONS,
                    true);
            e.printStackTrace(System.out);
            System.exit(1);
            return;
        }
        try {
            task.call();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
            return;
        }
    }

    /**
     * コマンドラインから設定情報を復元する。
     * @param args コマンドライン引数の一覧
     * @return 復元した設定情報
     * @throws IllegalStateException 復元に失敗した場合
     */
    public static Configuration loadConfigurationFromArguments(String[] args) {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(OPTIONS, args);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        Configuration result = new Configuration();

        String jdbc = getOption(cmd, OPT_JDBC_CONFIG, true);
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
                    "JDBCの設定ファイルの読み出しに失敗しました: -{0}={1}",
                    OPT_JDBC_CONFIG.getOpt(),
                    jdbc),
                    e);
        }

        String output = getOption(cmd, OPT_OUTPUT, true);
        result.setOutput(new File(output));
        LOG.info("Output: {}", output);

        String includes = getOption(cmd, OPT_INCLUDES, false);
        if (includes != null && includes.isEmpty() == false) {
            try {
                Pattern pattern = Pattern.compile(includes, Pattern.CASE_INSENSITIVE);
                result.setMatcher(new ModelMatcher.Regex(pattern));
                LOG.info("Inclusion: {}", pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "フィルターの正規表現が正しくありません: {0}",
                        includes),
                        e);
            }
        } else {
            result.setMatcher(ModelMatcher.ALL);
        }

        String excludes = getOption(cmd, OPT_EXCLUDES, false);
        if (excludes != null && excludes.isEmpty() == false) {
            try {
                Pattern pattern = Pattern.compile(excludes, Pattern.CASE_INSENSITIVE);
                result.setMatcher(new ModelMatcher.And(
                        result.getMatcher(),
                        new ModelMatcher.Not(new ModelMatcher.ConstantTable(Constants.SYSTEM_TABLE_NAMES)),
                        new ModelMatcher.Not(new ModelMatcher.Regex(pattern))));
                LOG.info("Exclusion: {}", pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "フィルターの正規表現が正しくありません: {0}",
                        excludes),
                        e);
            }
        } else {
            result.setMatcher(new ModelMatcher.And(
                    result.getMatcher(),
                    new ModelMatcher.Not(new ModelMatcher.ConstantTable(Constants.SYSTEM_TABLE_NAMES))));
        }

        String encoding = getOption(cmd, OPT_ENCODING, false);
        if (encoding != null) {
            try {
                Charset charset = Charset.forName(encoding);
                result.setEncoding(charset);
                LOG.info("Encoding: {}", charset);
            } catch (Exception e) {
                result.setEncoding(Constants.OUTPUT_ENCODING);
            }
        } else {
            result.setEncoding(Constants.OUTPUT_ENCODING);
        }

        checkIf(cmd, OPT_SID_COLUMN, OPT_TIMESTAMP_COLUMN);
        checkIf(cmd, OPT_TIMESTAMP_COLUMN, OPT_SID_COLUMN);

        checkIf(cmd, OPT_SID_COLUMN, OPT_DELETE_FLAG_COLUMN);
        checkIf(cmd, OPT_SID_COLUMN, OPT_DELETE_FLAG_VALUE);
        checkIf(cmd, OPT_DELETE_FLAG_COLUMN, OPT_DELETE_FLAG_VALUE);
        checkIf(cmd, OPT_DELETE_FLAG_VALUE, OPT_DELETE_FLAG_COLUMN);

        String sidColumn = trim(getOption(cmd, OPT_SID_COLUMN, false));
        String timestampColumn = trim(getOption(cmd, OPT_TIMESTAMP_COLUMN, false));
        String deleteFlagColumn = trim(getOption(cmd, OPT_DELETE_FLAG_COLUMN, false));
        String deleteFlagValue = trim(getOption(cmd, OPT_DELETE_FLAG_VALUE, false));
        if (deleteFlagValue != null) {
            // FIXME get "bare" string
            List<String> arguments = Arrays.asList(args);
            int index = arguments.indexOf('-' + OPT_DELETE_FLAG_VALUE.getOpt());
            assert index >= 0;
            assert arguments.size() > index + 1;
            deleteFlagValue = trim(arguments.get(index + 1));
        }

        result.setSidColumn(sidColumn);
        result.setTimestampColumn(timestampColumn);
        result.setDeleteFlagColumn(deleteFlagColumn);
        if (deleteFlagValue != null) {
            try {
                DmdlParser dmdl = new DmdlParser();
                AstLiteral literal = dmdl.parseLiteral(deleteFlagValue);
                result.setDeleteFlagValue(literal);
            } catch (DmdlSyntaxException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "論理削除フラグの値はJavaのリテラルの形式で指定してください: {0}",
                        deleteFlagValue), e);
            }
        }
        String recordLockDdlOutput = getOption(cmd, OPT_RECORD_LOCK_DDL_OUTPUT, false);
        if (recordLockDdlOutput != null) {
            result.setRecordLockDdlOutput(new File(recordLockDdlOutput));
        }
        return result;
    }

    private static void checkIf(CommandLine cmd, Option target, Option condition) {
        String conditionValue = getOption(cmd, condition, false);
        if (trim(conditionValue) == null) {
            return;
        }
        String targetValue = getOption(cmd, target, false);
        if (trim(targetValue) == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "引数 \"-{0}\" が指定されていません。 (\"-{1}\"を指定する場合は必須です)",
                    target.getOpt(),
                    condition.getOpt()));
        }
    }

    private static String trim(String string) {
        if (string == null) {
            return null;
        }
        String trimmed = string.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private static String getOption(CommandLine cmd, Option option, boolean mandatory) {
        assert cmd != null;
        assert option != null;
        String value = cmd.getOptionValue(option.getOpt());
        if (mandatory && value == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "コマンドライン引数\"{0}\"が設定されていません",
                    option.getOpt()));
        }
        LOG.debug("Option: {}={}", option.getOpt(), value);
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
