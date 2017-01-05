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
package com.asakusafw.utils.java.jsr199.testing;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * Mock Annotation Processor.
 */
@SupportedAnnotationTypes({
    "java.lang.SuppressWarnings"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MockProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<Element> elements = new HashSet<>();
        for (TypeElement annotationType : annotations) {
            Set<? extends Element> annotated = env.getElementsAnnotatedWith(annotationType);
            elements.addAll(annotated);
        }
        if (elements.isEmpty() == false) {
            try {
                JavaFileObject file = processingEnv.getFiler().createSourceFile("Generated");
                try (PrintWriter pw = new PrintWriter(file.openWriter())) {
                    pw.println("public class Generated {");
                    pw.println("    public static final String[] ELEMENTS = {");
                    for (Element element : elements) {
                        pw.println("        \"" + element.getSimpleName() + "\",");
                    }
                    pw.println("    };");
                    pw.println("}");
                }

                FileObject resource = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "com.example",
                        "example.properties");
                try (Writer rw = resource.openWriter()) {
                    rw.write("example=OK");
                }
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return false;
    }

    static final Set<String> load(ClassLoader loader) {
        try {
            Class<?> klass = Class.forName("Generated", true, loader);
            Field field = klass.getDeclaredField("ELEMENTS");
            return new HashSet<>(Arrays.asList((String[]) field.get(null)));
        } catch (ClassNotFoundException e) {
            return Collections.emptySet();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
