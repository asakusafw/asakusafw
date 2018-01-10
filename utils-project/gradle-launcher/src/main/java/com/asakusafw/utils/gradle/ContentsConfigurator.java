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
package com.asakusafw.utils.gradle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Configures project contents.
 * @since 0.9.2
 */
public class ContentsConfigurator implements Consumer<BaseProject<?>> {

    private final TryConsumer<? super Bundle, IOException> action;

    /**
     * Creates a new instance.
     * @param action the action
     */
    public ContentsConfigurator(TryConsumer<? super Bundle, IOException> action) {
        this.action = action;
    }

    /**
     * Returns a configuration which copies contents onto the contents root.
     * @param source the source path
     * @return the configurator
     */
    public static ContentsConfigurator copy(String source) {
        return copy(Paths.get(source));
    }

    /**
     * Returns a configuration which copies contents onto the contents root.
     * @param source the source path
     * @return the configurator
     */
    public static ContentsConfigurator copy(Path source) {
        return new ContentsConfigurator(bundle -> bundle.copy(source));
    }

    /**
     * Returns a configuration which copies contents onto the given destination path.
     * @param source the source path
     * @param onto the destination path (relative from the contents root)
     * @return the configurator
     */
    public static ContentsConfigurator copy(String source, String onto) {
        return copy(Paths.get(source), onto);
    }

    /**
     * Returns a configuration which copies contents onto the given destination path.
     * @param source the source path
     * @param onto the destination path (relative from the contents root)
     * @return the configurator
     */
    public static ContentsConfigurator copy(Path source, String onto) {
        return new ContentsConfigurator(bundle -> bundle.copy(source, onto));
    }

    /**
     * Returns a configuration which extracts archive file onto the contents root.
     * @param source the source path
     * @return the configurator
     */
    public static ContentsConfigurator extract(String source) {
        return extract(Paths.get(source));
    }

    /**
     * Returns a configuration which extracts archive file onto the contents root.
     * @param source the source path
     * @return the configurator
     */
    public static ContentsConfigurator extract(Path source) {
        return new ContentsConfigurator(bundle -> bundle.extract(source));
    }

    /**
     * Returns a configuration which extracts archive file onto the given destination path.
     * @param source the source path
     * @param onto the destination path (relative from the contents root)
     * @return the configurator
     */
    public static ContentsConfigurator extract(String source, String onto) {
        return extract(Paths.get(source), onto);
    }

    /**
     * Returns a configuration which extracts archive file onto the given destination path.
     * @param source the source path
     * @param onto the destination path (relative from the contents root)
     * @return the configurator
     */
    public static ContentsConfigurator extract(Path source, String onto) {
        return new ContentsConfigurator(bundle -> bundle.extract(source, onto));
    }

    @Override
    public void accept(BaseProject<?> project) {
        project.withContents(action);
    }
}
