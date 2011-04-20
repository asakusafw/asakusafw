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
package com.asakusafw.dmdl.thundergate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
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

/**
 * プログラムエントリ。
 */
public class Main {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final Option OPT_OUTPUT;
    private static final Option OPT_JDBC_CONFIG;
    private static final Option OPT_ENCODING;
    private static final Option OPT_INCLUDES;
    private static final Option OPT_EXCLUDES;

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

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_JDBC_CONFIG);
        OPTIONS.addOption(OPT_ENCODING);
        OPTIONS.addOption(OPT_INCLUDES);
        OPTIONS.addOption(OPT_EXCLUDES);
    }

    /**
     * このプログラムを実行する。
     * @param args 一つでも指定した場合、利用方法について表示する
     * @throws Exception if some errors were occurred
     */
    public static void main(String... args) throws Exception {
        try {
            Configuration conf = loadConfigurationFromArguments(args);
            new GenerateTask(conf).call();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            Main.class.getName()),
                    OPTIONS,
                    true);
            System.exit(1);
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
                        new ModelMatcher.Not(new ModelMatcher.Regex(pattern))));
                LOG.info("Exclusion: {}", pattern);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "フィルターの正規表現が正しくありません: {0}",
                        excludes),
                        e);
            }
        }

        String encoding = getOption(cmd, OPT_ENCODING, false);
        if (encoding != null) {
            try {
                Charset charset = Charset.forName(encoding);
                result.setEncoding(charset);
                LOG.info("Encoding: {}", charset);
            } catch (Exception e) {
                result.setEncoding(Charset.defaultCharset());
            }
        }

        return result;
    }

    private static String getOption(CommandLine cmd, Option option, boolean mandatory) {
        assert cmd != null;
        assert option != null;
        LOG.debug("Finding Variable from arguments: {}", cmd.getArgList());
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
