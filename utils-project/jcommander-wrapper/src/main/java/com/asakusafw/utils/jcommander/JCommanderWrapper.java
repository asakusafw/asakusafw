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
package com.asakusafw.utils.jcommander;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;

/**
 * A decorator of JCommander.
 * <ul>
 * <li> accepts nested groups </li>
 * <li> injects {@code JCommander} object into <code>&#64;Inject</code> field of each command </li>
 * <li> consumes <code>&#64;Parameter(help=true)</code> fields </li>
 * </ul>
 * @param <T> the command interface
 * @since 0.10.0
 */
public class JCommanderWrapper<T> implements CommandBuilder<T> {

    static final Logger LOG = LoggerFactory.getLogger(JCommanderWrapper.class);

    private final Builder<T> root;

    /**
     * Creates a new instance.
     * @param programName the program name {@code $0}
     * @param command the root command
     */
    public JCommanderWrapper(String programName, Object command) {
        JCommander commander = new JCommander();
        commander.setProgramName(programName);
        commander.addObject(command);
        this.root = new Builder<>(new String[] { programName }, commander);
    }

    /**
     * Configures this object.
     * @param configurator the configurator
     * @return this
     */
    @Override
    public JCommanderWrapper<T> configure(Consumer<? super CommandBuilder<T>> configurator) {
        root.configure(configurator);
        return this;
    }

    @Override
    public JCommanderWrapper<T> addCommand(T command) {
        root.addCommand(command);
        return this;
    }

    @Override
    public JCommanderWrapper<T> addCommand(String name, T command) {
        root.addCommand(name, command);
        return this;
    }

    @Override
    public JCommanderWrapper<T> addGroup(T command, Consumer<? super CommandBuilder<T>> configurator) {
        root.addGroup(command, configurator);
        return this;
    }

    @Override
    public JCommanderWrapper<T> addGroup(String name, T command, Consumer<? super CommandBuilder<T>> configurator) {
        root.addGroup(name, command, configurator);
        return this;
    }

    /**
     * Parses the program arguments and returns the corresponded command object.
     * @param args the program arguments
     * @return the command object, or {@code empty} if there are no commands to execute (e.g. help)
     * @throws ParameterException if arguments are not valid
     */
    public Optional<T> parse(String... args) {
        String[] expandedArgs = expand(args);
        try {
            root.commander.parse(expandedArgs);
        } catch (ParameterException e) {
            Optional.ofNullable(findCommander()).ifPresent(e::setJCommander);
            throw e;
        }
        JCommander commander = findCommander();
        @SuppressWarnings("unchecked")
        T cmd = (T) getActiveCommand(commander);
        if (cmd != null) {
            validate(commander, expandedArgs);
            return Optional.of(cmd);
        }
        return Optional.empty();
    }

    private static void validate(JCommander commander, String[] args) {
        List<String> dynamics = commander.getParameters().stream()
                .filter(it -> it.isDynamicParameter())
                .flatMap(it -> Arrays.stream(it.getParameter().names()))
                .distinct()
                .collect(Collectors.toList());
        Set<String> statics = commander.getParameters().stream()
                .filter(it -> it.isDynamicParameter() == false)
                .flatMap(it -> Arrays.stream(it.getParameter().names()))
                .collect(Collectors.toSet());
        for (String arg : args) {
            if (arg.equals("--")) {
                break;
            }
            if (arg.startsWith("-")) {
                if (statics.contains(arg) == false
                        && dynamics.stream().anyMatch(arg::startsWith) == false) {
                    ParameterException exc = new ParameterException(MessageFormat.format(
                            "unknown option: {0}",
                            arg));
                    exc.setJCommander(commander);
                    throw exc;
                }
            }
        }
    }

    private String[] expand(String[] args) {
        JCommander commander = findCommander(args);
        Params params = collectParams(commander);
        if (params.names.stream()
                .filter(JCommanderWrapper::isShortNameOption)
                .anyMatch(it -> it.length() > 2)) {
            return args;
        }
        List<String> dynamics = params.dynamicNames.stream()
                .filter(JCommanderWrapper::isShortNameOption)
                .collect(Collectors.toList());

        boolean sawExpand = false;
        boolean sawEscape = false;
        List<String> results = new ArrayList<>();
        for (String arg : args) {
            if (sawEscape) {
                results.add(arg);
            } else {
                if (isShortNameOption(arg)
                        && arg.length() > 2
                        && dynamics.stream().anyMatch(arg::startsWith) == false) {
                    LOG.debug("expand option: {}", arg);
                    for (int i = 1, n = arg.length(); i < n; i++) {
                        sawExpand = true;
                        results.add("-" + arg.charAt(i));
                    }
                } else {
                    sawEscape |= arg.equals("--");
                    results.add(arg);
                }
            }
        }
        if (sawExpand) {
            LOG.debug("expanded command line: {}", results);
            return results.toArray(new String[results.size()]);
        }
        return args;
    }

    private JCommander findCommander() {
        JCommander current = root.commander;
        while (true) {
            String cmd = current.getParsedAlias();
            if (cmd == null) {
                return current;
            } else {
                current = current.getCommands().get(cmd);
            }
        }
    }

