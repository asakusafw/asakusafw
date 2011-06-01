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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.rules.MethodRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.runtime.stage.ToolLauncher;

/**
 * Deploys mock framework environment.
 * @since 0.2.0
 */
public class FrameworkDeployer implements MethodRule {

    static final Logger LOG = LoggerFactory.getLogger(FrameworkDeployer.class);

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
        File source = new File("src/main/dist", TestDriverContext.SUBMIT_JOB_SCRIPT);
        File target = new File(getFrameworkHome(), TestDriverContext.SUBMIT_JOB_SCRIPT);
        copy(source, target);
        target.setExecutable(true);
    }

    void deployRuntimeLibrary() throws IOException {
        LOG.debug("Deploying runtime library");
        File runtime = DirectFlowCompiler.toLibraryPath(ToolLauncher.class);
        if (runtime == null) {
            throw new IOException("Failed to detect runtime library");
        }
        deployLibrary(runtime, new File(getFrameworkHome(), "core/lib/asakusa-runtime.jar"));
    }

    private void deployLibrary(File source, File target) throws IOException {
        assert source != null;
        assert target != null;
        if (source.isFile()) {
            copy(source, target);
        } else {
            LOG.debug("Package into archive: {} -> {}", source, target);
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
                LOG.debug("Copy into archive: {} -> {}", source, path);
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
        InputStream input = new FileInputStream(source);
        try {
            prepareParent(target);
            OutputStream output = new FileOutputStream(target);
            try {
                copy(input, output);
            } finally {
                output.close();
            }
        } finally {
            input.close();
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
                    "Failed to copy into {1} (cannot create target directory)",
                    target));
        }
    }

    /**
     * Returns path to framework deployed.
     * @return framework path
     */
    public File getFrameworkHome() {
        return folder.getRoot();
    }
}
