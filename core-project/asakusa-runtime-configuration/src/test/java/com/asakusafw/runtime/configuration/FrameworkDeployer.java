/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.configuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;

/**
 * Deploys mock framework environment.
 */
public class FrameworkDeployer implements TestRule {

    static final Logger LOG = LoggerFactory.getLogger(FrameworkDeployer.class);

    private static final AtomicReference<Reference<byte[]>> BOOTSTRAP_JAR_CACHE =
            new AtomicReference<Reference<byte[]>>();

    final TemporaryFolder folder = new TemporaryFolder();

    final boolean copyDefaults;

    private File home;

    private File work;

    private File bootstrapJar;

    private final List<File> runtimeJars = new ArrayList<File>();

    /**
     * Creates a new instance with creating default layout.
     */
    public FrameworkDeployer() {
        this(true);
    }

    /**
     * Creates a new instance.
     * @param copyDefaults creates default layout
     */
    public FrameworkDeployer(boolean copyDefaults) {
        this.copyDefaults = copyDefaults;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                folder.create();
                try {
                    createDirs();
                    if (copyDefaults) {
                        deployMainDist();
                        deployTestDist();
                        deployRuntimeLibrary();
                    }
                    deploy();
                    base.evaluate();
                } finally {
                    folder.delete();
                }
            }
        };
    }

    void createDirs() throws IOException {
        home = folder.newFolder("home");
        work = folder.newFolder("work");
    }

    /**
     * Deploys project-specific configurations.
     * @throws Throwable if failed to deploy
     */
    protected void deploy() throws Throwable {
        return;
    }

    void deployMainDist() throws IOException {
        LOG.debug("Deploying src/main/dist");
        File source = new File("src/main/dist");
        if (source.exists()) {
            copy(source, getHome());
        } else {
            LOG.debug("There is no prod distribution directory: {}", source.getAbsolutePath());
        }
    }

    void deployTestDist() throws IOException {
        LOG.debug("Deploying src/test/dist");
        File source = new File("src/test/dist");
        if (source.exists()) {
            copy(source, getHome());
        } else {
            LOG.debug("There is no test distribution directory: {}", source.getAbsolutePath());
        }
    }

    void deployRuntimeLibrary() throws IOException {
        LOG.debug("Deploying runtime library");
        Reference<byte[]> ref = BOOTSTRAP_JAR_CACHE.get();
        byte[] cached = ref == null ? null : ref.get();
        if (cached != null) {
            bootstrapJar = dump(
                    new ByteArrayInputStream(cached),
                    toFrameworkFile("core/lib/asakusa-runtime-all.jar"));
        } else {
            bootstrapJar = deployFatLibrary("core/lib/asakusa-runtime-all.jar",
                    ApplicationLauncher.class,
                    Snappy.class);
            int length = (int) bootstrapJar.length();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(Math.min(1024, length));
            try {
                InputStream in = new FileInputStream(bootstrapJar);
                try {
                    copyStream(in, buffer);
                } finally {
                    in.close();
                }
            } finally {
                buffer.close();
            }
            BOOTSTRAP_JAR_CACHE.set(new SoftReference<byte[]>(buffer.toByteArray()));
        }
    }

    private File deployFatLibrary(String targetPath, Class<?>... classes) throws IOException {
        if (classes.length == 1) {
            return deployLibrary(classes[0], targetPath);
        }
        List<File> paths = new ArrayList<File>();
        for (Class<?> aClass : classes) {
            File path = findLibraryPathFromClass(aClass);
            if (path == null) {
                throw new IOException(MessageFormat.format(
                        "Failed to detect library archive for \"{0}\"",
                        aClass.getName()));
            }
            paths.add(path);
        }
        File target = toFrameworkFile(targetPath);
        deployFatLibrary(paths, target);
        return target;
    }

    private void deployFatLibrary(List<File> paths, File target) throws IOException {
        prepareParent(target);
        Set<String> saw = new HashSet<String>();
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target)));
        try {
            for (File path : paths) {
                if (path.isDirectory()) {
                    putEntry(zip, path, null, saw);
                } else {
                    mergeEntries(zip, path, saw);
                }
            }
        } finally {
            zip.close();
        }
    }

    private void mergeEntries(ZipOutputStream zip, File file, Set<String> saw) throws IOException {
        ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        try {
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
        } finally {
            in.close();
        }
    }

    /**
     * Deploys class library archive into target path.
     * @param memberClass a member class which the target library includes in
     * @param targetPath target relative path from {@link #getHome()}
     * @return deployed file
     * @throws IOException if failed to deploy
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File deployLibrary(Class<?> memberClass, String targetPath) throws IOException {
        File archive = findLibraryPathFromClass(memberClass);
        if (archive == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to detect library archive for \"{0}\"",
                    targetPath));
        }
        File target = toFrameworkFile(targetPath);
        deployLibrary(archive, target);
        return target;
    }

    private File toFrameworkFile(String targetPath) {
        return new File(getHome(), targetPath);
    }

    /**
     * Find library file/directory from an element class.
     * @param aClass element class in target library
     * @return target file/directory, or {@code null} if not found
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File findLibraryPathFromClass(Class<?> aClass) {
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

    private File toClassPathRoot(Class<?> aClass, File classFile) {
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

    private File toClassPathRoot(Class<?> aClass, String uriQualifiedPath) {
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

    /**
     * Deploys source archive/directory onto the target jar file.
     * @param source source archive/directory
     * @param target target jar file
     * @return deployed
     * @throws IOException if failed to put
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #deployLibrary(Class, String)
     */
    public File deployLibrary(File source, File target) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        if (source.isFile()) {
            copy(source, target);
        } else {
            LOG.debug("Package into archive: {} -> {}", source, target);
            prepareParent(target);
            OutputStream output = new BufferedOutputStream(new FileOutputStream(target));
            try {
                ZipOutputStream zip = new ZipOutputStream(output);
                putEntry(zip, source, null, new HashSet<String>());
                zip.close();
            } finally {
                output.close();
            }
        }
        return target;
    }

    private void putEntry(
            ZipOutputStream zip,
            File source, String path,
            Set<String> saw) throws IOException {
        assert zip != null;
        assert source != null;
        assert !(source.isFile() && path == null);
        if (source.isDirectory()) {
            for (File child : source.listFiles()) {
                String next = (path == null) ? child.getName() : path + '/' + child.getName();
                putEntry(zip, child, next, saw);
            }
        } else {
            if (saw.contains(path)) {
                return;
            }
            saw.add(path);
            zip.putNextEntry(new ZipEntry(path));
            InputStream in = new BufferedInputStream(new FileInputStream(source));
            try {
                LOG.trace("Copy into archive: {} -> {}", source, path);
                copyStream(in, zip);
            } finally {
                in.close();
            }
            zip.closeEntry();
        }
    }

    /**
     * Copies a file/directory into target.
     * @param source source file/directory
     * @param target target path
     * @return same as {@code target}
     * @throws IOException if failed to copy
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File copy(File source, File target) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        if (source.isDirectory()) {
            for (File child : source.listFiles()) {
                copy(child, new File(target, child.getName()));
            }
        } else {
            copyFile(source, target);
        }
        return target;
    }

    /**
     * Copies an input stram into target file.
     * @param input input stream
     * @param target target path
     * @return same as {@code target}
     * @throws IOException if failed to copy
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File dump(InputStream input, File target) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        prepareParent(target);
        OutputStream output = new BufferedOutputStream(new FileOutputStream(target));
        try {
            copyStream(input, output);
        } finally {
            output.close();
        }
        return target;
    }

    private void copyFile(File source, File target) throws FileNotFoundException, IOException {
        assert source != null;
        assert target != null;
        InputStream input = new BufferedInputStream(new FileInputStream(source));
        try {
            prepareParent(target);
            OutputStream output = new BufferedOutputStream(new FileOutputStream(target));
            try {
                copyStream(input, output);
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
        if (source.canExecute()) {
            target.setExecutable(true);
        }
    }

    private void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buf = new byte[512];
        while (true) {
            int read = input.read(buf);
            if (read < 0) {
                break;
            }
            output.write(buf, 0, read);
        }
    }

    private void prepareParent(File target) throws IOException {
        assert target != null;
        if (target.getParentFile().isDirectory() == false && target.getParentFile().mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to copy into {0} (cannot create target directory)",
                    target));
        }
    }

    /**
     * Extracts ZIP archive into target directory.
     * @param archive ZIP archive
     * @param target target directory
     * @return same as {@code target}
     * @throws IOException if failed
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File extract(File archive, File target) throws IOException {
        if (archive == null) {
            throw new IllegalArgumentException("archive must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        InputStream input = new BufferedInputStream(new FileInputStream(target));
        try {
            ZipInputStream zip = new ZipInputStream(input);
            extract(zip, target);
        } finally {
            input.close();
        }
        return target;
    }

    /**
     * Extracts ZIP archive into target directory.
     * @param input ZIP archive
     * @param target target directory
     * @return same as {@code target}
     * @throws IOException if failed to extract archive into target
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public File extract(ZipInputStream input, File target) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        while (true) {
            ZipEntry entry = input.getNextEntry();
            if (entry == null) {
                break;
            }
            if (entry.isDirectory()) {
                continue;
            }
            File file = new File(target, entry.getName());
            dump(input, file);
        }
        return target;
    }

    /**
     * Returns path to framework deployed.
     * @return framework path
     */
    public File getHome() {
        return home;
    }

    /**
     * Returns path to temporary work directory.
     * @param child the child name
     * @return work directory
     */
    public File getWork(String child) {
        File dir = new File(work, child);
        if (dir.mkdirs() == false && dir.isDirectory() == false) {
            LOG.warn("Failed to create directory: {}", dir.getAbsolutePath());
        }
        return dir;
    }

    /**
     * Returns the path to the core runtime library.
     * @return the path, or {@code null} if not put
     */
    public File getCoreRuntimeLibrary() {
        return bootstrapJar;
    }

    /**
     * Returns the paths to the runtime libraries (except the core library).
     * @return the paths
     */
    public List<File> getRuntimeLibraries() {
        return Collections.unmodifiableList(runtimeJars);
    }

    /**
     * Returns the path to the core configuration file.
     * @return the path, or {@code null} if not put
     */
    public File getCoreConfigurationFile() {
        File conf = new File(getHome(), "core/conf/asakusa-resources.xml");
        if (conf.exists()) {
            return conf;
        }
        return null;
    }
}
