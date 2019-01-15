/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.utils.gradle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a file bundle that have a single root directory.
 * @since 0.9.2
 */
public class Bundle {

    static final Logger LOG = LoggerFactory.getLogger(Bundle.class);

    private final ProjectContext context;

    private final Path directory;

    private final CommandPath commandPath;

    /**
     * Creates a new instance.
     * @param context the current context
     * @param root the bundle root directory
     */
    public Bundle(ProjectContext context, Path root) {
        this.context = context;
        this.directory = root;
        this.commandPath = new CommandPath(Collections.singletonList(directory));
    }

    /**
     * Returns the bundle directory.
     * @return the bundle directory
     */
    public Path getDirectory() {
        return directory;
    }

    /**
     * Copies files from the given directory into bundle root.
     * @param source the source directory
     * @return this
     * @see Paths#get(String, String...)
     */
    public Bundle copy(Path source) {
        if (Files.isRegularFile(source)) {
            return copy(source, getFileName(source));
        } else {
            return copy0(source, directory);
        }
    }

    /**
     * Copies files from the given file or directory onto the path.
     * @param source the source file or directory
     * @param onto the copy target path (relative from the bundle root)
     * @return this
     * @see Paths#get(String, String...)
     */
    public Bundle copy(Path source, String onto) {
        return copy0(source, directory.resolve(onto));
    }

    /**
     * Copies files from the given file or directory into bundle root.
     * This accepts paths in archive files, like {@code /path/to/file.jar/META-INF}.
     * It extracts only under {@code META-INF} in {@code /path/to/file.jar}.
     * @param source the source file or directory
     * @return this
     * @see Paths#get(String, String...)
     */
    public Bundle extract(Path source) {
        return extract0(source, directory);
    }

    /**
     * Copies files from the given file or directory onto the path.
     * @param source the source file or directory
     * @param onto the copy target path (relative from the bundle root)
     * @return this
     * @see Paths#get(String, String...)
     */
    public Bundle extract(Path source, String onto) {
        return extract0(source, directory.resolve(onto));
    }

    /**
     * Removes all files on this bundle excepts the content root directory.
     * @return this
     */
    public Bundle clean() {
        if (Files.exists(directory) == false) {
            return this;
        }
        LOG.debug("removing files on {}", directory);
        if (Files.isDirectory(directory)) {
            try {
                Files.list(directory).forEach(Bundle::delete);
            } catch (IOException e) {
                throw new IllegalStateException(MessageFormat.format(
                        "error occurred while deleting {0}",
                        directory), e);
            }
        }
        return this;
    }

    /**
     * Removes target resource on the bundle only if it exists.
     * If there is no such a resource on this bundle, this method will do nothing.
     * @param path  the target path
     * @return this
     */
    public Bundle clean(String path) {
        Path target = directory.resolve(path);
        if (Files.exists(target)) {
            delete(target);
        }
        return this;
    }

    private Bundle copy0(Path source, Path to) {
        Path from = source;
        LOG.debug("copy: {} -> {}", from, to);
        merge(from, to);
        return this;
    }

