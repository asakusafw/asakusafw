/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Moderates input/output in testing.
 * @since 0.2.3
 */
public class TestModerator {

    private final TestToolRepository repository;

    private final TestContext context;

    /**
     * Creates a new instance which uses registerd services.
     * @param repository the tool repository
     * @param context current context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestModerator(TestToolRepository repository, TestContext context) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.repository = repository;
        this.context = context;
    }

    /**
     * Truncates the target importer's input.
     * @param description target importer
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void truncate(ImporterDescription description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        getDriver(description).truncate(description, context);
    }

    /**
     * Truncates the target exporter's output.
     * @param description target importer
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void truncate(ExporterDescription description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        getDriver(description).truncate(description, context);
    }

    /**
     * Prepares the target importer's input using the specified source.
     * @param modelClass class of data model
     * @param description target importer
     * @param source test data
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepare(
            Class<?> modelClass,
            ImporterDescription description,
            DataModelSourceFactory source) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = repository.toDataModelDefinition(modelClass);
        prepare(definition, description, source);
    }

    /**
     * Prepares the target exporter's output using the specified source.
     * @param modelClass class of data model
     * @param description target importer
     * @param source test data
     * @throws IOException if failed to prepare the importer
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void prepare(
            Class<?> modelClass,
            ExporterDescription description,
            DataModelSourceFactory source) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = repository.toDataModelDefinition(modelClass);
        prepare(definition, description, source);
    }

    /**
     * Inspects the target exporter's output using specified expected data and rule.
     * @param modelClass class of data model
     * @param description target exporter
     * @param verifyContext current verification context
     * @param verifier verifier factory
     * @return detected invalid differences
     * @throws IOException if failed to inspect the result
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<Difference> inspect(
            Class<?> modelClass,
            ExporterDescription description,
            VerifyContext verifyContext,
            VerifierFactory verifier) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (verifyContext == null) {
            throw new IllegalArgumentException("verifyContext must not be null"); //$NON-NLS-1$
        }
        if (verifier == null) {
            throw new IllegalArgumentException("verifier must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = repository.toDataModelDefinition(modelClass);
        return inspect(definition, description, verifyContext, verifier);
    }

    /**
     * Saves the target exporter's output into the specified sink.
     * @param modelClass class of data model
     * @param description target exporter
     * @param resultDataSink the result sink
     * @throws IOException if failed to save the result
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void save(
            Class<?> modelClass,
            ExporterDescription description,
            DataModelSinkFactory resultDataSink) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (resultDataSink == null) {
            throw new IllegalArgumentException("resultDataSink must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = repository.toDataModelDefinition(modelClass);
        DataModelSource source = getDriver(description).createSource(definition, description, context);
        try {
            DataModelSink sink = resultDataSink.createSink(definition, context);
            try {
                while (true) {
                    DataModelReflection next = source.next();
                    if (next == null) {
                        break;
                    }
                    sink.put(next);
                }
            } finally {
                sink.close();
            }
        } finally {
            source.close();
        }
    }

    /**
     * Saves difference list into the specified sink.
     * @param modelClass class of data model
     * @param differences target list
     * @param differenceSink the difference sink
     * @throws IOException if failed to save the differences
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void save(
            Class<?> modelClass,
            Iterable<Difference> differences,
            DifferenceSinkFactory differenceSink) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (differences == null) {
            throw new IllegalArgumentException("differences must not be null"); //$NON-NLS-1$
        }
        if (differenceSink == null) {
            throw new IllegalArgumentException("differenceSink must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = repository.toDataModelDefinition(modelClass);
        DifferenceSink sink = differenceSink.createSink(definition, context);
        try {
            for (Difference difference : differences) {
                sink.put(difference);
            }
        } finally {
            sink.close();
        }
    }

    private ImporterPreparator<? super ImporterDescription> getDriver(ImporterDescription description) {
        assert description != null;
        return repository.getImporterPreparator(description);
    }

    private ExporterRetriever<? super ExporterDescription> getDriver(ExporterDescription description) {
        assert description != null;
        return repository.getExporterRetriever(description);
    }

    private <T> void prepare(
            DataModelDefinition<T> definition,
            ImporterDescription description,
            DataModelSourceFactory source) throws IOException {
        assert definition != null;
        assert description != null;
        assert source != null;
        ModelOutput< T> output = getDriver(description).createOutput(definition, description, context);
        prepare(definition, output, source);
    }

    private <T> void prepare(
            DataModelDefinition<T> definition,
            ExporterDescription desctipion,
            DataModelSourceFactory source) throws IOException {
        assert definition != null;
        assert desctipion != null;
        assert source != null;
        ModelOutput<T> output = getDriver(desctipion).createOutput(definition, desctipion, context);
        prepare(definition, output, source);
    }

    private <T> void prepare(
            DataModelDefinition<T> definition,
            ModelOutput<T> output,
            DataModelSourceFactory source) throws IOException {
        assert definition != null;
        assert output != null;
        assert source != null;
        try {
            DataModelSource input = source.createSource(definition, context);
            if (input == null) {
                throw new IOException(MessageFormat.format(
                        "Failed to open source: {0} (handler not found)",
                        source));
            }
            try {
                while (true) {
                    DataModelReflection next = input.next();
                    if (next == null) {
                        break;
                    }
                    T object = definition.toObject(next);
                    output.write(object);
                }
            } finally {
                input.close();
            }
        } finally {
            output.close();
        }
    }

    private <T> List<Difference> inspect(
            DataModelDefinition<T> definition,
            ExporterDescription description,
            VerifyContext verifyContext,
            VerifierFactory verifier) throws IOException {
        assert definition != null;
        assert description != null;
        assert verifier != null;
        List<Difference> results;
        DataModelSource target = getDriver(description).createSource(definition, description, context);
        try {
            Verifier engine = verifier.createVerifier(definition, verifyContext);
            try {
                results = engine.verify(target);
            } finally {
                engine.close();
            }
        } finally {
            target.close();
        }
        Collections.sort(results, new DifferenceComparator(definition));
        return results;
    }

    private static class DifferenceComparator implements Comparator<Difference> {

        private final DataModelDefinition<?> definition;

        DifferenceComparator(DataModelDefinition<?> definition) {
            assert definition != null;
            this.definition = definition;
        }

        @Override
        public int compare(Difference o1, Difference o2) {
            DataModelReflection r1 = key(o1);
            DataModelReflection r2 = key(o2);
            if (r1 == null && r2 == null) {
                return 0;
            } else if (r1 == null) {
                return -1;
            } else if (r2 == null) {
                return +1;
            }
            for (PropertyName name : definition.getProperties()) {
                Class<?> type = definition.getType(name).getRepresentation();
                if (Comparable.class.isAssignableFrom(type) == false) {
                    // FIXME compare other kinds
                    continue;
                }
                Object p1 = r1.getValue(name);
                Object p2 = r2.getValue(name);
                if (p1 == null && p2 != null) {
                    return -1;
                } else if (p1 != null && p2 == null) {
                    return +1;
                }
                int cmp = compareProperty(type.asSubclass(Comparable.class), p1, p2);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private <T extends Comparable> int compareProperty(Class<T> type, Object p1, Object p2) {
            if (p1 == null && p2 == null) {
                return 0;
            } else if (p1 == null) {
                return -1;
            } else if (p2 == null) {
                return +1;
            }
            T o1 = type.cast(p1);
            T o2 = type.cast(p2);
            return o1.compareTo(o2);
        }

        private DataModelReflection key(Difference difference) {
            assert difference != null;
            DataModelReflection expected = difference.getExpected();
            DataModelReflection actual = difference.getActual();
            if (expected != null) {
                return expected;
            } else if (actual != null) {
                return actual;
            } else {
                return null;
            }
        }
    }
}
