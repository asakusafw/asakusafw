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
package com.asakusafw.runtime.directio.hadoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.hadoop.util.Progressable;

import com.asakusafw.runtime.directio.AbstractDirectDataSource;
import com.asakusafw.runtime.directio.Counter;
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
import com.asakusafw.runtime.stage.StageConstants;

/**
 * Utilities for Direct data access facilities on Hadoop.
 * @since 0.2.5
 */
public final class HadoopDataSourceUtil {

    static final Log LOG = LogFactory.getLog(HadoopDataSourceUtil.class);

    /**
     * The key prefix of data sources.
     */
    public static final String PREFIX = "com.asakusafw.directio.";

    /**
     * The key name of path.
     */
    public static final String KEY_PATH = "path";

    private static final Pattern PREFIX_PATTERN = Pattern.compile('^' + Pattern.quote(PREFIX));

    /**
     * The key name of system directory for this format.
     */
    public static final String KEY_SYSTEM_DIR = "com.asakusafw.output.system.dir";

    /**
     * The attribute key name of local tempdir.
     */
    public static final String KEY_LOCAL_TEMPDIR = "com.asakusafw.output.local.tempdir";

    static final String DEFAULT_SYSTEM_DIR = "_directio";

    static final String TRANSACTION_INFO_DIR = "transactions";

