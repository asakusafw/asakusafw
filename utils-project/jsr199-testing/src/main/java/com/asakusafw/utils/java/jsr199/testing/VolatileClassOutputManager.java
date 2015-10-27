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
package com.asakusafw.utils.java.jsr199.testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

/**
 * An implementation of {@link JavaFileManager} which stores generated contents onto the heap.
 */
public class VolatileClassOutputManager
        extends ForwardingJavaFileManager<JavaFileManager> {

    /**
     * The Java name separator character.
     */
    public static final char NAME_SEPARATOR = '.';

    /**
     * The path name separator character.
     */
    public static final char SEGMENT_SEPARATOR = '/';

    private static final char NAME_SEPARATOR_NEXT = NAME_SEPARATOR + 1;

    private SortedMap<String, VolatileClassFile> classMap;

    private SortedMap<String, VolatileJavaFile> sourceMap;

    private SortedMap<String, VolatileResourceFile> resourceMap;

    /**
     * Creates a new instance.
     * @param fileManager the delegation target file manager
     * @throws NullPointerException if the parameter is {@code null}
     */
    public VolatileClassOutputManager(JavaFileManager fileManager) {
        super(fileManager);
        this.classMap = new TreeMap<String, VolatileClassFile>();
        this.sourceMap = new TreeMap<String, VolatileJavaFile>();
        this.resourceMap = new TreeMap<String, VolatileResourceFile>();
    }

    /**
     * Returns the generated Java source files.
     * @return the generated Java source files
     */
    public Collection<VolatileJavaFile> getSources() {
        return new ArrayList<VolatileJavaFile>(sourceMap.values());
    }

    /**
     * Returns the generated resource files.
     * @return the generated resource files
     */
    public Collection<VolatileResourceFile> getResources() {
        return new ArrayList<VolatileResourceFile>(resourceMap.values());
    }

    /**
     * Returns the generated Java class files.
     * @return the generated Java class files
     */
    public Collection<VolatileClassFile> getCompiled() {
        return new ArrayList<VolatileClassFile>(classMap.values());
    }

    /**
     * {@inheritDoc}
     * {@link #getSources()}, {@link #getResources()}, and  {@link #getCompiled()} will return empty collections
     * after this operation. Please obtain the each result before closing this object, then the results will
     * be still available after this was closed.
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            this.sourceMap = new TreeMap<String, VolatileJavaFile>();
            this.classMap = new TreeMap<String, VolatileClassFile>();
            this.resourceMap = new TreeMap<String, VolatileResourceFile>();
        }
    }

    @Override
    public boolean hasLocation(Location location) {
        if (location == StandardLocation.CLASS_OUTPUT) {
            return true;
        } else {
            return super.hasLocation(location);
        }
    }

    @Override
    public FileObject getFileForInput(
            Location location,
            String packageName,
            String relativeName) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            String binaryName = normalizePath(packageName, relativeName, JavaFileObject.Kind.CLASS);
            if (binaryName == null) {
                String path = toPath(packageName, relativeName);
                return resourceMap.get(path);
            }
            return getJavaFileForInput(location, binaryName, JavaFileObject.Kind.CLASS);
        } else if (location == StandardLocation.SOURCE_OUTPUT) {
            String binaryName = normalizePath(packageName, relativeName, JavaFileObject.Kind.SOURCE);
            if (binaryName == null) {
                String path = toPath(packageName, relativeName);
                return resourceMap.get(path);
            }
            return getJavaFileForInput(location, binaryName, JavaFileObject.Kind.SOURCE);
        } else {
            return super.getFileForInput(location, packageName, relativeName);
        }
    }

    @Override
    public FileObject getFileForOutput(
            Location location,
            String packageName,
            String relativeName,
            FileObject sibling) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            String binaryName = normalizePath(packageName, relativeName, JavaFileObject.Kind.CLASS);
            if (binaryName == null) {
                String path = toPath(packageName, relativeName);
                VolatileResourceFile file = resourceMap.get(path);
                if (file == null) {
                    file = new VolatileResourceFile(path);
                    resourceMap.put(path, file);
                }
                return file;
            }
            return getJavaFileForOutput(location, binaryName, JavaFileObject.Kind.CLASS, sibling);
        } else if (location == StandardLocation.SOURCE_OUTPUT) {
            String binaryName = normalizePath(packageName, relativeName, JavaFileObject.Kind.SOURCE);
            if (binaryName == null) {
                String path = toPath(packageName, relativeName);
                VolatileResourceFile file = resourceMap.get(path);
                if (file == null) {
                    file = new VolatileResourceFile(path);
                    resourceMap.put(path, file);
                }
                return file;
            }
            return getJavaFileForOutput(location, binaryName, JavaFileObject.Kind.SOURCE, sibling);
        } else {
            return super.getFileForOutput(location, packageName, relativeName, sibling);
        }
    }

    @Override
    public JavaFileObject getJavaFileForInput(
            Location location,
            String className,
            Kind kind) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            String binaryName = normalizeClassName(className);
            if (classMap.containsKey(binaryName)) {
                return classMap.get(binaryName);
            }
            return null;
        } else if (location == StandardLocation.SOURCE_OUTPUT) {
            String binaryName = normalizeClassName(className);
            if (sourceMap.containsKey(binaryName)) {
                return sourceMap.get(binaryName);
            }
            return null;
        } else {
            return super.getJavaFileForInput(location, className, kind);
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            Location location,
            String className,
            Kind kind,
            FileObject sibling) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            String binaryName = normalizeClassName(className);
            if (classMap.containsKey(binaryName)) {
                return classMap.get(binaryName);
            }
            VolatileClassFile classFile = new VolatileClassFile(binaryName);
            classMap.put(binaryName, classFile);
            return classFile;
        } else if (location == StandardLocation.SOURCE_OUTPUT) {
            String binaryName = normalizeClassName(className);
            if (sourceMap.containsKey(binaryName)) {
                return sourceMap.get(binaryName);
            }
            VolatileJavaFile javaFile = new VolatileJavaFile(className.replace('.', '/'));
            sourceMap.put(binaryName, javaFile);
            return javaFile;
        } else {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }

    @Override
    public Iterable<JavaFileObject> list(
            Location location,
            String packageName,
            Set<Kind> kinds,
            boolean recurse) throws IOException {
        if (location == StandardLocation.CLASS_OUTPUT) {
            if (kinds.contains(JavaFileObject.Kind.CLASS) == false) {
                return Collections.emptySet();
            }
            return inPackage(classMap, packageName, recurse);
        } else if (location == StandardLocation.SOURCE_OUTPUT) {
            if (kinds.contains(JavaFileObject.Kind.SOURCE) == false) {
                return Collections.emptySet();
            }
            return inPackage(sourceMap, packageName, recurse);
        } else {
            return super.list(location, packageName, kinds, recurse);
        }
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        if (a instanceof VolatileJavaFile || a instanceof VolatileClassFile) {
            return a.toUri().equals(b.toUri());
        }
        if (b instanceof VolatileJavaFile || b instanceof VolatileClassFile) {
            return b.toUri().equals(a.toUri());
        }
        return super.isSameFile(a, b);
    }

    private Collection<JavaFileObject> inPackage(
            SortedMap<String, ? extends JavaFileObject> all,
            String packageName,
            boolean recurse) {
        assert all != null;
        assert packageName != null;
        Map<String, ? extends JavaFileObject> map;
        int prefix;
        if (packageName.isEmpty()) {
            map = all;
            prefix = 0;
        } else {
            String binaryName = normalizeClassName(packageName);
            map = all.subMap(binaryName + NAME_SEPARATOR, binaryName + NAME_SEPARATOR_NEXT);
            prefix = binaryName.length() + 1;
        }

        if (recurse) {
            return new ArrayList<JavaFileObject>(map.values());
        }

        List<JavaFileObject> results = new ArrayList<JavaFileObject>();
        for (Map.Entry<String, ? extends JavaFileObject> entry : map.entrySet()) {
            String className = entry.getKey();
            if (className.indexOf(NAME_SEPARATOR, prefix) < 0) {
                results.add(entry.getValue());
            }
        }
        return results;
    }

    private String normalizePath(String packageName, String relativeName, JavaFileObject.Kind kind) {
        assert packageName != null;
        assert relativeName != null;
        if (relativeName.endsWith(kind.extension) == false) {
            return null;
        }
        String strippedRelativeName = relativeName.substring(0, relativeName.length() - kind.extension.length());
        if (packageName.isEmpty()) {
            return normalizeClassName(strippedRelativeName);
        }
        String className = packageName + SEGMENT_SEPARATOR + strippedRelativeName;
        return normalizeClassName(className);
    }

    private static String normalizeClassName(String className) {
        assert className != null;
        return className.replace(SEGMENT_SEPARATOR, NAME_SEPARATOR);
    }

    private String toPath(String packageName, String relativeName) {
        assert packageName != null;
        assert relativeName != null;
        if (packageName.isEmpty()) {
            return relativeName;
        }
        return packageName + SEGMENT_SEPARATOR + relativeName;
    }
}
