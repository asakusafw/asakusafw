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
package com.asakusafw.compiler.flow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.batch.MockEmitter;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.jsr199.testing.VolatileClassFile;
import com.asakusafw.utils.java.jsr199.testing.VolatileClassOutputManager;
import com.asakusafw.utils.java.jsr199.testing.VolatileJavaFile;
import com.asakusafw.utils.java.jsr199.testing.VolatileResourceFile;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * メモリ上に構成物を展開するパッケージャ。
 */
public class VolatilePackager
        extends FlowCompilingEnvironment.Initialized
        implements Packager {

    static final Logger LOG = LoggerFactory.getLogger(VolatilePackager.class);

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final MockEmitter emitter;

    Map<String, byte[]> contents;

    /**
     * インスタンスを生成する。
     */
    public VolatilePackager() {
        this.emitter = new MockEmitter();
        this.contents = new TreeMap<String, byte[]>();
    }

    /**
     * このパッケージャが利用するエミッタを返す。
     * @return 利用するエミッタ
     */
    public MockEmitter getEmitter() {
        return emitter;
    }

    @Override
    public PrintWriter openWriter(CompilationUnit source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        return emitter.openFor(source);
    }

    @Override
    public OutputStream openStream(Name packageNameOrNull, String relativePath)
            throws IOException {
        if (relativePath == null) {
            throw new IllegalArgumentException("relativePath must not be null"); //$NON-NLS-1$
        }
        StringBuilder buf = new StringBuilder();
        if (packageNameOrNull != null) {
            buf.append(packageNameOrNull.toNameString().replace('.', '/'));
            buf.append('/');
        }
        buf.append(relativePath.replace('\\', '/'));
        final String name = buf.toString();
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                contents.put(name, toByteArray());
            }
        };
    }

    @Override
    public void build(OutputStream output) throws IOException {
        JarOutputStream jar = new JarOutputStream(output);
        try {
            compile(jar);
        } finally {
            jar.close();
        }
    }

    @Override
    public void packageSources(OutputStream output) throws IOException {
        JarOutputStream jar = new JarOutputStream(output);
        try {
            collect(jar);
        } finally {
            jar.close();
        }
    }

    private void collect(JarOutputStream jar) throws IOException {
        assert jar != null;
        for (VolatileJavaFile file : emitter.getEmitted()) {
            String path = file.toUri().getPath();
            JarEntry entry = new JarEntry(path);
            jar.putNextEntry(entry);
            jar.write(file.getCharContent(false).toString().getBytes(CHARSET));
            jar.closeEntry();
        }
    }

    private void compile(JarOutputStream jar) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException(
                    "この環境ではJavaコンパイラーを利用できません (JREにはコンパイラーが含まれていません)");
        }
        compile(compiler, jar);
    }

    private void compile(
            JavaCompiler compiler,
            JarOutputStream jar) throws IOException {
        assert compiler != null;
        assert jar != null;

        DiagnosticCollector<JavaFileObject> diagnostics =
            new DiagnosticCollector<JavaFileObject>();
        VolatileClassOutputManager fileManager = new VolatileClassOutputManager(
                compiler.getStandardFileManager(
                        diagnostics,
                        Locale.getDefault(),
                        CHARSET));
        try {
            List<String> arguments = Lists.create();
            Collections.addAll(arguments, "-source", "1.6");
            Collections.addAll(arguments, "-target", "1.6");
            Collections.addAll(arguments, "-encoding", CHARSET.name());

            StringWriter errors = new StringWriter();
            PrintWriter pw = new PrintWriter(errors);

            CompilationTask task = compiler.getTask(
                    pw,
                    fileManager,
                    diagnostics,
                    arguments,
                    Collections.<String>emptyList(),
                    emitter.getEmitted());

            Boolean successed = task.call();
            pw.close();
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                switch (diagnostic.getKind()) {
                case ERROR:
                case MANDATORY_WARNING:
                    getEnvironment().error(diagnostic.getMessage(null));
                    break;
                case WARNING:
                    LOG.warn(diagnostic.getMessage(null));
                    break;
                default:
                    LOG.info(diagnostic.getMessage(null));
                    break;
                }
            }
            if(Boolean.TRUE.equals(successed) == false) {
                throw new IOException(MessageFormat.format(
                        "{0}のコンパイルに失敗しました: {1}",
                        getEnvironment().getTargetId(),
                        errors.toString()));
            }

            for (VolatileResourceFile file : fileManager.getResources()) {
                addEntry(jar, file);
            }
            for (VolatileClassFile file : fileManager.getCompiled()) {
                addEntry(jar, file);
            }
            for (Map.Entry<String, byte[]> entry : contents.entrySet()) {
                addEntry(jar, entry.getKey(), entry.getValue());
            }
        } finally {
            fileManager.close();
        }
    }

    private void addEntry(JarOutputStream jar, String path, byte[] content) throws IOException {
        assert jar != null;
        assert path != null;
        assert content != null;
        JarEntry entry = new JarEntry(path);
        jar.putNextEntry(entry);
        jar.write(content);
        jar.closeEntry();
    }

    private void addEntry(JarOutputStream jar, FileObject file) throws IOException {
        assert jar != null;
        String path = file.toUri().getPath();
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        JarEntry entry = new JarEntry(path);
        jar.putNextEntry(entry);
        InputStream input = file.openInputStream();
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int read = input.read(buffer);
                if (read < 0) {
                    break;
                }
                jar.write(buffer, 0, read);
            }
            jar.closeEntry();
        } finally {
            input.close();
        }
    }
}
