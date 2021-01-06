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
package com.asakusafw.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;

/**
 * An abstract super class of reading/writing data model objects.
 * @param <T> the target data model type
 * @since 0.1.0
 */
public abstract class ModelIoFactory<T> {

    static final Log LOG = LogFactory.getLog(ModelIoFactory.class);

    /**
     * The qualified data model class name pattern in regex.
     * <ul>
     * <li> <code>$1</code> - base package name </li>
     * <li> <code>$2</code> - simple class name </li>
     * </ul>
     */
    static final Pattern MODEL_CLASS_NAME_PATTERN = Pattern.compile(
            "(.*)\\.model\\.([^\\.]+)$"); //$NON-NLS-1$

    /**
     * The qualified name pattern of {@link ModelInput} in MessageFormat.
     * <ul>
     * <li> <code>{0}</code> - base package name </li>
     * <li> <code>{1}</code> - simple class name </li>
     * </ul>
     * @deprecated Use {@link ModelInputLocation} to specify {@link ModelInput} instead
     */
    @Deprecated
    public static final String MODEL_INPUT_CLASS_FORMAT = "{0}.io.{1}ModelInput"; //$NON-NLS-1$

    /**
     * The qualified name pattern of {@link ModelOutput} in MessageFormat.
     * <ul>
     * <li> <code>{0}</code> - base package name </li>
     * <li> <code>{1}</code> - simple class name </li>
     * </ul>
     * @deprecated Use {@link ModelOutputLocation} to specify {@link ModelOutput} instead
     */
    @Deprecated
    public static final String MODEL_OUTPUT_CLASS_FORMAT = "{0}.io.{1}ModelOutput"; //$NON-NLS-1$

    private final Class<T> modelClass;

    /**
     * Creates a new instance.
     * @param modelClass the data model class
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ModelIoFactory(Class<T> modelClass) {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        this.modelClass = modelClass;
    }

    /**
     * Returns the target data model class.
     * @return the target data model class
     */
    protected Class<T> getModelClass() {
        return modelClass;
    }

