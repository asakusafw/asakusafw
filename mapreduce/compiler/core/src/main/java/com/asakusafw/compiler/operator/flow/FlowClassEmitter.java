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
package com.asakusafw.compiler.operator.flow;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;

/**
 * Emits support classes for flow-part classes.
 * @since 0.1.0
 * @version 0.7.0
 */
public class FlowClassEmitter {

    static final Logger LOG = LoggerFactory.getLogger(FlowClassEmitter.class);

    private static final String KEY_SUFFIX_FACTORY = "FACTORY"; //$NON-NLS-1$

    private final OperatorCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowClassEmitter(OperatorCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Emits new classes for the target flow-part class.
     * @param aClass the target flow-part class
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void emit(FlowPartClass aClass) {
        assert aClass != null;
        String key = getResourceKey(aClass, KEY_SUFFIX_FACTORY);
        if (key != null && environment.isResourceGenerated(key)) {
            return;
        }
        ModelFactory f = environment.getFactory();
        PackageDeclaration packageDecl = getPackage(f, aClass);
        ImportBuilder imports = getImportBuilder(f, packageDecl);
        FlowFactoryClassGenerator generator = new FlowFactoryClassGenerator(
                environment,
                f,
                imports,
                aClass);
        TypeDeclaration type = generator.generate();
        List<ImportDeclaration> decls = imports.toImportDeclarations();

        try {
            emit(key, f, packageDecl, decls, type, aClass.getElement());
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
            environment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            Messages.getString("FlowClassEmitter.errorFailedToCreateOperatorFactory"), //$NON-NLS-1$
                            aClass.getElement().getQualifiedName().toString(),
                            e.getMessage()));
        }
    }

    private void emit(
            String key,
            ModelFactory factory,
            PackageDeclaration packageDecl,
            List<ImportDeclaration> importDecls,
            TypeDeclaration typeDecl,
            TypeElement originating) throws IOException {
        CompilationUnit unit = factory.newCompilationUnit(
                packageDecl,
                importDecls,
                Collections.singletonList(typeDecl));
        try {
            environment.emit(unit, originating);
            if (key != null) {
                environment.setResourceGenerated(key);
            }
        } catch (FilerException e) {
            LOG.debug(MessageFormat.format(
                    "{0} has been already created in this session", //$NON-NLS-1$
                    typeDecl.getName().toNameString()), e);
        }
    }

    private String getResourceKey(FlowPartClass aClass, String suffix) {
        assert aClass != null;
        return String.format("%s::%s", aClass.getElement().getQualifiedName(), suffix); //$NON-NLS-1$
    }

    private PackageDeclaration getPackage(
            ModelFactory factory,
            FlowPartClass aClass) {
        PackageElement parent = (PackageElement) aClass.getElement().getEnclosingElement();
        return new Jsr269(factory).convert(parent);
    }

    private ImportBuilder getImportBuilder(
            ModelFactory factory,
            PackageDeclaration packageDecl) {
        assert factory != null;
        assert packageDecl != null;
        return new ImportBuilder(factory, packageDecl, Strategy.TOP_LEVEL);
    }
}
