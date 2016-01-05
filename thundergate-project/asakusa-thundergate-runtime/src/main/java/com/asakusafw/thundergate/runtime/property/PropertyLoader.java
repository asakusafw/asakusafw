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
package com.asakusafw.thundergate.runtime.property;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Stores and loads properties for ThunderGate script.
 * @since 0.2.5
 */
public class PropertyLoader implements Closeable {

    /**
     * インポーターが格納されているパス。
     */
    private static final String IMPORTER_PATH = "META-INF/bulkloader/{0}-importer.properties";

    /**
     * エクスポーターが格納されているパス。
     */
    private static final String EXPORTER_PATH = "META-INF/bulkloader/{0}-exporter.properties";

    private final ZipFile zip;

    private final String targetName;

    /**
     * インスタンスを生成する。
     * @param file プロパティを含むZIP/JARファイル
     * @param targetName プロパティの対象名
     * @throws IOException ZIP/JARファイルの分析に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public PropertyLoader(File file, String targetName) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null"); //$NON-NLS-1$
        }
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        this.zip = new ZipFile(file);
        this.targetName = targetName;
    }

    @Override
    public void close() throws IOException {
        zip.close();
    }

    /**
     * 対象のアーカイブに登録されたインポーターの設定内容を返す。
     * @return インポーターの設定内容
     * @throws IOException 設定の読み出しに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Properties loadImporterProperties() throws IOException {
        String path = getImporterPropertiesPath(targetName);
        return loadProperties(path);
    }

    /**
     * 対象のアーカイブに登録されたエクスポーターの設定内容を返す。
     * @return エクスポーターの設定内容
     * @throws IOException 設定の読み出しに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Properties loadExporterProperties() throws IOException {
        String path = getExporterPropertiesPath(targetName);
        return loadProperties(path);
    }

    /**
     * インポーターの設定が配置されるパスを返す。
     * @param targetName プロパティの対象名
     * @return インポーターの設定が配置されるパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getImporterPropertiesPath(String targetName) {
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        String path = resolvePath(IMPORTER_PATH, targetName);
        return path;
    }

    /**
     * エクスポーターの設定が配置されるパスを返す。
     * @param targetName プロパティの対象名
     * @return エクスポーターの設定が配置されるパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static String getExporterPropertiesPath(String targetName) {
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        String path = resolvePath(EXPORTER_PATH, targetName);
        return path;
    }

    /**
     * 対象のアーカイブにインポーターの設定内容を書き出す。
     * @param output 書き出す先のアーカイブ
     * @param targetName プロパティの対象名
     * @param properties 書き出す設定内容
     * @throws IOException 書き出しに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void saveImporterProperties(
            ZipOutputStream output,
            String targetName,
            Properties properties) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        String path = getImporterPropertiesPath(targetName);
        saveProperties(path, output, properties);
    }

    /**
     * 対象のアーカイブにエクスポーターの設定内容を書き出す。
     * @param output 書き出す先のアーカイブ
     * @param targetName プロパティの対象名
     * @param properties 書き出す設定内容
     * @throws IOException 書き出しに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static void saveExporterProperties(
            ZipOutputStream output,
            String targetName,
            Properties properties) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        String path = getExporterPropertiesPath(targetName);
        saveProperties(path, output, properties);
    }

    private static String resolvePath(String path, String targetName) {
        assert path != null;
        assert targetName != null;
        return MessageFormat.format(path, targetName);
    }

    private static void saveProperties(
            String path,
            ZipOutputStream target,
            Properties properties) throws IOException {
        assert path != null;
        assert target != null;
        assert properties != null;
        ZipEntry entry = new ZipEntry(path);
        target.putNextEntry(entry);
        properties.store(target, path);
    }

    private Properties loadProperties(String path) throws IOException {
        assert path != null;
        Properties result = new Properties();
        InputStream input = open(path);
        try {
            result.load(input);
        } finally {
            input.close();
        }
        return result;
    }

    private InputStream open(String path) throws IOException {
        assert path != null;
        ZipEntry entry = zip.getEntry(path);
        if (entry == null) {
            throw new FileNotFoundException(path);
        }
        return zip.getInputStream(entry);
    }
}
