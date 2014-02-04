/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.compiler.util.tester;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.MultipleModelInput;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.ToolLauncher;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.utils.collections.Lists;

/**
 * A driver for control Hadoop jobs for testing.
 */
public final class HadoopDriver implements Closeable {

    static final Logger LOG = LoggerFactory.getLogger(HadoopDriver.class);

    private volatile Logger logger;

    /**
     * Cluster working directory.
     */
    public static final String RUNTIME_WORK_ROOT = "target/testing";

    private final File command;

    private final Configuration configuration;

    private HadoopDriver() {
        this.command = ConfigurationProvider.findHadoopCommand();
        this.configuration = new ConfigurationProvider().newInstance();
        this.logger = LOG;
    }

    /**
     * Changes current logger.
     * @param logger the logger to set
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void setLogger(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("logger must not be null"); //$NON-NLS-1$
        }
        this.logger = logger;
    }

    /**
     * Returns the current configuration.
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 指定のセグメント一覧をHadoopが認識するファイルシステムのパスに変換して返す。
     * @param segments 対象のセグメント一覧
     * @return ファイルシステムのパス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Location toPath(String...segments) {
        if (segments == null) {
            throw new IllegalArgumentException("segments must not be null"); //$NON-NLS-1$
        }
        Location path = Location.fromPath(RUNTIME_WORK_ROOT, '/');
        for (String segment : segments) {
            path = path.append(segment);
        }
        return path;
    }

    /**
     * インスタンスを生成する。
     * @return 生成したインスタンス
     */
    public static HadoopDriver createInstance() {
        return new HadoopDriver();
    }

