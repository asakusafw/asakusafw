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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public JCommanderWrapper<T> configure(Consumer<? super CommandBuilder<T>> configurator) {
        configurator.accept(this);
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
        try {
            root.commander.parse(args);
        } catch (ParameterException e) {
            Optional.ofNullable(findCommander()).ifPresent(e::setJCommander);
            throw e;
        }
        @SuppressWarnings("unchecked")
        T cmd = (T) getActiveCommand(findCommander());
        return Optional.ofNullable(cmd);
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
