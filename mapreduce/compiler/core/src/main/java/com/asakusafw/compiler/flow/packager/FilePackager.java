/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.packager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.ResourceRepository;
import com.asakusafw.compiler.batch.ResourceRepository.Cursor;
import com.asakusafw.compiler.common.FileRepository;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.Packager;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.util.Filer;

/**
 * An implementation of {@link Packager} that generates packages into the local file system.
 * @since 0.1.0
 * @version 0.7.3
 */
public class FilePackager extends FlowCompilingEnvironment.Initialized implements Packager {

    static final Logger LOG = LoggerFactory.getLogger(FilePackager.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * The option name of whether or not packaging is enabled.
     * @since 0.5.0
     */
    public static final String KEY_OPTION_PACKAGING = "packaging"; //$NON-NLS-1$

    /**
     * The option name of Java source/target version.
     * @since 0.7.3
     */
    public static final String KEY_JAVA_VERSION = "javaVersion"; //$NON-NLS-1$

    /**
     * The default value of {@link #KEY_JAVA_VERSION}.
     * @since 0.7.3
     */
    public static final String DEFAULT_JAVA_VERSION = "1.8"; //$NON-NLS-1$

    private static final String SOURCE_DIRECTORY = "src"; //$NON-NLS-1$

    private static final String CLASS_DIRECTORY = "bin"; //$NON-NLS-1$

    private final File sourceDirectory;

    private final File classDirectory;

    private final Filer sourceFiler;

    private final Filer resourceFiler;

    private final List<? extends ResourceRepository> fragmentRepositories;

    /**
     * Creates a new instance.
     * @param workingDirectory the working directory
     * @param fragmentRepositories the resources for embedding to the generating packages
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public FilePackager(File workingDirectory, List<? extends ResourceRepository> fragmentRepositories) {
        Precondition.checkMustNotBeNull(workingDirectory, "workingDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(fragmentRepositories, "resourceRepositories"); //$NON-NLS-1$
        this.fragmentRepositories = fragmentRepositories;
        this.sourceDirectory = new File(workingDirectory, SOURCE_DIRECTORY);
        this.classDirectory = new File(workingDirectory, CLASS_DIRECTORY);
        this.sourceFiler = new Filer(sourceDirectory, CHARSET);
        this.resourceFiler = new Filer(classDirectory, CHARSET);
    }

    @Override
    public PrintWriter openWriter(CompilationUnit source) throws IOException {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        return sourceFiler.openFor(source);
    }

    @Override
    public OutputStream openStream(Name packageNameOrNull, String relativePath) throws IOException {
        Precondition.checkMustNotBeNull(relativePath, "relativePath"); //$NON-NLS-1$
        File directory = resourceFiler.getFolderFor(packageNameOrNull);
        File file = new File(directory, relativePath);
        mkdir(file.getParentFile());
        return buffering(new FileOutputStream(file));
    }

    private void mkdir(File file) throws IOException {
        assert file != null;
        if (file.isDirectory() == false) {
            if (file.mkdirs() == false) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("FilePackager.errorFailedToCreateDirectory"), //$NON-NLS-1$
                        file));
            }
        }
    }

    @Override
    public void build(OutputStream output) throws IOException {
        if (skipCompile()) {
            return;
        }
        compile();
        try (JarOutputStream jar = new JarOutputStream(buffering(output))) {
            LOG.debug("creating a package of compilation results"); //$NON-NLS-1$
            List<ResourceRepository> repos = new ArrayList<>();
            if (classDirectory.exists()) {
                repos.add(new FileRepository(classDirectory));
            }
            boolean exists = drain(
                    jar,
                    repos,
                    fragmentRepositories);
            if (exists == false) {
                LOG.warn(Messages.getString("FilePackager.warnEmptyBuild")); //$NON-NLS-1$
                addDummyEntry(jar);
            }
        }
    }

    @Override
    public void packageSources(OutputStream output) throws IOException {
        if (skipCompile()) {
            return;
        }
        LOG.debug("creating a package of generated source files"); //$NON-NLS-1$
        try (JarOutputStream jar = new JarOutputStream(buffering(output))) {
            boolean exists = drain(
                    jar,
                    Collections.singletonList(new FileRepository(sourceDirectory)),
                    Collections.emptyList());
            if (exists == false) {
                LOG.warn(Messages.getString("FilePackager.warnEmptySource")); //$NON-NLS-1$
                addDummyEntry(jar);
            }
        }
    }

    private boolean drain(
            JarOutputStream jar,
            Iterable<? extends ResourceRepository> main,
            Iterable<? extends ResourceRepository> fragments) throws IOException {
        assert jar != null;
        assert fragments != null;
        Set<Location> saw = new HashSet<>();
        for (ResourceRepository repo : main) {
            drainRepo(repo, jar, saw, true);
        }
        for (ResourceRepository repo : fragments) {
            drainRepo(repo, jar, saw, false);
        }
        return saw.isEmpty() == false;
    }

    private void drainRepo(
            ResourceRepository repo, JarOutputStream jar, Set<Location> saw,
            boolean allowFrameworkInfo) throws IOException {
        assert repo != null;
        assert jar != null;
        assert saw != null;
        try (Cursor cursor = repo.createCursor()) {
            while (cursor.next()) {
                Location location = cursor.getLocation();
                if (allowFrameworkInfo == false
                        && (FRAMEWORK_INFO.isPrefixOf(location) || MANIFEST_FILE.isPrefixOf(location))) {
                    LOG.debug("Skipped adding a framework info: {}", location); //$NON-NLS-1$
                    continue;
                }
                if (saw.contains(location)) {
                    LOG.warn(MessageFormat.format(
                            Messages.getString("FilePackager.warnConflictResource"), //$NON-NLS-1$
                            location));
                    continue;
                }
                saw.add(location);
                addEntry(jar, buffering(cursor.openResource()), location);
            }
        }
    }

    private void addDummyEntry(JarOutputStream jar) throws IOException {
        ZipEntry entry = new ZipEntry(".EMPTY"); //$NON-NLS-1$
        entry.setComment("This archive file is empty."); //$NON-NLS-1$
        jar.putNextEntry(entry);
    }

    private void addEntry(
            JarOutputStream jar,
            InputStream source,
            Location location) throws IOException {
        assert jar != null;
        assert source != null;
        assert location != null;
        LOG.trace("Adding to jar entry: {}", location); //$NON-NLS-1$
        JarEntry entry = new JarEntry(location.toPath('/'));
        jar.putNextEntry(entry);
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int read = source.read(buffer);
                if (read < 0) {
                    break;
                }
                jar.write(buffer, 0, read);
            }
            jar.closeEntry();
        } finally {
            source.close();
        }
    }

    private void compile() throws IOException {
        LOG.debug("compiling generated Java source files"); //$NON-NLS-1$
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException(
                    Messages.getString("FilePackager.errorMissingJavaCompiler")); //$NON-NLS-1$
        }
        if (sourceDirectory.isDirectory() == false) {
            return;
        }
        List<File> sources = collect(sourceDirectory, new ArrayList<File>());
        if (sources.isEmpty()) {
            return;
        }
        compile(compiler, sources);
    }

    private void compile(JavaCompiler compiler, List<File> sources) throws IOException {
        assert compiler != null;
        assert sources != null;

        LOG.debug("compiling Java:{} files -> {}", sources.size(), classDirectory); //$NON-NLS-1$

        mkdir(sourceDirectory);
        mkdir(classDirectory);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics,
                Locale.getDefault(),
                CHARSET)) {
            List<String> arguments = new ArrayList<>();
            String javaVersion = getJavaVersion();
            Collections.addAll(arguments, "-source", javaVersion); //$NON-NLS-1$
            Collections.addAll(arguments, "-target", javaVersion); //$NON-NLS-1$
            Collections.addAll(arguments, "-encoding", CHARSET.name()); //$NON-NLS-1$
            Collections.addAll(arguments,
                    "-sourcepath", //$NON-NLS-1$
                    sourceDirectory.getCanonicalFile().toString());
            Collections.addAll(arguments,
                    "-d", //$NON-NLS-1$
                    classDirectory.getCanonicalFile().toString());
            Collections.addAll(arguments, "-proc:none"); //$NON-NLS-1$
            Collections.addAll(arguments, "-Xlint:all"); //$NON-NLS-1$
            Collections.addAll(arguments, "-Xlint:-options"); //$NON-NLS-1$

            Boolean succeeded;
            StringWriter errors = new StringWriter();
            try (PrintWriter pw = new PrintWriter(errors)) {
                LOG.debug("Java compile options: {}", arguments); //$NON-NLS-1$
                CompilationTask task = compiler.getTask(
                        pw,
                        fileManager,
                        diagnostics,
                        arguments,
                        Collections.emptyList(),
                        fileManager.getJavaFileObjectsFromFiles(sources));
                succeeded = task.call();
            }
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                switch (diagnostic.getKind()) {
                case ERROR:
                case MANDATORY_WARNING:
                    getEnvironment().error(toMessage(diagnostic));
                    break;
                case WARNING:
                    LOG.warn(toMessage(diagnostic));
                    break;
                default:
                    LOG.info(toMessage(diagnostic));
                    break;
                }
            }
            if (Boolean.TRUE.equals(succeeded) == false) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("FilePackager.errorFailedToCompile"), //$NON-NLS-1$
                        getEnvironment().getTargetId(),
                        errors.toString()));
            }
        }
    }

    private static String toMessage(Diagnostic<? extends JavaFileObject> diagnostic) {
        return String.format("%s (%s:%,d)", //$NON-NLS-1$
                diagnostic.getMessage(null),
                Optional.ofNullable(diagnostic.getSource())
                    .map(FileObject::getName)
                    .orElse("<unknown-file>"), //$NON-NLS-1$
                diagnostic.getLineNumber());
    }

    private String getJavaVersion() {
        return getEnvironment().getOptions().getExtraAttribute(KEY_JAVA_VERSION, DEFAULT_JAVA_VERSION);
    }

    private List<File> collect(File file, List<File> sourceFiles) {
        if (file.isFile()) {
            LOG.trace("found compilation target: {}", file); //$NON-NLS-1$
            sourceFiles.add(file);
        } else {
            for (File child : list(file)) {
                collect(child, sourceFiles);
            }
        }
        return sourceFiles;
    }

    private static List<File> list(File file) {
        return Optional.ofNullable(file.listFiles())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    private InputStream buffering(InputStream input) {
        if (input instanceof BufferedInputStream) {
            return input;
        }
        return new BufferedInputStream(input);
    }

    private OutputStream buffering(OutputStream output) {
        if (output instanceof BufferedOutputStream) {
            return output;
        }
        return new BufferedOutputStream(output);
    }

    private boolean skipCompile() {
        GenericOptionValue option = getEnvironment().getOptions()
                .getGenericExtraAttribute(KEY_OPTION_PACKAGING, GenericOptionValue.ENABLED);
        if (option == GenericOptionValue.INVALID) {
            getEnvironment().error(
                    Messages.getString("FilePackager.errorInvalidCompilerOption"), //$NON-NLS-1$
                    getEnvironment().getOptions().getExtraAttributeKeyName(KEY_OPTION_PACKAGING),
                    getEnvironment().getOptions().getExtraAttribute(KEY_OPTION_PACKAGING),
                    GenericOptionValue.ENABLED.getSymbol() + '|' + GenericOptionValue.DISABLED.getSymbol());
            option = GenericOptionValue.AUTO;
        }
        return option == GenericOptionValue.DISABLED;
    }
}
