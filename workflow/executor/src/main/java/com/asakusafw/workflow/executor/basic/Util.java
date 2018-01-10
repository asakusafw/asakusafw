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
package com.asakusafw.workflow.executor.basic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.asakusafw.workflow.model.GraphElement;

final class Util {

    private Util() {
        return;
    }

    static <E extends GraphElement<E>> List<E> sort(Collection<? extends E> elements) {
        List<E> results = new ArrayList<>();
        Queue<E> rest = new ArrayDeque<>(elements);
        Set<E> done = new HashSet<>();
        while (rest.isEmpty() == false) {
            E next = rest.poll();
            if (next.getBlockers().stream().allMatch(it -> done.contains(it))) {
                results.add(next);
                done.add(next);
            } else {
                rest.offer(next);
            }
        }
        return results;
    }

}
