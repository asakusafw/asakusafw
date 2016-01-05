/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for Hadoop codec names.
 */
public final class CodecNames {

    private static final Map<String, String> CODEC_SHORT_NAMES;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("gzip", "org.apache.hadoop.io.compress.GzipCodec"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("deflate", "org.apache.hadoop.io.compress.DeflateCodec"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("bzip2", "org.apache.hadoop.io.compress.BZip2Codec"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("snappy", "org.apache.hadoop.io.compress.SnappyCodec"); //$NON-NLS-1$ //$NON-NLS-2$
        CODEC_SHORT_NAMES = map;
    }

    private CodecNames() {
        return;
    }

    /**
     * Returns the resolved codec name.
     * @param name the codec name
     * @return the resolved codec name
     */
    public static String resolveCodecName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String resolved = CODEC_SHORT_NAMES.get(name);
        if (resolved != null) {
            return resolved;
        }
        return name;
    }
}
