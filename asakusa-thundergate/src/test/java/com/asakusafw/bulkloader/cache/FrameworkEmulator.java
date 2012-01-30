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
package com.asakusafw.bulkloader.cache;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.MethodRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.ProcessFileListProvider;
import com.asakusafw.runtime.stage.ToolLauncher;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;

/**
 * Deploys mock framework environment.
 */
public class FrameworkEmulator implements MethodRule {

    static final Log LOG = LogFactory.getLog(FrameworkEmulator.class);

    final TemporaryFolder folder = new TemporaryFolder();

    @Override
    public Statement apply(final Statement base, FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                folder.create();
                try {
                    deploySubmitScript();
                    deployRuntimeLibrary();
                    base.evaluate();
                } finally {
                    folder.delete();
                }
            }
        };
    }

    void deploySubmitScript() throws IOException {
        LOG.debug("Deploying submit script");
        File source = new File("src/main/dist");
        File target = getFrameworkHome();
        copy(source, target);
    }

    void deployRuntimeLibrary() throws IOException {
        LOG.debug("Deploying runtime library");
        deployLibrary(ToolLauncher.class, "core/lib/asakusa-runtime.jar");
        deployLibrary(CacheInfo.class, "core/lib/asakusa-thundergate-runtime.jar");
    }

    /**
     * Deploys a library onto framework directory.
     * @param libraryMember a class declared in the target library
     * @param path target path (relative from framework home)
     * @throws IOException if failed to deploy the library
     */
    public void deployLibrary(Class<?> libraryMember, String path) throws IOException {
        File library = findLibraryPathFromClass(libraryMember);
        if (library == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to detect library: {0}",
                    path));
        }
        deployLibrary(library, new File(getFrameworkHome(), path));
    }

    private File findLibraryPathFromClass(Class<?> aClass) {
        assert aClass != null;
        int start = aClass.getName().lastIndexOf('.') + 1;
        String name = aClass.getName().substring(start);
        URL resource = aClass.getResource(name + ".class");
        if (resource == null) {
            LOG.warn(MessageFormat.format(
                    "Failed to locate the class file: {0}",
                    aClass.getName()));
            return null;
        }
        String protocol = resource.getProtocol();
        if (protocol.equals("file")) {
            File file = new File(resource.getPath());
            return toClassPathRoot(aClass, file);
        }
        if (protocol.equals("jar")) {
            String path = resource.getPath();
            return toClassPathRoot(aClass, path);
        } else {
            LOG.warn(MessageFormat.format(
                    "Failed to locate the library path (unsupported protocol {0}): {1}",
                    resource,
                    aClass.getName()));
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
                    "Failed to locate the JAR library file {0}: {1}",
                    qualifier,
                    aClass.getName()),
                    e);
            throw new UnsupportedOperationException(qualifier, e);
        }
        if (archive.getScheme().equals("file") == false) {
            LOG.warn(MessageFormat.format(
                    "Failed to locate the library path (unsupported protocol {0}): {1}",
                    archive,
                    aClass.getName()));
            return null;
        }
        File file = new File(archive);
        assert file.isFile() : file;
        return file;
    }

    private void deployLibrary(File source, File target) throws IOException {
        assert source != null;
        assert target != null;
        if (source.isFile()) {
            copy(source, target);
        } else {
            LOG.debug(MessageFormat.format(
                    "Package into archive: {0} -> {1}",
                    source,
                    target));
            prepareParent(target);
            OutputStream output = new FileOutputStream(target);
            try {
                ZipOutputStream zip = new ZipOutputStream(output);
                putEntry(zip, source, null);
                zip.close();
            } finally {
                output.close();
            }
        }
    }

    private void putEntry(ZipOutputStream zip, File source, String path) throws IOException {
        assert zip != null;
        assert source != null;
        assert !(source.isFile() && path == null);
        if (source.isDirectory()) {
            for (File child : source.listFiles()) {
                String next = (path == null) ? child.getName() : path + '/' + child.getName();
                putEntry(zip, child, next);
            }
        } else {
            zip.putNextEntry(new ZipEntry(path));
            InputStream in = new FileInputStream(source);
            try {
                copy(in, zip);
            } finally {
                in.close();
            }
            zip.closeEntry();
        }
    }

    private void copy(File source, File target) throws IOException {
        assert source != null;
        assert target != null;
        prepareParent(target);
        if (source.isDirectory()) {
            target.mkdir();
            for (File childSource : source.listFiles()) {
                File childTarget = new File(target, childSource.getName());
                copy(childSource, childTarget);
            }
        } else {
            InputStream input = new FileInputStream(source);
            try {
                OutputStream output = new FileOutputStream(target);
                try {
                    copy(input, output);
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
    }

    private void copy(InputStream input, OutputStream output) throws IOException {
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
     * Executes framework command.
     * @param scriptPath the path to the command script (relative path from ASAKUSA_HOME)
     * @param arguments command line arguments
     * @return file list provider
     * @throws IOException if failed to execute command
     * @throws InterruptedException if interrupted
     */
    public FileListProvider execute(String scriptPath, String... arguments) throws IOException, InterruptedException {
        List<String> command = new ArrayList<String>();
        command.add(new File(getFrameworkHome(), scriptPath).getAbsolutePath());
        Collections.addAll(command, arguments);

        Map<String, String> env = new HashMap<String, String>();
        env.put("ASAKUSA_HOME", getFrameworkHome().getAbsolutePath());

        LOG.info(MessageFormat.format("Starting command: {0} ({1})", command, env));
        return new ProcessFileListProvider(command, env);
    }

    /**
     * Returns path to framework deployed.
     * @return framework path
     */
    public File getFrameworkHome() {
        return folder.getRoot();
    }
}
