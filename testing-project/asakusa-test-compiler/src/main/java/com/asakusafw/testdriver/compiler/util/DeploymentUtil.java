/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

    /**
     * Find library file/directory from an element class.
     * @param aClass element class in target library
     * @return target file/directory, or {@code null} if not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static File findLibraryPathFromClass(Class<?> aClass) {
        if (aClass == null) {
            throw new IllegalArgumentException("aClass must not be null"); //$NON-NLS-1$
        }
        int start = aClass.getName().lastIndexOf('.') + 1;
        String name = aClass.getName().substring(start);
        URL resource = aClass.getResource(name + ".class");
        if (resource == null) {
            LOG.warn("Failed to locate the class file: {}", aClass.getName());
            return null;
        }
        String protocol = resource.getProtocol();
        if (protocol.equals("file")) {
            try {
                File file = new File(resource.toURI());
                return toClassPathRoot(aClass, file);
            } catch (URISyntaxException e) {
                LOG.warn(MessageFormat.format(
                        "Failed to locate the library path (cannot convert to local file): {0}",
                        resource), e);
                return null;
            }
        }
        if (protocol.equals("jar")) {
            String path = resource.getPath();
            return toClassPathRoot(aClass, path);
        } else {
            LOG.warn("Failed to locate the library path (unsupported protocol {}): {}",
                    resource,
                    aClass.getName());
            return null;
        }
    }

    private static File toClassPathRoot(Class<?> aClass, File classFile) {
        assert aClass != null;
        assert classFile != null;
        assert classFile.isFile();
        String name = aClass.getName();
        File current = classFile.getParentFile();
        assert current != null && current.isDirectory() : classFile;
        for (int i = name.indexOf('.'); i >= 0; i = name.indexOf('.', i + 1)) {
            current = current.getParentFile();
            assert current != null && current.isDirectory() : classFile;
        }
        return current;
    }

    private static File toClassPathRoot(Class<?> aClass, String uriQualifiedPath) {
        assert aClass != null;
        assert uriQualifiedPath != null;
        int entry = uriQualifiedPath.lastIndexOf('!');
        String qualifier;
        if (entry >= 0) {
            qualifier = uriQualifiedPath.substring(0, entry);
        } else {
            qualifier = uriQualifiedPath;
        }
        URI archive;
        try {
            archive = new URI(qualifier);
        } catch (URISyntaxException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to locate the JAR library file {}: {}",
                    qualifier,
                    aClass.getName()),
                    e);
            throw new UnsupportedOperationException(qualifier, e);
        }
        if (archive.getScheme().equals("file") == false) {
            LOG.warn("Failed to locate the library path (unsupported protocol {}): {}",
                    archive,
                    aClass.getName());
            return null;
        }
        File file = new File(archive);
        assert file.isFile() : file;
        return file;
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
     * Creates a fat jar from the classpath entries.
     * @param entries the classpath entries
     * @param target the target fat jar path
     * @throws IOException if I/O error was occurred while building the target jar
     */
    public static void buildFatJar(List<File> entries, File target) throws IOException {
        if (target.getParentFile().isDirectory() == false && target.getParentFile().mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to copy into {0} (cannot create target directory)",
                    target));
        }
        Set<String> saw = new HashSet<>();
        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target)))) {
            for (File path : entries) {
                if (path.isDirectory()) {
                    putEntry(zip, path, null, saw);
                } else {
                    mergeEntries(zip, path, saw);
                }
            }
        }
    }

    private static void putEntry(
            ZipOutputStream zip,
            File source, String path,
            Set<String> saw) throws IOException {
        assert zip != null;
        assert source != null;
        assert !(source.isFile() && path == null);
        if (source.isDirectory()) {
            for (File child : list(source)) {
                String next = (path == null) ? child.getName() : path + '/' + child.getName();
                putEntry(zip, child, next, saw);
            }
        } else {
            if (saw.contains(path)) {
                return;
            }
            saw.add(path);
            zip.putNextEntry(new ZipEntry(path));
            try (InputStream in = new BufferedInputStream(new FileInputStream(source))) {
                LOG.trace("Copy into archive: {} -> {}", source, path);
                copyStream(in, zip);
            }
            zip.closeEntry();
        }
    }

    private static List<File> list(File file) {
        return Optional.ofNullable(file.listFiles())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    private static void mergeEntries(ZipOutputStream zip, File file, Set<String> saw) throws IOException {
        try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            while (true) {
                ZipEntry entry = in.getNextEntry();
                if (entry == null) {
                    break;
                }
                if (saw.contains(entry.getName())) {
                    continue;
                }
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Copy into archive: {} -> {}",
                            entry.getName(),
                            file);
                }
                saw.add(entry.getName());
                zip.putNextEntry(new ZipEntry(entry.getName()));
                copyStream(in, zip);
            }
        }
    }

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        IOUtils.copyLarge(input, output);
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
