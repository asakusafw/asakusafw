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
package com.asakusafw.compiler.batch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * Analyzes batch classes (which annotated with {@link Batch}).
 */
public final class BatchDriver {

    static final Logger LOG = LoggerFactory.getLogger(BatchDriver.class);

    private final Class<? extends BatchDescription> description;

    private BatchClass batchClass;

    private final List<String> diagnostics;

    private BatchDriver(Class<? extends BatchDescription> description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        this.description = description;
        this.diagnostics = new ArrayList<>();
    }

    /**
     * Creates a new instance.
     * @param description the target batch class
     * @return the created instance
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static BatchDriver analyze(Class<? extends BatchDescription> description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        BatchDriver analyzer = new BatchDriver(description);
        analyzer.analyze();
        return analyzer;
    }

    /**
     * Returns information of the target batch class.
     * @return information of the target batch class, or {@code null} if the target batch class is not valid
     */
    public BatchClass getBatchClass() {
        return batchClass;
    }

    /**
     * Returns the target batch class.
     * @return the target batch class
     */
    public Class<? extends BatchDescription> getDescription() {
        return this.description;
    }

    /**
     * Returns the diagnostics messages.
     * @return the diagnostics messages
     */
    public List<String> getDiagnostics() {
        return diagnostics;
    }

    private void analyze() {
        Batch config = findConfig();
        BatchDescription instance = describe();
        if (hasError()) {
            return;
        }
        this.batchClass = new BatchClass(config, instance);
    }

    private Batch findConfig() {
        if (description.getEnclosingClass() != null) {
            error(null, Messages.getString("BatchDriver.errorEnclosingClass")); //$NON-NLS-1$
        }
        if (Modifier.isPublic(description.getModifiers()) == false) {
            error(null, Messages.getString("BatchDriver.errorNotPublic")); //$NON-NLS-1$
        }
        if (Modifier.isAbstract(description.getModifiers())) {
            error(null, Messages.getString("BatchDriver.errorAbstract")); //$NON-NLS-1$
        }
        Batch conf = description.getAnnotation(Batch.class);
        if (conf == null) {
            error(null, Messages.getString("BatchDriver.errorMissingAnnotation")); //$NON-NLS-1$
        }
        return conf;
    }

    private BatchDescription describe() {
        Constructor<? extends BatchDescription> ctor;
        try {
            ctor = description.getConstructor();
        } catch (Exception e) {
            error(
                    e,
                    Messages.getString("BatchDriver.errorMissingDefaultConstructor"), //$NON-NLS-1$
                    description.getName(),
                    e.toString());
            return null;
        }
        BatchDescription instance;
        try {
            instance = ctor.newInstance();
        } catch (Exception e) {
            error(e, Messages.getString("BatchDriver.errorFailedToInstantiate"), //$NON-NLS-1$
                    description.getName(), e.toString());
            return null;
        }
        try {
            instance.start();
        } catch (Exception e) {
            error(e, Messages.getString("BatchDriver.errorFailedToAnalyze"), //$NON-NLS-1$
                    description.getName(), e.toString());
            return null;
        }
        return instance;
    }

    private void error(
            Throwable reason,
            String message,
            Object... args) {
        String text = format(message, args);
        diagnostics.add(text);
        if (reason == null) {
            LOG.error(text);
        } else {
            LOG.error(text, reason);
        }
    }

    private String format(String message, Object... args) {
        assert message != null;
        assert args != null;
        if (args.length == 0) {
            return message;
        } else {
            return MessageFormat.format(message, args);
        }
    }

    /**
     * Returns whether this analysis result contains any erroneous information or not.
     * @return {@code true} if this contains any erroneous information, otherwise {@code false}
     */
    public boolean hasError() {
        return getDiagnostics().isEmpty() == false;
    }
}
