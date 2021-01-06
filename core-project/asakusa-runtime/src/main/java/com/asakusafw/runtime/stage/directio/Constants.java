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
package com.asakusafw.runtime.stage.directio;

import org.apache.hadoop.mapreduce.TaskAttemptContext;

final class Constants {

    private static final String NAME_DATASOURCE_OUTPUT_STATS =
            "com.asakusafw.directio.output.datasource.Statistics"; //$NON-NLS-1$

    private static final String NAME_PORT_OUTPUT_STATS =
            "com.asakusafw.directio.output.port.Statistics"; //$NON-NLS-1$

    private static final String SUFFIX_FILE_COUNT = ".files"; //$NON-NLS-1$

    private static final String SUFFIX_RECORD_COUNT = ".records"; //$NON-NLS-1$

    private static final String SUFFIX_BYTE_COUNT = ".bytes"; //$NON-NLS-1$

    static void putCounts(
            TaskAttemptContext context,
            String sourceId, String portId,
            long fileCount, long recordCount, long byteCount) {
        putCounts0(context, NAME_DATASOURCE_OUTPUT_STATS, sourceId, fileCount, recordCount, byteCount);
        putCounts0(context, NAME_PORT_OUTPUT_STATS, portId, fileCount, recordCount, byteCount);
    }

    private static void putCounts0(
            TaskAttemptContext context,
            String groupId, String itemId,
            long fileCount, long recordCount, long byteCount) {
        if (itemId == null) {
            return;
        }
        context.getCounter(groupId, itemId + SUFFIX_FILE_COUNT).increment(fileCount);
        context.getCounter(groupId, itemId + SUFFIX_RECORD_COUNT).increment(recordCount);
        context.getCounter(groupId, itemId + SUFFIX_BYTE_COUNT).increment(byteCount);
    }

    private Constants() {
        return;
    }
}
