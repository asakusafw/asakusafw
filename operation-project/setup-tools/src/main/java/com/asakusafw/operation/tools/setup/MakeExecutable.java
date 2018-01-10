/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.operation.tools.setup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class MakeExecutable {

    private static final Set<String> TRIGGER_NAME = Stream.of(
            "bin",
            "libexec").collect(Collectors.toSet());

    private static final Set<PosixFilePermission> EXECUTABLES = EnumSet.of(
            PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE);

    private static final Set<String> EXTENSION_ALWAYS = Stream.of(
            ".sh").collect(Collectors.toSet());

    private static final Set<String> EXTENSION_NEVER = Stream.of(
            ".jar",
            ".cmd",
            ".exe").collect(Collectors.toSet());

    private MakeExecutable() {
        return;
    }

    static void setExecutable(Path root) throws IOException {
        if (root.getFileSystem().supportedFileAttributeViews().contains("posix") == false) {
            System.out.printf("installation path is not a POSIX file system: %s%n", root);
            return;
        }
        walk(root, false);
    }

    private static void walk(Path current, boolean inExecDir) throws IOException {
        if (Files.isDirectory(current)) {
            List<Path> list = Files.list(current).collect(Collectors.toList());
            for (Path child : list) {
                boolean next = inExecDir || Optional.ofNullable(child.getFileName())
                        .map(Path::toString)
                        .filter(TRIGGER_NAME::contains)
                        .isPresent();
                walk(child, next);
            }
        } else if (isExecutableTarget(current, inExecDir)) {
            PosixFileAttributeView attr = Files.getFileAttributeView(current, PosixFileAttributeView.class);
            Set<PosixFilePermission> perms = attr.readAttributes().permissions();
            if (perms.containsAll(EXECUTABLES) == false) {
                System.out.printf("set executable: %s (%s)%n", current, PosixFilePermissions.toString(perms));
                perms.addAll(EXECUTABLES);
                attr.setPermissions(perms);
            }
        }
    }

    private static boolean isExecutableTarget(Path file, boolean execDir) {
        String name = Optional.ofNullable(file.getFileName())
                .map(Path::toString)
                .orElse(null);
        if (name == null) {
            return false;
        }
        if (EXTENSION_ALWAYS.stream().anyMatch(name::endsWith)) {
            return true;
        }
        if (EXTENSION_NEVER.stream().anyMatch(name::endsWith)) {
            return false;
        }
        return execDir;
    }
}
