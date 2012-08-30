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
package com.asakusafw.vocabulary.batch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * スクリプトを実行する記述。
 */
public class ScriptWorkDescription extends WorkDescription {

    /**
     * コマンド行のプロパティキー名。
     */
    public static final String K_NAME = "name";

    /**
     * コマンド行のプロパティキー名。
     */
    public static final String K_COMMAND = "command";

    /**
     * プロファイルのプロパティキー名。
     */
    public static final String K_PROFILE = "profile";

    /**
     * 環境変数のプロパティキー接頭辞。
     */
    public static final String K_ENVIRONMENT_PREFIX = "env.";

    private String name;

    private String command;

    private String profileName;

    private Map<String, String> variables;

    /**
     * インスタンスを生成する。
     * @param name 識別子
     * @param command コマンド行
     * @param profileName プロファイル名
     * @param variables 環境変数一覧
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ScriptWorkDescription(
            String name,
            String command,
            String profileName,
            Map<String, String> variables) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (command == null) {
            throw new IllegalArgumentException("command must not be null"); //$NON-NLS-1$
        }
        if (profileName == null) {
            throw new IllegalArgumentException("profileName must not be null"); //$NON-NLS-1$
        }
        if (variables == null) {
            throw new IllegalArgumentException("variables must not be null"); //$NON-NLS-1$
        }
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0}はスクリプトの名前として正しくありません ({0})",
                    name,
                    command));
        }
        this.name = name;
        this.command = command;
        this.profileName = profileName;
        this.variables = Collections.unmodifiableSortedMap(new TreeMap<String, String>(variables));
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * このスクリプトのコマンドを返す。
     * @return コマンド
     */
    public String getCommand() {
        return command;
    }

    /**
     * このスクリプトを実行するプロファイルの名前を返す。
     * @return スクリプトを実行するプロファイルの名前
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * このスクリプトを実行する際の環境変数一覧を返す。
     * @return スクリプト実行時の環境変数の一覧
     */
    public Map<String, String> getVariables() {
        return variables;
    }

    /**
     * このクラスのオブジェクトの内容を表すプロパティファイルを読み出し、新しいオブジェクトを返す。
     * @param context プロパティファイルを検索する文脈となるクラス
     * @param path 指定した文脈クラスを含むパッケージからの、プロパティファイルの相対パス
     * @return 生成したオブジェクト
     * @throws IOException プロパティファイルの読み出しに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static ScriptWorkDescription load(
            Class<?> context,
            String path) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        InputStream stream = context.getResourceAsStream(path);
        if (stream == null) {
            throw new FileNotFoundException(path);
        }
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } finally {
            stream.close();
        }
        return load(properties);
    }

    private static ScriptWorkDescription load(Properties properties) {
        assert properties != null;
        String name = properties.getProperty(K_NAME);
        String command = properties.getProperty(K_COMMAND);
        String profile = properties.getProperty(K_PROFILE);
        properties.remove(K_COMMAND);
        properties.remove(K_PROFILE);

        Map<String, String> env = new HashMap<String, String>();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            if ((entry.getKey() instanceof String) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "不正なキー{0}が含まれています ({1})",
                        entry.getKey(),
                        entry.getKey().getClass().getName()));
            }
            if ((entry.getValue() instanceof String) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "不正な値{0}が含まれています ({1})",
                        entry.getValue(),
                        entry.getValue().getClass().getName()));
            }
            String key = (String) entry.getKey();
            if (key.startsWith(K_ENVIRONMENT_PREFIX) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "不正なキー{0}が含まれています (\"{1}\" が先頭にありません)",
                        entry.getKey(),
                        K_ENVIRONMENT_PREFIX));
            }
            key = key.substring(K_ENVIRONMENT_PREFIX.length());
            String value = (String) entry.getValue();
            env.put(key, value);
        }
        return new ScriptWorkDescription(name, command, profile, env);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + command.hashCode();
        result = prime * result + profileName.hashCode();
        result = prime * result + variables.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ScriptWorkDescription other = (ScriptWorkDescription) obj;
        if (name.equals(other.name) == false) {
            return false;
        }
        if (command.equals(other.command) == false) {
            return false;
        }
        if (profileName.equals(other.profileName) == false) {
            return false;
        }
        if (variables.equals(other.variables) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Script({0})@{1}", getCommand(), getProfileName());
    }
}
