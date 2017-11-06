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
package com.asakusafw.runtime.directio.hadoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.asakusafw.runtime.directio.AbstractDirectDataSource;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceProfile;
import com.asakusafw.runtime.directio.DirectDataSourceProvider;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.FilePattern.PatternElement;
import com.asakusafw.runtime.directio.FilePattern.PatternElementKind;
import com.asakusafw.runtime.directio.FilePattern.Segment;
import com.asakusafw.runtime.directio.FilePattern.Selection;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.stage.output.BridgeOutputFormat;

/**
 * Utilities for Direct data access facilities on Hadoop.
 * @since 0.2.5
 * @version 0.10.0
 */
public final class HadoopDataSourceUtil {

    static final Log LOG = LogFactory.getLog(HadoopDataSourceUtil.class);

    static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(String.format("DirectIO-MOVE-%d", THREAD_COUNTER.incrementAndGet())); //$NON-NLS-1$
            return t;
        }
    };

    /**
     * The key prefix of data sources.
     */
    public static final String PREFIX = "com.asakusafw.directio."; //$NON-NLS-1$

    /**
     * The key name of path.
     */
    public static final String KEY_PATH = "path"; //$NON-NLS-1$

    private static final Pattern PREFIX_PATTERN = Pattern.compile('^' + Pattern.quote(PREFIX));

    /**
     * The key name of system directory for this format.
     */
    public static final String KEY_SYSTEM_DIR = "com.asakusafw.output.system.dir"; //$NON-NLS-1$

    /**
     * The attribute key name of local tempdir.
     */
    public static final String KEY_LOCAL_TEMPDIR = "com.asakusafw.output.local.tempdir"; //$NON-NLS-1$

    private static final int PARALLEL_MOVE_MIN = 3;

    /**
     * The file name prefix of transaction began mark.
     */
    public static final String PREFIX_BEGIN_MARK = "tx-";

    /**
     * The file name prefix of transaction committed mark.
     * @since 0.10.0
     */
    public static final String PREFIX_COMMIT_MARK = "commit-";

    /**
     * The default system directory name.
     * @since 0.10.0
     */
    public static final String DEFAULT_SYSTEM_DIR = "_directio"; //$NON-NLS-1$

    /**
     * The transaction directory name.
     * @since 0.10.0
     */
    public static final String TRANSACTION_INFO_DIR = "transactions"; //$NON-NLS-1$

    /**
     * Charset for commit mark file comments.
     */
    public static final Charset COMMENT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Loads a profile list from the configuration.
     * @param conf target configuration
     * @return the restored profile list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<DirectDataSourceProfile> loadProfiles(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        Map<String, String> pathToKey = new HashMap<>();
        Map<String, String> map = getConfigMap(conf);
        Set<String> keys = getChildKeys(map, "."); //$NON-NLS-1$
        try {
            List<DirectDataSourceProfile> results = new ArrayList<>();
            for (String key : keys) {
                String className = map.get(key);
                Map<String, String> config = createPrefixMap(map, key + "."); //$NON-NLS-1$
                String path = config.remove(KEY_PATH);
                if (path == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Missing I/O configuration: {0}",
                            PREFIX + key + '.' + KEY_PATH));
                }
                path = normalizePath(path);
                if (pathToKey.containsKey(path)) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Path mapping is duplicated: {0} ({1} <=> {2})",
                            path.isEmpty() ? "/" : path, //$NON-NLS-1$
                            PREFIX + key + '.' + KEY_PATH,
                            PREFIX + pathToKey.get(key) + '.' + KEY_PATH));
                } else {
                    pathToKey.put(path, key);
                }
                Class<? extends AbstractDirectDataSource> aClass = conf.getClassByName(className)
                    .asSubclass(AbstractDirectDataSource.class);
                results.add(new DirectDataSourceProfile(key, aClass, path, config));
            }
            return results;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalizePath(String path) {
        assert path != null;
        StringBuilder buf = new StringBuilder();
        int offset = 0;
        for (int i = 0, n = path.length(); i < n; i++) {
            if (path.charAt(i) == '/') {
                offset = i + 1;
            } else {
                break;
            }
        }
        boolean sawSeparator = false;
        for (int i = offset, n = path.length(); i < n; i++) {
            char c = path.charAt(i);
            if (c == '/') {
                sawSeparator = true;
            } else {
                if (sawSeparator) {
                    buf.append('/');
                    sawSeparator = false;
                }
                buf.append(c);
            }
        }
        return buf.toString();
    }

    private static Map<String, String> getConfigMap(Configuration conf) {
        assert conf != null;
        Map<String, String> map = conf.getValByRegex(PREFIX_PATTERN.pattern());
        NavigableMap<String, String> prefixMap = createPrefixMap(map, PREFIX);
        return prefixMap;
    }

    private static NavigableMap<String, String> createPrefixMap(Map<?, ?> properties, String prefix) {
        assert properties != null;
        assert prefix != null;
        NavigableMap<String, String> results = new TreeMap<>();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            if ((entry.getKey() instanceof String) == false || (entry.getValue() instanceof String) == false) {
                continue;
            }
            String name = (String) entry.getKey();
            if (name.startsWith(prefix) == false) {
                continue;
            }
            results.put(name.substring(prefix.length()), (String) entry.getValue());
        }
        return results;
    }

    private static Set<String> getChildKeys(Map<String, String> properties, String delimitier) {
        assert properties != null;
        assert delimitier != null;
        Set<String> results = new TreeSet<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String name = entry.getKey();
            int index = name.indexOf(delimitier);
            if (index < 0) {
                results.add(name);
            } else {
                results.add(name.substring(0, index));
            }
        }
        return results;
    }

    /**
     * Loads {@link DirectDataSourceRepository} from {@link Configuration}.
     * @param conf configuration object
     * @return the created repository
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static DirectDataSourceRepository loadRepository(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        List<DirectDataSourceProfile> profiles = loadProfiles(conf);
        return createRepository(conf, profiles);
    }

    static DirectDataSourceRepository createRepository(
            Configuration conf,
            List<DirectDataSourceProfile> profiles) {
        assert conf != null;
        assert profiles != null;
        List<DirectDataSourceProvider> providers = new ArrayList<>();
        for (DirectDataSourceProfile profile : profiles) {
            providers.add(createProvider(conf, profile));
        }
        return new DirectDataSourceRepository(providers);
    }

    private static DirectDataSourceProvider createProvider(Configuration conf, DirectDataSourceProfile profile) {
        assert conf != null;
        assert profile != null;
        return new HadoopDataSourceProvider(conf, profile);
    }

    /**
     * Returns whether the local attempt output directory is defined.
     * @param localFileSystem current local file system
     * @return {@code true} to defined, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static boolean isLocalAttemptOutputDefined(LocalFileSystem localFileSystem) {
        try {
            return getLocalTemporaryDirectory(localFileSystem) != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the local temporary directory.
     * @param localFileSystem the local file system
     * @return the output path (must be on local fs), or {@code null} if not defined
     * @throws IOException if failed to compute the path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Path getLocalTemporaryDirectory(LocalFileSystem localFileSystem) throws IOException {
        if (localFileSystem == null) {
            throw new IllegalArgumentException("localFileSystem must not be null"); //$NON-NLS-1$
        }
        Configuration conf = localFileSystem.getConf();
        if (conf == null) {
            return null;
        }
        String path = conf.get(KEY_LOCAL_TEMPDIR);
        if (path == null) {
            return null;
        }
        LocalFileSystem fs = FileSystem.getLocal(conf);
        Path result = fs.makeQualified(new Path(path));
        return result;
    }

    /**
     * Creates output context from execution ID and datasource ID.
     * @param executionId current execution ID
     * @param datasourceId target datasource ID
     * @return output context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static OutputTransactionContext createContext(String executionId, String datasourceId) {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        if (datasourceId == null) {
            throw new IllegalArgumentException("datasourceId must not be null"); //$NON-NLS-1$
        }
        return new OutputTransactionContext(executionId, datasourceId, new Counter());
    }

    /**
     * Creates output context from Hadoop context.
     * @param context current context in Hadoop
     * @param datasourceId datasource ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @deprecated Use {@link BridgeOutputFormat#createContext(JobContext, String)} instead
     */
    @Deprecated
    public static OutputTransactionContext createContext(JobContext context, String datasourceId) {
        return BridgeOutputFormat.createContext(context, datasourceId);
    }

    /**
     * Creates output context from Hadoop context.
     * @param context current context in Hadoop
     * @param datasourceId datasource ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @deprecated Use {@link BridgeOutputFormat#createContext(TaskAttemptContext, String)} instead
     */
    @Deprecated
    public static OutputAttemptContext createContext(TaskAttemptContext context, String datasourceId) {
        return BridgeOutputFormat.createContext(context, datasourceId);
    }

    /**
     * Extracts an execution ID from the transaction info.
     * @param transactionInfoPath target path
     * @return execution ID, or {@code null} if is not a valid transaction info
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #getCommitMarkPath(Configuration, String)
     */
    public static String getTransactionInfoExecutionId(Path transactionInfoPath) {
        if (transactionInfoPath == null) {
            throw new IllegalArgumentException("transactionInfoPath must not be null"); //$NON-NLS-1$
        }
        return getMarkPath(transactionInfoPath, Pattern.compile("tx-(.+)")); //$NON-NLS-1$
    }

    private static String getMarkPath(Path path, Pattern pattern) {
        assert path != null;
        assert pattern != null;
        String name = path.getName();
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches() == false) {
            return null;
        }
        return matcher.group(1);
    }

    /**
     * Returns the transaction info path.
     * @param conf the current configuration
     * @param executionId target transaction ID
     * @return target path
     * @throws IOException if failed to compute the path by I/O exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Path getTransactionInfoPath(Configuration conf, String executionId) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("transactionId must not be null"); //$NON-NLS-1$
        }
        return new Path(getTransactionInfoDir(conf), PREFIX_BEGIN_MARK + executionId);
    }

    /**
     * Returns the commit mark path.
     * @param conf the current configuration
     * @param executionId target transaction ID
     * @return target path
     * @throws IOException if failed to compute the path by I/O exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Path getCommitMarkPath(Configuration conf, String executionId) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("transactionId must not be null"); //$NON-NLS-1$
        }
        return new Path(getTransactionInfoDir(conf), PREFIX_COMMIT_MARK + executionId);
    }

    /**
     * Returns the all transaction info files.
     * @param conf the current configuration
     * @return target path
     * @throws IOException if failed to find files by I/O error
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Collection<FileStatus> findAllTransactionInfoFiles(Configuration conf) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        Path dir = getTransactionInfoDir(conf);
        FileSystem fs = dir.getFileSystem(conf);
        FileStatus[] statusArray;
        try {
            statusArray = fs.listStatus(dir);
        } catch (FileNotFoundException e) {
            statusArray = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Target file is not found: {0}", dir), e); //$NON-NLS-1$
            }
        }
        if (statusArray == null || statusArray.length == 0) {
            return Collections.emptyList();
        }
        Collection<FileStatus> results = new ArrayList<>();
        for (FileStatus stat : statusArray) {
            if (getTransactionInfoExecutionId(stat.getPath()) != null) {
                results.add(stat);
            }
        }
        return results;
    }

    /**
     * Returns the system directory.
     * @param conf the current configuration
     * @return the system directory
     * @throws IOException if I/O error was occurred
     */
    public static Path getSystemDir(Configuration conf) throws IOException {
        return getSystemDir(conf, true);
    }

    /**
     * Returns the system directory.
     * @param conf the current configuration
     * @param resolve {@code true} to resolve the result path, otherwise {@code false}
     * @return the system directory
     * @throws IOException if I/O error was occurred
     * @since 0.10.0
     */
    public static Path getSystemDir(Configuration conf, boolean resolve) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        String working = conf.get(KEY_SYSTEM_DIR, DEFAULT_SYSTEM_DIR);
        Path path = new Path(working);
        if (resolve) {
            path = path.getFileSystem(conf).makeQualified(path);
        }
        return path;
    }

    private static Path getTransactionInfoDir(Configuration conf) throws IOException {
        return new Path(getSystemDir(conf), TRANSACTION_INFO_DIR);
    }

    /**
     * Searches file/directories by pattern.
     * @param fs target file system
     * @param base base path
     * @param pattern search pattern
     * @return found files, or an empty list if not found
     * @throws IOException if failed to search by I/O error
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<FileStatus> search(FileSystem fs, Path base, FilePattern pattern) throws IOException {
        if (fs == null) {
            throw new IllegalArgumentException("fs must not be null"); //$NON-NLS-1$
        }
        if (base == null) {
            throw new IllegalArgumentException("base must not be null"); //$NON-NLS-1$
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start searching for files (path={0}, resourcePattern={1})", //$NON-NLS-1$
                    base,
                    pattern));
        }
        List<FileStatus> current = new ArrayList<>(1);
        try {
            FileStatus stat = fs.getFileStatus(base);
            current.add(stat);
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
        int steps = 0;
        LinkedList<Segment> segments = new LinkedList<>(pattern.getSegments());
        while (segments.isEmpty() == false) {
            if (segments.getFirst().isTraverse()) {
                segments.removeFirst();
                current = recursiveStep(fs, current);
            } else {
                List<Path> step = consumeStep(segments);
                current = globStep(fs, current, step);
            }
            steps++;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish searching for files (path={0}, resourcePattern={1}, results={2}, steps={3})", //$NON-NLS-1$
                    base,
                    pattern,
                    current.size(),
                    steps));
        }
        return current;
    }

    private static List<Path> consumeStep(LinkedList<Segment> segments) {
        assert segments != null;
        assert segments.isEmpty() == false;
        assert segments.getFirst().isTraverse() == false;
        List<Path> results = new ArrayList<>();

        Segment current = segments.removeFirst();
        for (String segment : resolve(current)) {
            results.add(new Path(segment));
        }
        while (isGlobRequired(current) && segments.isEmpty() == false && segments.getFirst().isTraverse() == false) {
            current = segments.removeFirst();
            Set<String> suffixCandidates = resolve(current);
            if (suffixCandidates.size() == 1) {
                String suffix = suffixCandidates.iterator().next();
                for (ListIterator<Path> i = results.listIterator(); i.hasNext();) {
                    Path parent = i.next();
                    i.set(new Path(parent, suffix));
                }
            } else {
                List<Path> nextResults = new ArrayList<>();
                for (Path parent : results) {
                    for (String suffix : suffixCandidates) {
                        nextResults.add(new Path(parent, suffix));
                    }
                }
                results = nextResults;
            }
        }

        Set<Path> saw = new HashSet<>();
        for (Iterator<Path> iter = results.iterator(); iter.hasNext();) {
            Path path = iter.next();
            if (saw.contains(path)) {
                iter.remove();
            } else {
                saw.add(path);
            }
        }
        return results;
    }

    private static boolean isGlobRequired(Segment segment) {
        assert segment != null;
        assert segment.isTraverse() == false;
        for (PatternElement element : segment.getElements()) {
            if (element.getKind() == PatternElementKind.WILDCARD) {
                return false;
            }
        }
        return true;
    }

    private static Set<String> resolve(Segment segment) {
        assert segment != null;
        assert segment.isTraverse() == false;
        List<Set<String>> candidates = new ArrayList<>();
        for (PatternElement element : segment.getElements()) {
            switch (element.getKind()) {
            case TOKEN:
                candidates.add(Collections.singleton(element.getToken()));
                break;
            case WILDCARD:
                candidates.add(Collections.singleton("*")); //$NON-NLS-1$
                break;
            case SELECTION:
                candidates.add(new TreeSet<>(((Selection) element).getContents()));
                break;
            default:
                throw new AssertionError();
            }
        }
        List<String> results = stringCrossJoin(candidates);
        return new TreeSet<>(results);
    }

    private static List<String> stringCrossJoin(List<Set<String>> candidates) {
        assert candidates != null;
        assert candidates.isEmpty() == false;
        List<String> results = new ArrayList<>();
        Iterator<Set<String>> iter = candidates.iterator();
        assert iter.hasNext();
        results.addAll(iter.next());
        while (iter.hasNext()) {
            Set<String> next = iter.next();
            if (next.size() == 1) {
                String suffix = next.iterator().next();
                for (ListIterator<String> i = results.listIterator(); i.hasNext();) {
                    String vaule = i.next();
                    i.set(vaule + suffix);
                }
            } else {
                List<String> nextResults = new ArrayList<>();
                for (String value : results) {
                    for (String suffix : next) {
                        nextResults.add(value + suffix);
                    }
                }
                results = nextResults;
            }
        }
        return results;
    }

    private static List<FileStatus> recursiveStep(FileSystem fs, List<FileStatus> current) throws IOException {
        assert fs != null;
        assert current != null;
        Set<Path> paths = new HashSet<>();
        List<FileStatus> results = new ArrayList<>();
        LinkedList<FileStatus> work = new LinkedList<>(current);
        while (work.isEmpty() == false) {
            FileStatus next = work.removeFirst();
            Path path = next.getPath();
            if (paths.contains(path) == false) {
                paths.add(path);
                results.add(next);
                if (next.isDirectory()) {
                    FileStatus[] children;
                    try {
                        children = fs.listStatus(path);
                    } catch (FileNotFoundException e) {
                        children = null;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format("Target file is not found: {0}", path), e); //$NON-NLS-1$
                        }
                    }
                    if (children != null) {
                        Collections.addAll(work, children);
                    }
                }
            }
        }
        return results;
    }

    private static List<FileStatus> globStep(
            FileSystem fs,
            List<FileStatus> current,
            List<Path> expressions) throws IOException {
        assert fs != null;
        assert current != null;
        assert expressions != null;
        Set<Path> paths = new HashSet<>();
        List<FileStatus> results = new ArrayList<>();
        for (FileStatus status : current) {
            if (status.isDirectory() == false) {
                continue;
            }
            for (Path expression : expressions) {
                Path path = new Path(status.getPath(), expression);
                FileStatus[] expanded = fs.globStatus(path);
                if (expanded != null) {
                    for (FileStatus s : expanded) {
                        Path p = s.getPath();
                        if (paths.contains(p) == false) {
                            paths.add(p);
                            results.add(s);
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Returns only minimal covered files.
     * If the parameter contains both directory and its children, this result includes only the directory.
     * @param statList target files
     * @return minimal covered
     */
    public static List<FileStatus> onlyMinimalCovered(List<FileStatus> statList) {
        assert statList != null;
        FileStatus[] stats = statList.toArray(new FileStatus[statList.size()]);
        for (int i = 0; i < stats.length; i++) {
            if (stats[i] == null || stats[i].isDirectory() == false) {
                continue;
            }
            for (int j = 0; j < stats.length; j++) {
                if (i == j || stats[j] == null) {
                    continue;
                }
                if (contains(stats[i], stats[j])) {
                    stats[j] = null;
                }
            }
        }
        List<FileStatus> results = new ArrayList<>();
        for (int i = 0; i < stats.length; i++) {
            FileStatus stat = stats[i];
            if (stat != null) {
                results.add(stat);
            }
        }
        return results;
    }

    private static boolean contains(FileStatus dir, FileStatus target) {
        assert dir != null;
        assert target != null;
        assert dir.isDirectory();
        Path parent = dir.getPath();
        Path child = target.getPath();
        return contains(parent, child);
    }

    /**
     * Returns whether the parent path contains the child path, or not.
     * If the parent and child is same, this returns {@code false}.
     * @param parent the parent path
     * @param child the child path
     * @return {@code true} if parent path strictly contains the child, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static boolean contains(Path parent, Path child) {
        if (parent == null) {
            throw new IllegalArgumentException("parent must not be null"); //$NON-NLS-1$
        }
        if (child == null) {
            throw new IllegalArgumentException("child must not be null"); //$NON-NLS-1$
        }
        if (parent.depth() >= child.depth()) {
            return false;
        }
        URI parentUri = parent.toUri();
        URI childUri = child.toUri();
        URI relative = parentUri.relativize(childUri);
        if (relative.equals(childUri) == false) {
            return true;
        }
        return false;
    }

    /**
     * Moves all files in source directory into target directory.
     * @param counter counter which accepts operations count
     * @param fs file system
     * @param from path to source directory
     * @param to path to target directory
     * @throws IOException if failed to move files
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void move(
            Counter counter, FileSystem fs,
            Path from, Path to) throws IOException {
        try {
            move(counter, fs, from, fs, to, false, 0);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Moves all files in source directory into target directory.
     * @param counter counter which accepts operations count
     * @param localFs the local file system
     * @param fs the target file system
     * @param from path to source directory (must be on local file system)
     * @param to path to target directory
     * @throws IOException if failed to move files
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void moveFromLocal(
            Counter counter,
            LocalFileSystem localFs, FileSystem fs,
            Path from, Path to) throws IOException {
        try {
            move(counter, localFs, from, fs, to, true, 0);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Moves all files in source directory into target directory.
     * @param counter counter which accepts operations count
     * @param fs file system
     * @param from path to source directory
     * @param to path to target directory
     * @param threads the number of threads for moving each file
     * @throws IOException if failed to move files
     * @throws InterruptedException if interrupted while moving files
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.9.0
     */
    public static void move(
            Counter counter, FileSystem fs,
            Path from, Path to, int threads) throws IOException, InterruptedException {
        move(counter, fs, from, fs, to, false, threads);
    }

    private static void move(
            Counter counter,
            FileSystem fromFs, Path from,
            FileSystem toFs, Path to,
            boolean fromLocal, int threads) throws IOException, InterruptedException {
        if (counter == null) {
            throw new IllegalArgumentException("counter must not be null"); //$NON-NLS-1$
        }
        if (fromFs == null) {
            throw new IllegalArgumentException("fromFs must not be null"); //$NON-NLS-1$
        }
        if (from == null) {
            throw new IllegalArgumentException("from must not be null"); //$NON-NLS-1$
        }
        if (toFs == null) {
            throw new IllegalArgumentException("toFs must not be null"); //$NON-NLS-1$
        }
        if (to == null) {
            throw new IllegalArgumentException("to must not be null"); //$NON-NLS-1$
        }
        if (fromLocal && isLocalPath(from) == false) {
            throw new IllegalArgumentException("from must be on local file system"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start moving files (from={0}, to={1})", //$NON-NLS-1$
                    from,
                    to));
        }
        Path source = fromFs.makeQualified(from);
        Path target = toFs.makeQualified(to);
        List<Path> list = createFileListRelative(counter, fromFs, source);
        if (list.isEmpty()) {
            return;
        }
        boolean parallel = threads > 1 && list.size() >= PARALLEL_MOVE_MIN;
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Process moving files (from={0}, to={1}, count={2}, parallel={3})", //$NON-NLS-1$
                    from,
                    to,
                    list.size(),
                    parallel ? threads : "N/A")); //$NON-NLS-1$
        }
        if (parallel) {
            ExecutorService executor = Executors.newFixedThreadPool(
                    Math.min(threads, list.size()),
                    DAEMON_THREAD_FACTORY);
            try {
                moveParallel(counter, fromFs, toFs, source, target, list, fromLocal, executor);
            } finally {
                executor.shutdownNow();
            }
        } else {
            moveSerial(counter, fromFs, toFs, source, target, list, fromLocal);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish moving files (from={0}, to={1}, count={2})", //$NON-NLS-1$
                    from,
                    to,
                    list.size()));
        }
    }

    private static void moveSerial(
            Counter counter,
            FileSystem fromFs, FileSystem toFs,
            Path source, Path target,
            List<Path> list, boolean fromLocal) throws IOException {
        Set<Path> directoryCreated = new HashSet<>();
        for (Path path : list) {
            Path sourceFile = new Path(source, path);
            Path targetFile = new Path(target, path);
            if (LOG.isTraceEnabled()) {
                FileStatus stat = fromFs.getFileStatus(sourceFile);
                LOG.trace(MessageFormat.format(
                        "Moving file (from={0}, to={1}, size={2})", //$NON-NLS-1$
                        sourceFile,
                        targetFile,
                        stat.getLen()));
            }
            prepareTarget(toFs, targetFile, directoryCreated);
            counter.add(1);
            moveFile(toFs, sourceFile, targetFile, fromLocal);
            counter.add(1);
        }
    }

    private static void prepareTarget(FileSystem fs, Path file, Set<Path> directoryCreated) throws IOException {
        try {
            FileStatus stat = fs.getFileStatus(file);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "deleting file: {0}", //$NON-NLS-1$
                        file));
            }
            if (stat.isDirectory()) {
                fs.delete(file, true);
            } else {
                fs.delete(file, false);
            }
        } catch (FileNotFoundException e) {
            Path parent = file.getParent();
            if (directoryCreated.contains(parent) == false) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "creating directory: {0}", //$NON-NLS-1$
                            parent));
                }
                fs.mkdirs(parent);
                directoryCreated.add(parent);
            }
        }
    }

    static void moveFile(FileSystem toFs, Path sourceFile, Path targetFile, boolean fromLocal) throws IOException {
        if (fromLocal) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "moving file from local: {0} -> {1}", //$NON-NLS-1$
                        sourceFile, targetFile));
            }
            toFs.moveFromLocalFile(sourceFile, targetFile);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "moving file: {0} -> {1}", //$NON-NLS-1$
                        sourceFile, targetFile));
            }
            boolean succeed = toFs.rename(sourceFile, targetFile);
            if (succeed == false) {
                throw new IOException(MessageFormat.format(
                        "failed to move file (from={0}, to={1})",
                        sourceFile,
                        targetFile));
            }
        }
    }

    private static void moveParallel(
            Counter counter,
            FileSystem fromFs, FileSystem toFs,
            Path source, Path target,
            List<Path> list, boolean fromLocal,
            ExecutorService executor) throws IOException, InterruptedException {
        prepareParallel(counter, toFs, target, list, executor);
        parallel(executor, list.stream()
                .map(path -> (Callable<?>) () -> {
                    Path sourceFile = new Path(source, path);
                    Path targetFile = new Path(target, path);
                    if (LOG.isTraceEnabled()) {
                        FileStatus stat = fromFs.getFileStatus(sourceFile);
                        LOG.trace(MessageFormat.format(
                                "moving file (from={0}, to={1}, size={2})", //$NON-NLS-1$
                                sourceFile,
                                targetFile,
                                stat.getLen()));
                    }
                    moveFile(toFs, sourceFile, targetFile, fromLocal);
                    counter.add(1);
                    return null;
                })
                .collect(Collectors.toList()));
    }

    private static void prepareParallel(
            Counter counter, FileSystem fs, Path base, List<Path> list,
            ExecutorService executor) throws IOException, InterruptedException {
        ConcurrentMap<Path, Boolean> requiredDirs = new ConcurrentHashMap<>();
        parallel(executor, list.stream()
                .map(p -> new Path(base, p))
                .map(file -> (Callable<?>) () -> {
                    try {
                        FileStatus stat = fs.getFileStatus(file);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format(
                                    "deleting file: {0}", //$NON-NLS-1$
                                    file));
                        }
                        if (stat.isDirectory()) {
                            fs.delete(file, true);
                        } else {
                            fs.delete(file, false);
                        }
                        counter.add(1);
                    } catch (FileNotFoundException e) {
                        Path parent = file.getParent();
                        if (fs.exists(parent) == false) {
                            requiredDirs.put(parent, Boolean.TRUE);
                        }
                    }
                    return null;
                })
                .collect(Collectors.toList()));
        parallel(executor, requiredDirs.keySet().stream()
                .map(parent -> (Callable<?>) () -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "creating directory: {0}", //$NON-NLS-1$
                                parent));
                    }
                    fs.mkdirs(parent);
                    counter.add(1);
                    return null;
                })
                .collect(Collectors.toList()));
    }

    private static void parallel(
            ExecutorService executor,
            Collection<? extends Callable<?>> tasks) throws IOException, InterruptedException {
        List<Future<?>> futures = tasks.stream()
                .map((Callable<?> task) -> executor.submit(task))
                .collect(Collectors.toList());
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (CancellationException | InterruptedException e) {
                cancel(futures);
                throw e;
            } catch (ExecutionException e) {
                cancel(futures);
                try {
                    throw e.getCause();
                } catch (Error | RuntimeException | IOException | InterruptedException cause) {
                    throw cause;
                } catch (Throwable cause) {
                    throw new IOException(cause);
                }
            }
        }
    }

    private static void cancel(List<? extends Future<?>> futures) {
        futures.forEach(f -> f.cancel(true));
    }

    private static boolean isLocalPath(Path path) {
        assert path != null;
        String scheme = path.toUri().getScheme();
        return scheme != null && scheme.equals("file"); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    private static List<Path> createFileListRelative(Counter counter, FileSystem fs, Path source) throws IOException {
        assert counter != null;
        assert fs != null;
        assert source != null;
        assert source.isAbsolute();
        URI baseUri = source.toUri();
        FileStatus root;
        try {
            root = fs.getFileStatus(source);
        } catch (FileNotFoundException e) {
            LOG.warn(MessageFormat.format(
                    "Source path is not found: {0} (May be already moved)",
                    baseUri));
            return Collections.emptyList();
        }
        counter.add(1);
        List<FileStatus> all = recursiveStep(fs, Collections.singletonList(root));
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Source path contains {1} files/directories: {0}", //$NON-NLS-1$
                    baseUri,
                    all.size()));
        }
        List<Path> results = new ArrayList<>();
        for (FileStatus stat : all) {
            if (stat.isDirectory()) {
                continue;
            }
            Path path = stat.getPath();
            URI uri = path.toUri();
            URI relative = baseUri.relativize(uri);
            if (relative.equals(uri) == false) {
                results.add(new Path(relative));
            } else {
                throw new IOException(MessageFormat.format(
                        "Failed to compute relative path: base={0}, target={1}",
                        baseUri,
                        uri));
            }
            counter.add(1);
        }
        Collections.sort(results);
        return results;
    }

    /**
     * Converts {@link DataFormat} into an equivalent {@link HadoopFileFormat}.
     * @param <T> the data type
     * @param configuration the current configuration
     * @param format the target data format
     * @return the related format
     * @throws IOException if the given {@link DataFormat} is not supported
     * @since 0.9.1
     */
    public static <T> HadoopFileFormat<T> toHadoopFileFormat(
            Configuration configuration, DataFormat<T> format) throws IOException {
        assert format != null;
        if (format instanceof HadoopFileFormat<?>) {
            return (HadoopFileFormat<T>) format;
        } else {
            return new HadoopFileFormatAdapter<>(validateBinaryStreamFormat(format), configuration);
        }
    }

    private static <T> BinaryStreamFormat<T> validateBinaryStreamFormat(DataFormat<T> format) throws IOException {
        assert format != null;
        if ((format instanceof BinaryStreamFormat<?>) == false) {
            throw new IOException(MessageFormat.format(
                    "{1} must be a subtype of {0}",
                    BinaryStreamFormat.class.getName(),
                    format.getClass().getName()));
        }
        return (BinaryStreamFormat<T>) format;
    }

    private HadoopDataSourceUtil() {
        return;
    }

    private static class HadoopDataSourceProvider implements DirectDataSourceProvider {

        private final Configuration configuration;

        private final DirectDataSourceProfile profile;

        HadoopDataSourceProvider(Configuration configuration, DirectDataSourceProfile profile) {
            assert configuration != null;
            assert profile != null;
            this.configuration = configuration;
            this.profile = profile;
        }

        @Override
        public String getId() {
            return profile.getId();
        }

        @Override
        public String getPath() {
            return profile.getPath();
        }

        @Override
        public DirectDataSource newInstance() throws IOException, InterruptedException {
            try {
                AbstractDirectDataSource instance = profile.getTargetClass().getConstructor().newInstance();
                if (instance instanceof Configurable) {
                    ((Configurable) instance).setConf(configuration);
                }
                instance.configure(profile);
                return instance;
            } catch (Exception e) {
                throw new IOException(MessageFormat.format(
                        "Failed to create data source instance: {0} ({1})",
                        PREFIX + profile.getId(),
                        profile.getTargetClass().getName()), e);
            }
        }
    }
}