    /**
     * Creates a new data model object.
     * @return the created object
     * @throws IOException if failed to create a new object
     */
    public T createModelObject() throws IOException {
        try {
            return modelClass.newInstance();
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Cannot create a new model object for {0}",
                    getClass().getName()),
                    e);
        }
    }

    /**
     * Creates a new {@link ModelInput} for the target {@link InputStream}.
     * @param in an input stream that provides serialized data model objects
     * @return the created instance
     * @throws IOException if failed to initialize the {@code ModelInput}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ModelInput<T> createModelInput(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        RecordParser parser = createRecordParser(in);
        return createModelInput(parser);
    }

    /**
     * Creates a new {@link ModelInput} for the target {@link RecordParser}.
     * @param parser a parser that provides records of data model objects
     * @return the created instance
     * @throws IOException if failed to initialize the {@code ModelInput}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ModelInput<T> createModelInput(RecordParser parser) throws IOException {
        if (parser == null) {
            throw new IllegalArgumentException("parser must not be null"); //$NON-NLS-1$
        }
        Class<?> inputClass;
        try {
            inputClass = findModelInputClass();
        } catch (ClassNotFoundException e) {
            throw new IOException(MessageFormat.format(
                    "Cannot find a model input for {0}",
                    modelClass.getName()),
                    e);
        }
        try {
            Constructor<?> ctor = inputClass.getConstructor(RecordParser.class);
            @SuppressWarnings("unchecked")
            ModelInput<T> instance = (ModelInput<T>) ctor.newInstance(parser);
            return instance;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Cannot initialize a model input for {0}",
                    modelClass.getName()),
                    e);
        }
    }

    /**
     * Creates a new {@link ModelOutput} for the target {@link OutputStream}.
     * @param out an output stream that accepts serialized data model objects
     * @return the created instance
     * @throws IOException if failed to initialize the {@code ModelOutput}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ModelOutput<T> createModelOutput(OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out must not be null"); //$NON-NLS-1$
        }
        RecordEmitter emitter = createRecordEmitter(out);
        return createModelOutput(emitter);
    }

    /**
     * Creates a new {@link ModelOutput} for the target {@link RecordEmitter}.
     * @param emitter an emitter that accepts data model objects
     * @return the created instance
     * @throws IOException if failed to initialize the {@code ModelOutput}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ModelOutput<T> createModelOutput(RecordEmitter emitter) throws IOException {
        if (emitter == null) {
            throw new IllegalArgumentException("emitter must not be null"); //$NON-NLS-1$
        }
        Class<?> outputClass;
        try {
            outputClass = findModelOutputClass();
        } catch (ClassNotFoundException e) {
            throw new IOException(MessageFormat.format(
                    "Cannot find a model output for {0}",
                    modelClass.getName()),
                    e);
        }
        try {
            Constructor<?> ctor = outputClass.getConstructor(RecordEmitter.class);
            @SuppressWarnings("unchecked")
            ModelOutput<T> instance = (ModelOutput<T>) ctor.newInstance(emitter);
            return instance;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Cannot initialize a model output for {0}",
                    modelClass.getName()),
                    e);
        }
    }

    /**
     * Creates a new default {@link RecordParser}.
     * @param in the source input
     * @return the created object
     * @throws IOException if failed to initialize the parser
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    protected abstract RecordParser createRecordParser(InputStream in) throws IOException;

    /**
     * Creates a new default {@link RecordEmitter}.
     * @param out the target output
     * @return the created object
     * @throws IOException if failed to initialize the emitter
     * @throws IllegalArgumentException if the parameter {@code null}
     */
    protected abstract RecordEmitter createRecordEmitter(OutputStream out) throws IOException;

    /**
     * Returns the {@link ModelInput} class for this factory.
     * @return the {@link ModelInput} class
     * @throws ClassNotFoundException if the target class is not found
     * @see ModelInputLocation
     */
    protected Class<?> findModelInputClass() throws ClassNotFoundException {
        ModelInputLocation annotation = modelClass.getAnnotation(ModelInputLocation.class);
        if (annotation != null) {
            return annotation.value();
        }
        LOG.warn(MessageFormat.format(
                "Data model class \"{0}\" does not have annotation \"{1}\"",
                modelClass.getName(),
                ModelInputLocation.class.getName(),
                ModelInput.class.getSimpleName()));
        return findClassFromModel(MODEL_INPUT_CLASS_FORMAT);
    }

    /**
     * Returns the {@link ModelOutput} class for this factory.
     * @return the {@link ModelOutput} class
     * @throws ClassNotFoundException if the target class is not found
     * @see ModelInputLocation
     */
    protected Class<?> findModelOutputClass() throws ClassNotFoundException {
        ModelOutputLocation annotation = modelClass.getAnnotation(ModelOutputLocation.class);
        if (annotation != null) {
            return annotation.value();
        }
        LOG.warn(MessageFormat.format(
                "Data model class \"{0}\" does not have annotation \"{1}\"",
                modelClass.getName(),
                ModelOutputLocation.class.getName(),
                ModelOutput.class.getSimpleName()));
        return findClassFromModel(MODEL_OUTPUT_CLASS_FORMAT);
    }

    private Class<?> findClassFromModel(String format) throws ClassNotFoundException {
        Matcher m = MODEL_CLASS_NAME_PATTERN.matcher(modelClass.getName());
        if (m.matches() == false) {
            throw new ClassNotFoundException(MessageFormat.format(
                    "Invalid model class name pattern: {0}",
                    modelClass.getName()));
        }
        String qualifier = m.group(1);
        String simpleName = m.group(2);

        String result = MessageFormat.format(format, qualifier, simpleName);

        return Class.forName(result, false, modelClass.getClassLoader());
    }
}
