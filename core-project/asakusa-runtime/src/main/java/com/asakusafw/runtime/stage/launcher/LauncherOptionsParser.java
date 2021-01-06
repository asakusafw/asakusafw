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
package com.asakusafw.runtime.stage.launcher;

import static com.asakusafw.runtime.stage.launcher.Util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;

import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.cache.BatchFileCacheRepository;
import com.asakusafw.runtime.util.cache.ConcurrentBatchFileCacheRepository;
import com.asakusafw.runtime.util.cache.FileCacheRepository;
import com.asakusafw.runtime.util.cache.HadoopFileCacheRepository;
import com.asakusafw.runtime.util.cache.NullBatchFileCacheRepository;
import com.asakusafw.runtime.util.lock.ConstantRetryStrategy;
import com.asakusafw.runtime.util.lock.LocalFileLockProvider;

/**
 * Converts application arguments into {@link LauncherOptions}.
 * @since 0.7.0
 */
public final class LauncherOptionsParser {

    static final Log LOG = LogFactory.getLog(LauncherOptionsParser.class);

    private static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    };

    private static final String SYSPROP_TMPDIR = "java.io.tmpdir"; //$NON-NLS-1$

    private static final String SYSPROP_USER_NAME = "user.name"; //$NON-NLS-1$

    private static final String PATTERN_TMPDIR_NAME = "asakusa-launcher-cache-{0}"; //$NON-NLS-1$

    static final String KEY_ARG_LIBRARIES = "-libjars"; //$NON-NLS-1$

    static final String KEY_CONF_LIBRARIES = "tmpjars"; //$NON-NLS-1$

    static final String KEY_CONF_JAR = StageConstants.PROP_APPLICATION_JAR;

    /**
     * The configuration key of whether library cache mechanism is enabled or not.
     */
    public static final String KEY_CACHE_ENABLED = "com.asakusafw.launcher.cache.enabled"; //$NON-NLS-1$

    /**
     * The configuration key of remote cache repository path.
     */
    public static final String KEY_CACHE_REPOSITORY = "com.asakusafw.launcher.cache.path"; //$NON-NLS-1$

    /**
     * The configuration key of max number of cache building retry attempt.
     */
    public static final String KEY_CACHE_RETRY_COUNT = "com.asakusafw.launcher.cache.retry.max"; //$NON-NLS-1$

    /**
     * The configuration key of interval long of cache building retry attempt.
     */
    public static final String KEY_CACHE_RETRY_INTERVAL = "com.asakusafw.launcher.cache.retry.interval"; //$NON-NLS-1$

    /**
     * The configuration key of local temporary directory for cache building.
     */
    public static final String KEY_CACHE_TEMPORARY = "com.asakusafw.launcher.cache.local"; //$NON-NLS-1$

    /**
     * The configuration key of max thread number for cache building.
     */
    public static final String KEY_MAX_THREADS = "com.asakusafw.launcher.cache.threads"; //$NON-NLS-1$

    /**
     * The configuration key of whether cached job jar is enabled or not.
     */
    public static final String KEY_CACHE_JOBJAR = "com.asakusafw.launcher.cache.jobjar"; //$NON-NLS-1$

    static final String PATH_LOCK_DIRECTORY = "lock"; //$NON-NLS-1$

    static final boolean DEFAULT_CACHE_ENABLED = true;

    static final int DEFAULT_CACHE_RETRY_COUNT = 50;

    static final long DEFAULT_CACHE_RETRY_INTERVAL = 100;

    static final boolean DEFAULT_CACHE_JOBJAR = true;

    static final int MINIMUM_MAX_THREADS = 1;

    static final int DEFAULT_MAX_THREADS = 4;

    private final Configuration configuration;

    private final List<String> arguments;

    private final Set<Object> applicationResources = new HashSet<>();

    private final Set<File> applicationCacheFiles = new HashSet<>();

    private LauncherOptionsParser(Configuration configuration, String[] args) {
        this.configuration = configuration;
        this.arguments = Arrays.asList(args);
    }

    /**
     * Analyze application arguments and returns {@link LauncherOptions} object.
     * @param configuration the Hadoop configuration for the application
     * @param args the launcher arguments
     * @return the analyzed options
     * @throws IllegalArgumentException if launch arguments are not valid
     * @throws IOException if failed to build launch options by I/O error
     * @throws InterruptedException if interrupted while building launch options
     */
    public static LauncherOptions parse(
            Configuration configuration,
            String... args) throws IOException, InterruptedException {
        LauncherOptionsParser parser = new LauncherOptionsParser(configuration, args);
        boolean success = false;
        try {
            LauncherOptions result = parser.analyze();
            success = true;
            return result;
        } finally {
            parser.cleanUp(success);
        }
    }

    private LauncherOptions analyze() throws IOException, InterruptedException {
        LinkedList<String> copy = new LinkedList<>(arguments);
        String applicationClassName = consumeApplicationClassName(copy);
        List<Path> libraryPaths = consumeLibraryPaths(copy);
        GenericOptionsParser genericOptions = processGenericOptions(copy);
        URLClassLoader applicationClassLoader = buildApplicationClassLoader(libraryPaths, applicationClassName);
        Class<? extends Tool> applicationClass = buildApplicationClass(applicationClassName);

        return new LauncherOptions(
                configuration,
                applicationClass,
                Arrays.asList(genericOptions.getRemainingArgs()),
                applicationClassLoader,
                applicationCacheFiles);
    }

    private String consumeApplicationClassName(LinkedList<String> rest) {
        if (rest.isEmpty()) {
            throw new IllegalArgumentException("the first argument must be target application class name");
        }
        return rest.removeFirst();
    }

    private List<Path> consumeLibraryPaths(LinkedList<String> rest) throws IOException {
        List<String> names = consumeLibraryNames(rest);
        if (names.isEmpty()) {
            return Collections.emptyList();
        }
        List<Path> results = new ArrayList<>();
        LocalFileSystem local = FileSystem.getLocal(configuration);
        for (String name : names) {
            Path path = new Path(name);
            FileSystem fs;
            if (path.toUri().getScheme() == null) {
                fs = local;
            } else {
                fs = path.getFileSystem(configuration);
            }
            path = fs.makeQualified(path);
            if (fs.exists(path) == false) {
                throw new FileNotFoundException(path.toString());
            }
            results.add(path);
        }
        return results;
    }

    private List<String> consumeLibraryNames(LinkedList<String> rest) {
        List<String> results = new ArrayList<>();
        for (Iterator<String> iter = rest.iterator(); iter.hasNext();) {
            String token = iter.next();
            if (token.equals(KEY_ARG_LIBRARIES)) {
                iter.remove();
                if (iter.hasNext()) {
                    String libraries = iter.next();
                    iter.remove();
                    for (String library : libraries.split(",")) { //$NON-NLS-1$
                        String path = library.trim();
                        if (path.isEmpty() == false) {
                            results.add(path);
                        }
                    }
                }
            }
        }
        return results;
    }

    private GenericOptionsParser processGenericOptions(LinkedList<String> rest) throws IOException {
        String[] args = rest.toArray(new String[rest.size()]);
        return new GenericOptionsParser(configuration, args);
    }

    private URLClassLoader buildApplicationClassLoader(
            List<Path> libraryPaths,
            String applicationClassName) throws IOException, InterruptedException {
        List<URL> libraries = processLibraries(libraryPaths, applicationClassName);
        ClassLoader parent = configuration.getClassLoader();
        URLClassLoader application = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () ->
                new URLClassLoader(libraries.toArray(new URL[libraries.size()]), parent));
        this.applicationResources.add(application);
        configuration.setClassLoader(application);
        return application;
    }

    private List<URL> processLibraries(
            List<Path> libraryPaths,
            String applicationClassName) throws IOException, InterruptedException {
        if (libraryPaths.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Path, Path> resolved = processLibraryCache(libraryPaths);
        List<URL> localUrls = new ArrayList<>();
        List<Path> remotePaths = new ArrayList<>();
        for (Path path : libraryPaths) {
            URI uri = path.toUri();
            assert uri.getScheme() != null;
            if (uri.getScheme().equals("file")) { //$NON-NLS-1$
                localUrls.add(uri.toURL());
            }
            Path remote = resolved.get(path);
            if (remote == null) {
                remotePaths.add(path);
            } else {
                remotePaths.add(remote);
            }
        }

        if (configuration.getBoolean(KEY_CACHE_JOBJAR, DEFAULT_CACHE_JOBJAR)) {
            configureJobJar(libraryPaths, applicationClassName, resolved);
        }

        String libjars = buildLibjars(remotePaths);
        if (libjars.isEmpty() == false) {
            configuration.set(KEY_CONF_LIBRARIES, libjars);
        }
        return localUrls;
    }

    private void configureJobJar(List<Path> paths, String className, Map<Path, Path> cacheMap) throws IOException {
        if (configuration.get(KEY_CONF_JAR) != null) {
            return;
        }
        for (Path path : paths) {
            Path remote = cacheMap.get(path);
            URI uri = path.toUri();
            if (remote != null && uri.getScheme().equals("file")) { //$NON-NLS-1$
                File file = new File(uri);
                if (isInclude(file, className)) {
                    Path qualified = remote.getFileSystem(configuration).makeQualified(remote);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "Application class is in: file={2} ({1}), class={0}", //$NON-NLS-1$
                                className,
                                path,
                                qualified));
                    }
                    URI target = qualified.toUri();
                    if (target.getScheme() != null
                            && (target.getScheme().equals("file") || target.getAuthority() != null)) { //$NON-NLS-1$
                        configuration.set(KEY_CONF_JAR, qualified.toString());
                    }
                    break;
                }
            }
        }
    }

    private boolean isInclude(File file, String className) {
        try (ZipFile zip = new ZipFile(file)) {
            return zip.getEntry(className.replace('.', '/') + ".class") != null; //$NON-NLS-1$
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Exception occurred while detecting for class: file={0}, class={1}", //$NON-NLS-1$
                        file,
                        className), e);
            }
        }
        return false;
    }

    private String buildLibjars(List<Path> paths) throws IOException {
        StringBuilder buf = new StringBuilder(configuration.get(KEY_CONF_LIBRARIES, "")); //$NON-NLS-1$
        for (Path path : paths) {
            if (buf.length() != 0) {
                buf.append(',');
            }
            FileSystem fs = path.getFileSystem(configuration);
            buf.append(fs.makeQualified(path).toString());
        }
        String libjars = buf.toString();
        return libjars;
    }

    private Map<Path, Path> processLibraryCache(List<Path> libraryPaths) throws IOException, InterruptedException {
        boolean useCache = computeEnabled();
        if (useCache) {
            Path repositoryPath = computeRepositoryPath();
            File temporary = computeTemporaryDirectory();
            int threads = Math.max(configuration.getInt(KEY_MAX_THREADS, DEFAULT_MAX_THREADS), MINIMUM_MAX_THREADS);
            int retryCount = configuration.getInt(KEY_CACHE_RETRY_COUNT, DEFAULT_CACHE_RETRY_COUNT);
            long retryInterval = configuration.getLong(KEY_CACHE_RETRY_INTERVAL, DEFAULT_CACHE_RETRY_INTERVAL);
            FileCacheRepository unit = new HadoopFileCacheRepository(
                    configuration,
                    repositoryPath,
                    new LocalFileLockProvider<Path>(new File(temporary, PATH_LOCK_DIRECTORY)),
                    new ConstantRetryStrategy(retryCount, retryInterval));
            ExecutorService executor = Executors.newFixedThreadPool(threads, DAEMON_THREAD_FACTORY);
            try {
                BatchFileCacheRepository repo = new ConcurrentBatchFileCacheRepository(unit, executor);
                return repo.resolve(libraryPaths);
            } finally {
                executor.shutdownNow();
            }
        } else {
            return new NullBatchFileCacheRepository().resolve(libraryPaths);
        }
    }

    private boolean computeEnabled() {
        boolean explicit = configuration.getBoolean(KEY_CACHE_ENABLED, DEFAULT_CACHE_ENABLED);
        if (explicit == false) {
            return false;
        }
        String repositoryPath = configuration.get(KEY_CACHE_REPOSITORY);
        if (repositoryPath == null || repositoryPath.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private Path computeRepositoryPath() throws IOException {
        assert configuration.get(KEY_CACHE_REPOSITORY) != null;
        Path repositoryPath = new Path(configuration.get(KEY_CACHE_REPOSITORY));
        repositoryPath = repositoryPath.getFileSystem(configuration).makeQualified(repositoryPath);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Using cache repository: -D{0}={1}", //$NON-NLS-1$
                    KEY_CACHE_REPOSITORY,
                    repositoryPath));
        }
        return repositoryPath;
    }

    private File computeTemporaryDirectory() {
        String temporary = configuration.get(KEY_CACHE_TEMPORARY);
        if (temporary != null) {
            return new File(temporary);
        }
        String tmpdir = System.getProperty(SYSPROP_TMPDIR);
        if (tmpdir == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "System property \"{0}\" must be defined",
                    SYSPROP_TMPDIR));
        }
        String name = System.getProperty(SYSPROP_USER_NAME);
        if (name == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "System property \"{0}\" must be defined",
                    SYSPROP_USER_NAME));
        }
        String folder = MessageFormat.format(PATTERN_TMPDIR_NAME, name);
        File result = new File(tmpdir, folder);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Auto configuration: -D{0}={1}", //$NON-NLS-1$
                    KEY_CACHE_TEMPORARY,
                    result));
        }
        return result;
    }

    private Class<? extends Tool> buildApplicationClass(String applicationClassName) {
        Class<? extends Tool> applicationClass;
        try {
            Class<?> aClass = configuration.getClassByName(applicationClassName);
            if (Tool.class.isAssignableFrom(aClass) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Application \"{0}\" must be a subclass of \"{1}\"",
                        aClass.getName(),
                        Tool.class.getName()));
            }
            applicationClass = aClass.asSubclass(Tool.class);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Application \"{0}\" is not found",
                    applicationClassName));
        }
        return applicationClass;
    }

    private void cleanUp(boolean success) {
        if (success == false) {
            for (Object object : applicationResources) {
                closeQuiet(object);
            }
            for (File file : applicationCacheFiles) {
                if (delete(file) == false) {
                    LOG.warn(MessageFormat.format(
                            "Failed to delete the application cache directory: {0}",
                            file));
                }
            }
        }
    }
}
