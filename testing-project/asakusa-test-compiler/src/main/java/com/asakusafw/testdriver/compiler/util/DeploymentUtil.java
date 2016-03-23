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
package com.asakusafw.testdriver.compiler.util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for deployment.
 * @since 0.8.0
 */
public final class DeploymentUtil {

    static final Logger LOG = LoggerFactory.getLogger(DeploymentUtil.class);

    /**
     * Deploys an artifact onto the directory.
     * @param source the source file/directory
     * @param destination the target path
     * @param options the deployment options
     * @throws IOException if I/O error was occurred
     */
    public static void deploy(File source, File destination, DeployOption... options) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(options);
        Set<DeployOption> opts = EnumSet.noneOf(DeployOption.class);
        Collections.addAll(opts, options);
        if (opts.contains(DeployOption.DELETE_SOURCE)) {
            move(source, destination);
        } else {
            copy(source, destination);
        }
    }

    /**
     * Deploys an artifact onto the directory.
     * @param source the source file/directory
     * @param destination the target directory
     * @param options the operation options
     * @throws IOException if I/O error was occurred
     */
    public static void deployToDirectory(File source, File destination, DeployOption... options) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(options);
        deploy(source, new File(destination, source.getName()), options);
    }

    /**
     * Deletes the target file/directory.
     * @param target the target file/directory
     * @param options the operation options
     * @return {@code true} if the target file/directory was successfully deleted (or does not exist initially),
     *     otherwise {@code false}
     * @throws IOException if I/O error was occurred
     */
    public static boolean delete(File target, DeleteOption... options) throws IOException {
        if (target.exists() == false) {
            return true;
        }
        Set<DeleteOption> opts = EnumSet.noneOf(DeleteOption.class);
        Collections.addAll(opts, options);
        if (opts.contains(DeleteOption.QUIET)) {
            return FileUtils.deleteQuietly(target);
        } else {
            FileUtils.forceDelete(target);
            return true;
        }
    }

    private static void copy(File source, File destination) throws IOException {
        prepareDestination(destination);
        doCopy(source, destination);
    }

    private static void move(File source, File destination) throws IOException {
        prepareDestination(destination);
        boolean success = source.renameTo(destination);
        if (success == false) {
            doCopy(source, destination);
            if (FileUtils.deleteQuietly(source) == false) {
                LOG.warn(MessageFormat.format(
                        "failed to delete moving original file: {0}",
                        source));
            }
        }
    }

    private static void prepareDestination(File destination) throws IOException {
        if (destination.exists()) {
            FileUtils.forceDelete(destination);
        }
        File parent = destination.getAbsoluteFile().getParentFile();
        if (parent.mkdirs() == false && parent.exists() == false) {
            throw new IOException(MessageFormat.format(
                    "failed to create directory: {0}",
                    destination));
        }
    }

    private static void doCopy(File source, File destination) throws IOException {
        if (source.isFile()) {
            FileUtils.copyFile(source, destination, false);
        } else {
            FileUtils.copyDirectory(source, destination, false);
        }
    }

    /**
     * The deleting options.
     * @since 0.8.0
     */
    public enum DeleteOption {

        /**
         * Suppress exceptions even if the operation was failed.
         */
        QUIET,
    }

    /**
     * The deployment options.
     * @since 0.8.0
     */
    public enum DeployOption {

        /**
         * Deletes the original source file/directory.
         */
        DELETE_SOURCE,
    }

    private DeploymentUtil() {
        return;
    }
}
