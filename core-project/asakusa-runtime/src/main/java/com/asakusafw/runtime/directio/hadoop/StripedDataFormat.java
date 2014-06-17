/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectInputFragment;

/**
 * Data model format which has striping information in the format.
 * Client should not implement this interface directly.
 * @param <T> the type of target data model
 * @since 0.7.0
 */
public interface StripedDataFormat<T> extends DataFormat<T> {

    /**
     * Compute {@link DirectInputFragment} for the input.
     * @param context the current input context
     * @return the computed result
     * @throws IOException if failed to compute fragments by I/O error
     * @throws InterruptedException if interrupted while computing fragments
     */
    List<DirectInputFragment> computeInputFragments(InputContext context) throws IOException, InterruptedException;

    /**
     * A input context for computing {@link DirectInputFragment}s.
     * @since 0.7.0
     */
    public static class InputContext {

        private final FileSystem fileSystem;

        private final Collection<? extends FileStatus> inputFiles;

        private final long minimumFragmentSize;

        private final long preferredFragmentSize;

        private final boolean splitBlocks;

        private final boolean combineBlocks;

        /**
         * Creates a new instance.
         * @param inputFiles the input files information
         * @param fileSystem the file system for input files
         * @param minimumFragmentSize the minimum fragment size, or {@code < 0} if fragmentation is not expected
         * @param preferredFragmentSize the preferred fragment size, or {@code < 0} if fragmentation is not expected
         * @param splitBlocks {@code true} to split a file block into multiple fragments
         * @param combineBlocks {@code true} to combine multiple splits into a single fragment
         */
        public InputContext(
                Collection<? extends FileStatus> inputFiles,
                FileSystem fileSystem,
                long minimumFragmentSize,  long preferredFragmentSize,
                boolean splitBlocks, boolean combineBlocks) {
            this.inputFiles = inputFiles;
            this.fileSystem = fileSystem;
            this.minimumFragmentSize = minimumFragmentSize;
            this.preferredFragmentSize = preferredFragmentSize;
            this.splitBlocks = splitBlocks;
            this.combineBlocks = combineBlocks;
        }

        /**
         * Returns the file system for the this datastore.
         * @return the file system object
         */
        public FileSystem getFileSystem() {
            return fileSystem;
        }

        /**
         * @return the inputFiles
         */
        public Collection<? extends FileStatus> getInputFiles() {
            return inputFiles;
        }

        /**
         * Returns the minimum fragment size.
         * @return the minimum fragment size, or {@code < 0} if fragmentation is restricted
         */
        public long getMinimumFragmentSize() {
            return minimumFragmentSize <= 0 ? -1 : minimumFragmentSize;
        }

        /**
         * Returns the preferred fragment size.
         * @return the preferred fragment size, or {@code < 0} if fragmentation is restricted
         */
        public long getPreferredFragmentSize() {
            long min = getMinimumFragmentSize();
            if (min <= 0) {
                return -1;
            }
            return preferredFragmentSize <= 0 ? -1 : preferredFragmentSize;
        }

        /**
         * Returns whether split DFS block into multiple splits for optimization or not.
         * @return the {@code true} to split, otherwise {@code false}
         */
        public boolean isSplitBlocks() {
            return splitBlocks;
        }

        /**
         * Returns whether combines multiple blocks into a fragment for optimization.
         * @return the {@code true} to combine, otherwise {@code false}
         */
        public boolean isCombineBlocks() {
            return combineBlocks;
        }
    }
}
