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
package com.asakusafw.yaess.bootstrap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple {@link ExecutionTracker}.
 */
public class SerialExecutionTracker implements ExecutionTracker {

    private static final Map<Id, List<Record>> map = new WeakHashMap<>();

    @Override
    public synchronized void add(Id id, Record record) throws IOException, InterruptedException {
        List<Record> history = map.get(id);
        if (history == null) {
            history = new ArrayList<>();
            map.put(id, history);
        }
        history.add(record);
    }

    /**
     * Clears all tracking records.
     */
    public static synchronized void clear() {
        map.clear();
    }

    /**
     * Returns tracking records.
     * @param id target ID
     * @return results
     */
    public static synchronized List<Record> get(Id id) {
        List<Record> results = map.get(id);
        if (results == null) {
            results = Collections.emptyList();
        }
        return new ArrayList<>(results);
    }
}
