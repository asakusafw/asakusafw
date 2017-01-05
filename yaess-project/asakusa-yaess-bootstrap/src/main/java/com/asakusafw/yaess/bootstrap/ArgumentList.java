/**
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
package com.asakusafw.yaess.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * The structured argument list.
 * @since 0.8.0
 */
public class ArgumentList {

    private final List<String> standard;

    private final List<ExtendedArgument> extended;

    /**
     * Creates a new instance.
     * @param standard the standard arguments
     * @param extended the extended arguments
     */
    public ArgumentList(List<String> standard, List<ExtendedArgument> extended) {
        Objects.requireNonNull(standard);
        Objects.requireNonNull(extended);
        this.standard = Collections.unmodifiableList(new ArrayList<>(standard));
        this.extended = Collections.unmodifiableList(new ArrayList<>(extended));
    }

    /**
     * Returns the standard arguments.
     * @return the standard arguments
     */
    public List<String> getStandard() {
        return standard;
    }

    /**
     * Returns the standard arguments.
     * @return the standard arguments
     */
    public String[] getStandardAsArray() {
        return standard.toArray(new String[standard.size()]);
    }

    /**
     * Returns the extended arguments.
     * @return the extended arguments
     */
    public List<ExtendedArgument> getExtended() {
        return extended;
    }

    /**
     * Parses the argument list.
     * @param arguments the target list
     * @return the parsed arguments
     */
    public static ArgumentList parse(String... arguments) {
        List<String> standard = new ArrayList<>();
        List<ExtendedArgument> extended = new ArrayList<>();
        LinkedList<String> rest = new LinkedList<>();
        Collections.addAll(rest, arguments);
        while (rest.isEmpty() == false) {
            String s = rest.removeFirst();
            if (s.startsWith(ExtendedArgument.PREFIX) && rest.isEmpty() == false) {
                String name = s.substring(ExtendedArgument.PREFIX.length());
                String value = rest.removeFirst();
                extended.add(new ExtendedArgument(name, value));
            } else {
                standard.add(s);
            }
        }
        return new ArgumentList(standard, extended);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (String s : standard) {
            b.append(s);
            b.append(' ');
        }
        for (ExtendedArgument a : extended) {
            b.append(a);
            b.append(' ');
        }
        if (b.length() > 0) {
            b.deleteCharAt(b.length() - 1);
        }
        return b.toString();
    }
}
