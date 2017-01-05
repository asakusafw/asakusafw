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
package com.asakusafw.compiler.tool.batchspec;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.tool.batchspec.BatchSpec.Parameter;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Outputs batch specification (comments, parameters).
 * @since 0.5.0
 */
public class BatchSpecProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(BatchSpecProcessor.class);

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        Class<? extends BatchDescription> description = workflow.getDescription().getClass();
        process(description);
    }

    void process(Class<? extends BatchDescription> description) throws IOException {
        LOG.info(MessageFormat.format(
                Messages.getString("BatchSpecProcessor.infoExtractingMetadata"), //$NON-NLS-1$
                description.getName()));
        BatchSpec spec = toSpec(description);
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        try (OutputStream output = getEnvironment().openResource(Constants.PATH);
                Writer writer = new OutputStreamWriter(output, Constants.ENCODING)) {
            gson.toJson(spec, BatchSpec.class, writer);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} => {}", description.getName(), gson.toJson(spec, BatchSpec.class)); //$NON-NLS-1$
        }
    }

    private BatchSpec toSpec(Class<? extends BatchDescription> description) {
        Batch info = description.getAnnotation(Batch.class);
        String batchId = getEnvironment().getConfiguration().getBatchId();
        if (info == null) {
            LOG.warn(MessageFormat.format(
                    Messages.getString("BatchSpecProcessor.warnMissingMetadata"), //$NON-NLS-1$
                    Batch.class.getName(),
                    description.getName()));
            return new BatchSpec(batchId);
        } else {
            String comment = normalizeComment(info.comment());
            boolean strict = info.strict();
            List<Parameter> parameters = toParameters(description, info.parameters());
            return new BatchSpec(info.name(), comment, strict, parameters);
        }
    }

    private List<Parameter> toParameters(
            Class<? extends BatchDescription> description,
            com.asakusafw.vocabulary.batch.Batch.Parameter[] parameters) {
        Set<String> keys = new HashSet<>();
        List<Parameter> results = new ArrayList<>();
        for (com.asakusafw.vocabulary.batch.Batch.Parameter parameter : parameters) {
            String key = parameter.key();
            if (keys.contains(key)) {
                getEnvironment().error(
                        Messages.getString("BatchSpecProcessor.errorConflictParameter"), //$NON-NLS-1$
                        description.getName(),
                        key);
                continue;
            }
            String comment = normalizeComment(parameter.comment());
            boolean required = parameter.required();
            String pattern = normalizePattern(description, parameter);
            results.add(new Parameter(key, comment, required, pattern));
            keys.add(key);
        }
        return results;
    }

    private String normalizeComment(String string) {
        return string.isEmpty() ? null : string;
    }

    private String normalizePattern(
            Class<? extends BatchDescription> description,
            com.asakusafw.vocabulary.batch.Batch.Parameter parameter) {
        String key = parameter.key();
        String pattern = parameter.pattern();
        try {
            Pattern.compile(pattern); // check only
            return pattern;
        } catch (PatternSyntaxException e) {
            LOG.warn(MessageFormat.format(
                    Messages.getString("BatchSpecProcessor.errorMalformedParameter"), //$NON-NLS-1$
                    description.getName(),
                    key,
                    pattern), e);
            return Batch.DEFAULT_PARAMETER_VALUE_PATTERN;
        }
    }
}