    /**
     * 指定のディレクトリパスの内容をすべて含めた入力を返す。
     * @param <T> モデルの種類
     * @param modelType モデルの種類
     * @param location 対象のパス
     * @return 入力
     * @throws IOException 情報の取得に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T extends Writable> ModelInput<T> openInput(
            Class<T> modelType,
            final Location location) throws IOException {
        if (modelType == null) {
            throw new IllegalArgumentException("modelType must not be null"); //$NON-NLS-1$
        }
        if (location == null) {
            throw new IllegalArgumentException("location must not be null"); //$NON-NLS-1$
        }
        final File temp = createTempFile(modelType);
        if (temp.delete() == false) {
            logger.debug("Failed to delete a placeholder file: {}", temp);
        }
        if (temp.mkdirs() == false) {
            throw new IOException(temp.getAbsolutePath());
        }
        copyFromHadoop(location.toPath('/'), temp);

        List<ModelInput<T>> sources = Lists.create();
        if (location.isPrefix()) {
            for (File file : temp.listFiles()) {
                if (file.isFile() && file.getName().startsWith("_") == false) {
                    sources.add(TemporaryStorage.openInput(configuration, modelType, new Path(file.toURI())));
                }
            }
        } else {
            for (File folder : temp.listFiles()) {
                for (File file : folder.listFiles()) {
                    if (file.isFile() && file.getName().startsWith("_") == false) {
                        sources.add(TemporaryStorage.openInput(configuration, modelType, new Path(file.toURI())));
                    }
                }
            }
        }
        return new MultipleModelInput<T>(sources) {
            final AtomicBoolean closed = new AtomicBoolean();
            @Override
            public void close() throws IOException {
                if (closed.compareAndSet(false, true) == false) {
                    return;
                }
                super.close();
                onInputCompleted(temp, location);
            }
        };
    }

    /**
     * 指定のパスにモデルの内容を出力する。
     * @param <T> モデルの種類
     * @param modelType モデルの種類
     * @param path 出力先のファイルパス
     * @return 出力
     * @throws IOException 情報の取得に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T extends Writable> ModelOutput<T> openOutput(
            Class<T> modelType,
            final Location path) throws IOException {
        return TemporaryStorage.openOutput(
                configuration,
                modelType,
                new Path(path.toPath('/')));
    }

    private <T> File createTempFile(Class<T> modelType) throws IOException {
        assert modelType != null;
        return File.createTempFile(
                modelType.getSimpleName() + "_",
                ".seq");
    }

    void onInputCompleted(File temp, Location path) {
        assert temp != null;
        assert path != null;
        logger.debug("Input completed: {} -> {}", path, temp);
        if (delete(temp) == false) {
            logger.warn("Failed to delete temporary file: {}", temp);
        }
    }

    private boolean delete(File temp) {
        boolean success = true;
        if (temp.isDirectory()) {
            for (File child : temp.listFiles()) {
                success &= delete(child);
            }
        }
        success &= temp.delete();
        return success;
    }

    /**
     * Executes the target job using {@link ToolLauncher}.
     * @param runtimeLib core runtime library
     * @param className target class name
     * @param libjars extra libraries
     * @param conf configuration file (nullable)
     * @param properties hadoop properties
     * @return {@code true} if succeeded, otherwise {@code false}
     * @throws IOException if failed to execute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean runJob(
            File runtimeLib,
            List<File> libjars,
            String className,
            File conf,
            Map<String, String> properties) throws IOException {
        if (runtimeLib == null) {
            throw new IllegalArgumentException("runtime must not be null"); //$NON-NLS-1$
        }
        if (className == null) {
            throw new IllegalArgumentException("className must not be null"); //$NON-NLS-1$
        }
        if (libjars == null) {
            throw new IllegalArgumentException("libjars must not be null"); //$NON-NLS-1$
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        logger.info("run {} with {}", className, libjars);
        List<String> arguments = Lists.create();
        arguments.add("jar");
        arguments.add(runtimeLib.getAbsolutePath());
        arguments.add(ToolLauncher.class.getName());
        arguments.add(className);

        if (libjars.isEmpty() == false) {
            StringBuilder buf = new StringBuilder();
            for (File f : libjars) {
                buf.append(f.getAbsolutePath());
                buf.append(",");
            }
            buf.deleteCharAt(buf.length() - 1);
            arguments.add("-libjars");
            arguments.add(buf.toString());
        }

        if (conf != null) {
            arguments.add("-conf");
            arguments.add(conf.getCanonicalPath());
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            arguments.add("-D");
            arguments.add(MessageFormat.format("{0}={1}", entry.getKey(), entry.getValue()));
        }

        int ret = invoke(arguments.toArray(new String[arguments.size()]));
        if (ret != 0) {
            logger.info("running {} returned {}", className, ret);
            return false;
        }
        return true;
    }

    private void copyFromHadoop(String source, File destination) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (destination == null) {
            throw new IllegalArgumentException("destination must not be null"); //$NON-NLS-1$
        }
        logger.info("copy {} to {}", source, destination);
        int ret = invoke("fs", "-get", source, destination.getAbsolutePath());
        if (ret != 0) {
            throw new IOException(MessageFormat.format(
                    "Failed to fs -get: result={0}, source={1}, destination={2}",
                    String.valueOf(ret),
                    source,
                    destination.getAbsolutePath()));
        }
    }

    /**
     * クラスタ上のユーザーディレクトリ以下のリソースを削除する。
     * @throws IOException 起動に失敗した場合
     */
    public void clean() throws IOException {
        logger.info("clean user directory");
        int ret = invoke("fs", "-rmr", toPath().toPath('/'));
        if (ret != 0) {
            logger.info(MessageFormat.format(
                    "Failed to fs -rmr {0}: result={1}",
                    toPath(),
                    String.valueOf(ret)));
        }
    }

    private int invoke(String... arguments) throws IOException {
        String hadoop = getHadoopCommand();
        List<String> commands = Lists.create();
        commands.add(hadoop);
        Collections.addAll(commands, arguments);

        logger.info("invoke: {}", commands);
        ProcessBuilder builder = new ProcessBuilder()
            .command(commands)
            .redirectErrorStream(true);
        Process process = builder.start();
        try {
            InputStream stream = process.getInputStream();
            Scanner scanner = new Scanner(stream);
            while (scanner.hasNextLine()) {
                logger.info(scanner.nextLine());
            }
            scanner.close();
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        } finally {
            process.destroy();
        }
    }

    private String getHadoopCommand() {
        return command.getAbsolutePath();
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
