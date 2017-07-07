/*
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
package com.asakusafw.gradle.tasks.internal

import org.gradle.api.Task
import org.gradle.api.file.FileCollection

/**
 * Utilities for Asakusa Tool Launchers.
 */
final class ToolLauncherUtils {

    public static final String MAIN_CLASS = 'com.asakusafw.sdk.launcher.Launcher'

    static File createLaunchFile(Task self, FileCollection classpath, String mainClass, List<String> arguments) {
        Properties properties = new Properties()
        properties.setProperty 'main', mainClass
        classpath.eachWithIndex { File f, int index ->
            properties.setProperty "classpath.${index}", f.absolutePath
        }
        arguments.eachWithIndex { String s, int index ->
            properties.setProperty "argument.${index}", s
        }

        File temporary = self.getTemporaryDir()
        if (temporary.exists() == false) {
            self.project.mkdir temporary
        }

        File result = new File(temporary, 'launch.properties')
        result.withOutputStream { OutputStream out ->
            properties.store out, null
        }
        return result
    }

    private ToolLauncherUtils() {
    }
}
