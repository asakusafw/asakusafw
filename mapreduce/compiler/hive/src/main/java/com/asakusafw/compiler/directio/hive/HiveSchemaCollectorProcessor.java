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
package com.asakusafw.compiler.directio.hive;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.directio.hive.info.InputInfo;
import com.asakusafw.directio.hive.info.LocationInfo;
import com.asakusafw.directio.hive.info.OutputInfo;
import com.asakusafw.directio.hive.info.TableInfo;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Collects Hive table definition and puts their schema information into compilation results.
 * @since 0.8.1
 */
public class HiveSchemaCollectorProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(HiveSchemaCollectorProcessor.class);

    /**
     * The schema output base path.
     */
    public static final String PATH_BASE = "etc/hive-schema";

    /**
     * The input schema file path.
     */
    public static final String PATH_INPUT = PATH_BASE + "/input.json";

    /**
     * The output schema file path.
     */
    public static final String PATH_OUTPUT = PATH_BASE + "/output.json";

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
   }

    @Override
    public void process(Workflow workflow) throws IOException {
        LOG.debug("collecting Hive inputs/outputs");
        Context context = new Context();
        processBatch(context, workflow);

        List<InputInfo> inputs = normalize(context.inputs);
        LOG.debug("generating Hive input table schema: {} entries", inputs.size());
        try (OutputStream stream = getEnvironment().openResource(PATH_INPUT)) {
            Persistent.write(InputInfo.class, inputs, stream);
        }

        List<OutputInfo> outputs = normalize(context.outputs);
        LOG.debug("generating Hive input table schema: {} entries", inputs.size());
        try (OutputStream stream = getEnvironment().openResource(PATH_OUTPUT)) {
            Persistent.write(OutputInfo.class, outputs, stream);
        }
    }

    private void processBatch(Context context, Workflow workflow) {
        for (Workflow.Unit unit : workflow.getGraph().getNodeSet()) {
            JobflowModel jobflow = (JobflowModel) unit.getProcessed();
            processJobflow(context, jobflow);
        }
    }

    private void processJobflow(Context context, JobflowModel jobflow) {
        LOG.debug("collectiong Hive inputs/outputs from jobflow: {}", jobflow.getFlowId());
        for (JobflowModel.Import node : jobflow.getImports()) {
            ImporterDescription description = node.getDescription().getImporterDescription();
            if (description instanceof DirectFileInputDescription) {
                processInput(context, (DirectFileInputDescription) description);
            }
        }
        for (JobflowModel.Export node : jobflow.getExports()) {
            ExporterDescription description = node.getDescription().getExporterDescription();
            if (description instanceof DirectFileOutputDescription) {
                processOutput(context, (DirectFileOutputDescription) description);
            }
        }
    }

    private void processInput(Context context, DirectFileInputDescription description) {
        TableInfo info = processDataFormat(description.getFormat());
        if (info == null) {
            return;
        }
        context.inputs.add(new InputInfo(
                new LocationInfo(description.getBasePath(), description.getResourcePattern()),
                info));
    }

    private void processOutput(Context context, DirectFileOutputDescription description) {
        TableInfo info = processDataFormat(description.getFormat());
        if (info == null) {
            return;
        }
        context.outputs.add(new OutputInfo(
                new LocationInfo(description.getBasePath(), description.getResourcePattern()),
                info));
    }

    private TableInfo processDataFormat(Class<? extends DataFormat<?>> format) {
        if (TableInfo.Provider.class.isAssignableFrom(format) == false) {
            LOG.debug("not Hive table: {}", format.getName());
            return null;
        }
        LOG.debug("found Hive table: {}", format.getName());
        TableInfo.Provider provider;
        try {
            provider = format.asSubclass(TableInfo.Provider.class)
                    .getConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            LOG.warn(MessageFormat.format(
                    "error occurred while extracting Hive table schema: {0}",
                    format.getName()), e);
            return null;
        }
        TableInfo schema = provider.getSchema();
        if (schema == null) {
            return null;
        }
        LOG.debug("extracted Hive table: {} ({})", schema, provider);
        return schema;
    }

    private <T extends TableInfo.Provider> List<T> normalize(List<T> elements) {
        if (elements.size() <= 1) {
            return elements;
        }
        Set<T> saw = new HashSet<>();
        List<T> normalized = new ArrayList<>();
        for (T element : elements) {
            if (saw.contains(element)) {
                continue;
            }
            saw.add(element);
            normalized.add(element);
        }
        Collections.sort(normalized, (o1, o2) -> o1.getSchema().getName().compareTo(o2.getSchema().getName()));
        return normalized;
    }

    private static class Context {

        final List<InputInfo> inputs = new ArrayList<>();

        final List<OutputInfo> outputs = new ArrayList<>();

        Context() {
            return;
        }
    }
}