    private JCommander findCommander(String[] args) {
        Deque<String> rest = new ArrayDeque<>(Arrays.asList(args));
        JCommander current = root.commander;
        while (rest.isEmpty() == false) {
            String cmd = rest.removeFirst();
            JCommander next = current.getCommands().get(cmd);
            if (next == null) {
                break;
            } else {
                current = next;
            }
        }
        return current;
    }

    private static boolean isShortNameOption(String arg) {
        return arg.startsWith("-") && arg.startsWith("--") == false;
    }

    private static Object getActiveCommand(JCommander commander) {
        List<Object> candidates = commander.getObjects();
        if (candidates.size() != 1) {
            throw new IllegalStateException(commander.getProgramName());
        }
        Object result = candidates.get(0);
        if (isHelp(result)) {
            commander.usage();
            return null;
        }
        inject(result, commander);
        return result;
    }

    private static boolean isHelp(Object object) {
        for (Class<?> current = object.getClass();
                current != Object.class && current != null;
                current = current.getSuperclass()) {
            for (Field f : current.getDeclaredFields()) {
                Parameter parameter = f.getAnnotation(Parameter.class);
                try {
                    if (parameter != null && parameter.help()) {
                        f.setAccessible(true);
                        Object value = f.get(object);
                        if (Objects.equals(value, Boolean.TRUE)) {
                            return true;
                        }
                    }
                    if (f.isAnnotationPresent(ParametersDelegate.class)) {
                        f.setAccessible(true);
                        Object delegate = f.get(object);
                        if (isHelp(delegate)) {
                            return true;
                        }
                    }
                } catch (ReflectiveOperationException e) {
                    LOG.warn("error occurred while searching for help option: {}", object, e);
                    return false;
                }
            }
        }
        return false;
    }

    private static void inject(Object object, JCommander commander) {
        for (Class<?> current = object.getClass();
                current != Object.class && current != null;
                current = current.getSuperclass()) {
            for (Field f : current.getDeclaredFields()) {
                try {
                    if (f.getType() == JCommander.class
                            && f.isAnnotationPresent(Inject.class)) {
                        f.setAccessible(true);
                        f.set(object, commander);
                    }
                    if (f.isAnnotationPresent(ParametersDelegate.class)) {
                        f.setAccessible(true);
                        Object delegate = f.get(object);
                        inject(delegate, commander);
                    }
                } catch (ReflectiveOperationException e) {
                    throw new CommandConfigurationException(MessageFormat.format(
                            "error occurred while injecting JCommander object: {0}#{1}",
                            object.getClass().getName(),
                            f.getName()), e);
                }
            }
        }
    }

    private static Params collectParams(JCommander commander) {
        Params results = new Params();
        commander.getObjects().forEach(it -> collectParams(results, it));
        return results;
    }

    private static void collectParams(Params results, Object object) {
        for (Class<?> current = object.getClass();
                current != Object.class && current != null;
                current = current.getSuperclass()) {
            for (Field f : current.getDeclaredFields()) {
                Optional.ofNullable(f.getAnnotation(Parameter.class)).ifPresent(results::add);
                Optional.ofNullable(f.getAnnotation(DynamicParameter.class)).ifPresent(results::add);
                try {
                    if (f.isAnnotationPresent(ParametersDelegate.class)) {
                        f.setAccessible(true);
                        Object delegate = f.get(object);
                        collectParams(results, delegate);
                    }
                } catch (ReflectiveOperationException e) {
                    LOG.warn("error occurred while analyzing arguments: {}", object, e);
                }
            }
        }
    }

    private static class Params {

        final Set<String> names = new HashSet<>();

        final Set<String> dynamicNames = new HashSet<>();

        Params() {
            return;
        }

        void add(Parameter annotation) {
            for (String name : annotation.names()) {
                names.add(name);
            }
        }

        void add(DynamicParameter annotation) {
            for (String name : annotation.names()) {
                dynamicNames.add(name);
            }
        }
    }

    private static class Builder<T> implements CommandBuilder<T> {

        private final String[] nameSequence;

        final JCommander commander;

        Builder(String[] nameSequence, JCommander commander) {
            this.nameSequence = nameSequence;
            this.commander = commander;
        }

        @Override
        public CommandBuilder<T> addCommand(String name, T command) {
            add(name, command);
            return this;
        }

        @Override
        public CommandBuilder<T> addGroup(String name, T object, Consumer<? super CommandBuilder<T>> configurator) {
            JCommander next = commander.getCommands().get(name);
            if (next == null) {
                next = add(name, object);
            }
            String[] nextSequence = Arrays.copyOf(nameSequence, nameSequence.length + 1);
            nextSequence[nameSequence.length] = name;
            configurator.accept(new Builder<>(nextSequence, next));
            return this;
        }

        JCommander add(String name, Object command) {
            if (commander.getCommands().containsKey(name)) {
                throw new IllegalStateException();
            }
            commander.addCommand(name, command);
            JCommander next = commander.getCommands().get(name);
            next.setProgramName(Stream.concat(
                    Arrays.stream(nameSequence),
                    Stream.of(name)).collect(Collectors.joining(" ")));
            return next;
        }
    }
}