    private Bundle extract0(Path source, Path to) {
        Path origin = source.toAbsolutePath();
        Path archive = origin;
        while (Files.exists(archive) == false) {
            Path next = archive.getParent();
            if (next == null) {
                break;
            }
            archive = next;
        }
        if (Files.isRegularFile(archive) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "extract source must be a file: {0}",
                    source));
        }
        Path offset = archive.relativize(origin);
        LOG.debug("extract: {} -- {} -> {}", archive, offset, to);
        try (FileSystem fs = FileSystems.newFileSystem(archive, getClass().getClassLoader())) {
            boolean merged = false;
            for (Path root : fs.getRootDirectories()) {
                Path from = root;
                for (Path segment : offset) {
                    from = from.resolve(getFileName(segment));
                }
                if (Files.exists(from)) {
                    merge(from, to);
                    merged = true;
                }
            }
            if (merged == false) {
                throw new FileNotFoundException(MessageFormat.format(
                        "missing entry \"{0}\" in \"{1}\"",
                        archive, offset));
            }
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "error occurred while extracting {0}",
                    source), e);
        }
        return this;
    }

    private static String getFileName(Path path) {
        return Optional.ofNullable(path.getFileName())
                .map(Path::toString)
                .orElseThrow(() -> new IllegalStateException("empty path"));
    }

    private static void merge(Path from, Path to) {
        LOG.trace("merge: {}@{} -> {}", from, from.getFileSystem(), to);
        try {
            if (Files.isDirectory(from)) {
                Files.createDirectories(to);
                Files.list(from).forEach(f -> merge(f, to.resolve(getFileName(f))));
            } else {
                createParentDirectory(to);
                Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "failed to merge file: {0} -> {1}",
                    to), e);
        }
    }

    private static void delete(Path path) {
        LOG.debug("delete: {}", path);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    super.postVisitDirectory(dir, exc);
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "failed to delete file: {0}",
                    path), e);
        }
    }

    /**
     * Returns a file on this bundle.
     * @param path the relative path from the bundle root
     * @return the file, or {@code empty} if it is not found
     */
    public Optional<Path> find(String path) {
        return Optional.of(directory.resolve(path))
                .filter(Files::exists);
    }

    /**
     * Returns a file on the bundle.
     * @param path the relative path from the bundle root
     * @return the file, or {@code empty} if it is not found
     */
    public Path get(String path) {
        return find(path)
                .orElseThrow(() -> new IllegalStateException(String.format("%s is not found", path)));
    }

    /**
     * Creates a file.
     * @param path the relative path from the bundle root
     * @return the created file
     */
    public Path put(String path) {
        Path file = directory.resolve(path);
        try {
            createParentDirectory(file);
            Files.deleteIfExists(file);
            Files.createFile(file);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "error occurred while creating file: {0}",
                    path), e);
        }
    }

    private static void createParentDirectory(Path file) throws IOException {
        Path parent = Optional.ofNullable(file.toAbsolutePath().getParent())
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                        "failed to create the parent directory: {0}",
                        file)));
        Files.createDirectories(parent);
    }

    /**
     * Returns a file on this bundle.
     * @param path the relative path from the bundle root
     * @param configurator the configurator, which will be invoked only if the file exists
     * @return the file, or {@code empty} if it is not found
     */
    public Bundle find(String path, TryConsumer<? super Path, IOException> configurator) {
        find(path).ifPresent(it -> configure(it, configurator));
        return this;
    }

    /**
     * Process a file on the bundle.
     * @param path the relative path from the bundle root
     * @param configurator the file configurator
     * @return this
     */
    public Bundle get(String path, TryConsumer<? super Path, IOException> configurator) {
        return configure(get(path), configurator);
    }

    /**
     * Creates a file.
     * @param path the relative path from the bundle root
     * @param configurator consumes the created file
     * @return this
     */
    public Bundle put(String path, TryConsumer<? super Path, IOException> configurator) {
        return configure(put(path), configurator);
    }

    private <T> Bundle configure(T obj, TryConsumer<? super T, IOException> configurator) {
        try {
            configurator.accept(obj);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "error occurred while processing file: {0}",
                    obj), e);
        }
        return this;
    }

    /**
     * Launches the command.
     * @param command the command, relative from bundle root
     * @param arguments the command arguments
     * @return the exit status
     * @see BasicCommandLauncher
     */
    public int launch(String command, String... arguments) {
        Path cmd = commandPath.find(command)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                        "command not found: {0} ({1})",
                        command, commandPath.asPathString())));
        return context.getCommandLauncher().launch(cmd, Arrays.asList(arguments));
    }

    /**
     * Launches the command, or raise exception if the command failed.
     * @param command the command, relative from bundle root
     * @param arguments the command arguments
     * @return this
     * @see BasicCommandLauncher
     */
    public Bundle withLaunch(String command, String... arguments) {
        int status = launch(command, arguments);
        if (status != 0) {
            throw new IllegalStateException(MessageFormat.format(
                    "run with exit status={2}: command={0}, args={1}",
                    command,
                    Arrays.toString(arguments),
                    status));
        }
        return this;
    }
}