    /**
     * Charset for commit mark file comments.
     */
    public static final Charset COMMENT_CHARSET = Charset.forName("UTF-8");

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
        Map<String, String> pathToKey = new HashMap<String, String>();
        Map<String, String> map = getConfigMap(conf);
        Set<String> keys = getChildKeys(map, ".");
        try {
            List<DirectDataSourceProfile> results = new ArrayList<DirectDataSourceProfile>();
            for (String key : keys) {
                String className = map.get(key);
                Map<String, String> config = createPrefixMap(map, key + ".");
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
                            path.isEmpty() ? "/" : path,
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
        NavigableMap<String, String> results = new TreeMap<String, String>();
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
        Set<String> results = new TreeSet<String>();
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
        List<DirectDataSourceProvider> providers = new ArrayList<DirectDataSourceProvider>();
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
     * Creates output context from Hadoop context.
     * @param context current context in Hadoop
     * @param datasourceId datasource ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static OutputTransactionContext createContext(JobContext context, String datasourceId) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (datasourceId == null) {
            throw new IllegalArgumentException("datasourceId must not be null"); //$NON-NLS-1$
        }
        String transactionId = getTransactionId(context, datasourceId);
        return new OutputTransactionContext(transactionId, datasourceId, createCounter(context));
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
        String transactionId = getTransactionId(executionId);
        return new OutputTransactionContext(transactionId, datasourceId, new Counter());
    }

    /**
     * Creates output context from Hadoop context.
     * @param context current context in Hadoop
     * @param datasourceId datasource ID
     * @return the created context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static OutputAttemptContext createContext(TaskAttemptContext context, String datasourceId) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (datasourceId == null) {
            throw new IllegalArgumentException("datasourceId must not be null"); //$NON-NLS-1$
        }
        String transactionId = getTransactionId(context, datasourceId);
        String attemptId = getAttemptId(context, datasourceId);
        return new OutputAttemptContext(transactionId, attemptId, datasourceId, createCounter(context));
    }

    private static String getTransactionId(JobContext jobContext, String datasourceId) {
        assert jobContext != null;
        assert datasourceId != null;
        String executionId = jobContext.getConfiguration().get(StageConstants.PROP_EXECUTION_ID);
        if (executionId == null) {
            executionId = jobContext.getJobID().toString();
        }
        return getTransactionId(executionId);
    }

    private static String getTransactionId(String executionId) {
        return executionId;
    }

    private static String getAttemptId(TaskAttemptContext taskContext, String datasourceId) {
        assert taskContext != null;
        assert datasourceId != null;
        return taskContext.getTaskAttemptID().toString();
    }

    private static Counter createCounter(JobContext context) {
        assert context != null;
        if (context instanceof Progressable) {
            return new ProgressableCounter((Progressable) context);
        } else if (context instanceof org.apache.hadoop.mapred.JobContext) {
            return new ProgressableCounter(((org.apache.hadoop.mapred.JobContext) context).getProgressible());
        } else {
            return new Counter();
        }
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
        return getMarkPath(transactionInfoPath, Pattern.compile("tx-(.+)"));
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
        return new Path(
                getTransactionInfoDir(conf),
                String.format("tx-%s", executionId));
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
        return new Path(
                getTransactionInfoDir(conf),
                String.format("commit-%s", executionId));
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
                LOG.debug(MessageFormat.format("Target file is not found: {0}", dir), e);
            }
        }
        if (statusArray == null || statusArray.length == 0) {
            return Collections.emptyList();
        }
        Collection<FileStatus> results = new ArrayList<FileStatus>();
        for (FileStatus stat : statusArray) {
            if (getTransactionInfoExecutionId(stat.getPath()) != null) {
                results.add(stat);
            }
        }
        return results;
    }

    private static Path getTransactionInfoDir(Configuration conf) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        String working = conf.get(KEY_SYSTEM_DIR, DEFAULT_SYSTEM_DIR);
        Path path = new Path(working, TRANSACTION_INFO_DIR);
        return path.makeQualified(path.getFileSystem(conf));
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
                    "Start searching for files (path={0}, resourcePattern={1})",
                    base,
                    pattern));
        }
        List<FileStatus> current = new ArrayList<FileStatus>(1);
        try {
            FileStatus stat = fs.getFileStatus(base);
            current.add(stat);
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
        int steps = 0;
        LinkedList<Segment> segments = new LinkedList<Segment>(pattern.getSegments());
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
                    "Finish searching for files (path={0}, resourcePattern={1}, results={2}, steps={3})",
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
        List<Path> results = new ArrayList<Path>();

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
                List<Path> nextResults = new ArrayList<Path>();
                for (Path parent : results) {
                    for (String suffix : suffixCandidates) {
                        nextResults.add(new Path(parent, suffix));
                    }
                }
                results = nextResults;
            }
        }

        Set<Path> saw = new HashSet<Path>();
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
        List<Set<String>> candidates = new ArrayList<Set<String>>();
        for (PatternElement element : segment.getElements()) {
            switch (element.getKind()) {
            case TOKEN:
                candidates.add(Collections.singleton(element.getToken()));
                break;
            case WILDCARD:
                candidates.add(Collections.singleton("*"));
                break;
            case SELECTION:
                candidates.add(new TreeSet<String>(((Selection) element).getContents()));
                break;
            default:
                throw new AssertionError();
            }
        }
        List<String> results = stringCrossJoin(candidates);
        return new TreeSet<String>(results);
    }

    private static List<String> stringCrossJoin(List<Set<String>> candidates) {
        assert candidates != null;
        assert candidates.isEmpty() == false;
        List<String> results = new ArrayList<String>();
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
                List<String> nextResults = new ArrayList<String>();
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
        Set<Path> paths = new HashSet<Path>();
        List<FileStatus> results = new ArrayList<FileStatus>();
        LinkedList<FileStatus> work = new LinkedList<FileStatus>(current);
        while (work.isEmpty() == false) {
            FileStatus next = work.removeFirst();
            Path path = next.getPath();
            if (paths.contains(path) == false) {
                paths.add(path);
                results.add(next);
                if (next.isDir()) {
                    FileStatus[] children;
                    try {
                        children = fs.listStatus(path);
                    } catch (FileNotFoundException e) {
                        children = null;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format("Target file is not found: {0}", path), e);
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
        Set<Path> paths = new HashSet<Path>();
        List<FileStatus> results = new ArrayList<FileStatus>();
        for (FileStatus status : current) {
            if (status.isDir() == false) {
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
            if (stats[i] == null || stats[i].isDir() == false) {
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
        List<FileStatus> results = new ArrayList<FileStatus>();
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
        assert dir.isDir();
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
            Counter counter,
            FileSystem fs,
            Path from,
            Path to) throws IOException {
        move(counter, fs, from, fs, to, false);
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
            LocalFileSystem localFs,
            FileSystem fs,
            Path from,
            Path to) throws IOException {
        move(counter, localFs, from, fs, to, true);
    }

    private static void move(
            Counter counter,
            FileSystem fromFs, Path from,
            FileSystem toFs, Path to,
            boolean fromLocal) throws IOException {
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
                    "Start moving files (from={0}, to={1})",
                    from,
                    to));
        }
        Path source = fromFs.makeQualified(from);
        Path target = toFs.makeQualified(to);
        List<Path> list = createFileListRelative(counter, fromFs, source);
        if (list.isEmpty()) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Process moving files (from={0}, to={1}, count={2})",
                    from,
                    to,
                    list.size()));
        }
        Set<Path> directoryCreated = new HashSet<Path>();
        for (Path path : list) {
            Path sourceFile = new Path(source, path);
            Path targetFile = new Path(target, path);
            if (LOG.isTraceEnabled()) {
                FileStatus stat = fromFs.getFileStatus(sourceFile);
                LOG.trace(MessageFormat.format(
                        "Moving file (from={0}, to={1}, size={2})",
                        sourceFile,
                        targetFile,
                        stat.getLen()));
            }
            try {
                FileStatus stat = toFs.getFileStatus(targetFile);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Deleting file: {0}",
                            targetFile));
                }
                if (stat.isDir()) {
                    toFs.delete(targetFile, true);
                } else {
                    toFs.delete(targetFile, false);
                }
            } catch (FileNotFoundException e) {
                Path targetParent = targetFile.getParent();
                if (directoryCreated.contains(targetParent) == false) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "Creating directory: {0}",
                                targetParent));
                    }
                    toFs.mkdirs(targetParent);
                    directoryCreated.add(targetParent);
                }
            }
            counter.add(1);
            if (fromLocal) {
                toFs.moveFromLocalFile(sourceFile, targetFile);
            } else {
                boolean succeed = toFs.rename(sourceFile, targetFile);
                if (succeed == false) {
                    throw new IOException(MessageFormat.format(
                            "Failed to move file (from={0}, to={1})",
                            sourceFile,
                            targetFile));
                }
            }
            counter.add(1);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish moving files (from={0}, to={1}, count={2})",
                    from,
                    to,
                    list.size()));
        }
    }

    private static boolean isLocalPath(Path path) {
        assert path != null;
        String scheme = path.toUri().getScheme();
        return scheme != null && scheme.equals("file");
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
                    "Source path contains {1} files/directories: {0}",
                    baseUri,
                    all.size()));
        }
        List<Path> results = new ArrayList<Path>();
        for (FileStatus stat : all) {
            if (stat.isDir()) {
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

    private HadoopDataSourceUtil() {
        return;
    }

    private static class HadoopDataSourceProvider implements DirectDataSourceProvider {

        private final Configuration configuration;

        private final DirectDataSourceProfile profile;

        public HadoopDataSourceProvider(Configuration configuration, DirectDataSourceProfile profile) {
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
