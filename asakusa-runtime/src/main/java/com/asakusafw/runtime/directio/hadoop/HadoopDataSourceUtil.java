/**
 * Copyright 2012 Asakusa Framework Team.
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
import java.text.MessageFormat;
import java.util.ArrayList;
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
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.asakusafw.runtime.directio.AbstractDirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceProfile;
import com.asakusafw.runtime.directio.DirectDataSourceProvider;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.SearchPattern;
import com.asakusafw.runtime.directio.SearchPattern.PatternElement;
import com.asakusafw.runtime.directio.SearchPattern.PatternElementKind;
import com.asakusafw.runtime.directio.SearchPattern.Segment;
import com.asakusafw.runtime.directio.SearchPattern.Selection;
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

    private static DirectDataSourceRepository createRepository(
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
        return new OutputTransactionContext(transactionId, datasourceId);
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
        return new OutputAttemptContext(transactionId, attemptId, datasourceId);
    }

    private static String getTransactionId(JobContext jobContext, String datasourceId) {
        assert jobContext != null;
        assert datasourceId != null;
        String executionId = jobContext.getConfiguration().get(StageConstants.PROP_EXECUTION_ID);
        if (executionId == null) {
            executionId = jobContext.getJobID().toString();
        }
        return executionId;
    }

    private static String getAttemptId(TaskAttemptContext taskContext, String datasourceId) {
        assert taskContext != null;
        assert datasourceId != null;
        return taskContext.getTaskAttemptID().toString();
    }

    private static DirectDataSourceProvider createProvider(Configuration conf, DirectDataSourceProfile profile) {
        assert conf != null;
        assert profile != null;
        return new HadoopDataSourceProvider(conf, profile);
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
    public static List<FileStatus> search(FileSystem fs, Path base, SearchPattern pattern) throws IOException {
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
                    "Start searching files (path={0}, resourcePattern={1})",
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
                    "Finish searching files (path={0}, resourcePattern={1}, results={2}, steps={3})",
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
                    FileStatus[] children = fs.listStatus(path);
                    Collections.addAll(work, children);
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
     * @param fs file system
     * @param from path to source directory
     * @param to path to target directory
     * @throws IOException if failed to move files
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void move(FileSystem fs, Path from, Path to) throws IOException {
        if (fs == null) {
            throw new IllegalArgumentException("fs must not be null"); //$NON-NLS-1$
        }
        if (from == null) {
            throw new IllegalArgumentException("from must not be null"); //$NON-NLS-1$
        }
        if (to == null) {
            throw new IllegalArgumentException("to must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start moving files (from={0}, to={1})",
                    from,
                    to));
        }
        Path source = fs.makeQualified(from);
        Path target = fs.makeQualified(to);
        List<Path> list = createFileListRelative(fs, source);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Process moving files (from={0}, to={1}, count={2})",
                    from,
                    to,
                    list.size()));
        }
        boolean fromLocal = isLocalPath(from);
        Set<Path> directoryCreated = new HashSet<Path>();
        for (Path path : list) {
            Path sourceFile = new Path(source, path);
            Path targetFile = new Path(target, path);
            if (LOG.isTraceEnabled()) {
                FileStatus stat = fs.getFileStatus(sourceFile);
                LOG.trace(MessageFormat.format(
                        "Moving file (from={0}, to={1}, size={2})",
                        sourceFile,
                        targetFile,
                        stat.getLen()));
            }
            try {
                FileStatus stat = fs.getFileStatus(targetFile);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Deleting file: {0}",
                            targetFile));
                }
                if (stat.isDir()) {
                    fs.delete(targetFile, true);
                } else {
                    fs.delete(targetFile, false);
                }
            } catch (FileNotFoundException e) {
                Path parent = targetFile.getParent();
                if (directoryCreated.contains(parent) == false) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(MessageFormat.format(
                                "Creating directory: {0}",
                                parent));
                    }
                    fs.mkdirs(parent);
                    directoryCreated.add(parent);
                }
            }
            if (fromLocal) {
                fs.moveFromLocalFile(sourceFile, targetFile);
            } else {
                boolean succeed = fs.rename(sourceFile, targetFile);
                if (succeed == false) {
                    throw new IOException(MessageFormat.format(
                            "Failed to move file (from={0}, to={1})",
                            sourceFile,
                            targetFile));
                }
            }
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
    private static List<Path> createFileListRelative(FileSystem fs, Path source) throws IOException {
        assert fs != null;
        assert source != null;
        assert source.isAbsolute();
        URI baseUri = source.toUri();
        FileStatus root;
        try {
            root = fs.getFileStatus(source);
        } catch (FileNotFoundException e) {
            LOG.warn(MessageFormat.format(
                    "Source path is not found: {0}",
                    baseUri));
            return Collections.emptyList();
        }
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
