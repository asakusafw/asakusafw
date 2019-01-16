/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.dmdl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.analyzer.DmdlAnalyzer;
import com.asakusafw.dmdl.analyzer.DmdlSemanticException;
import com.asakusafw.dmdl.analyzer.driver.BasicTypeDriver;
import com.asakusafw.dmdl.analyzer.driver.CollectionTypeDriver;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstScript;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.parser.DmdlParser;
import com.asakusafw.dmdl.parser.DmdlSyntaxException;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;

/**
 * Testing utilities for this project.
 */
public abstract class DmdlTesterRoot {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * a test name handler.
     */
    @Rule
    public TestName currentTestName = new TestName();

    /**
     * {@link TypeDriver}s.
     */
    protected final List<TypeDriver> typeDrivers = Stream.of(new BasicTypeDriver(), new CollectionTypeDriver())
            .collect(Collectors.toList());

    /**
     * {@link AttributeDriver}s.
     */
    protected final List<AttributeDriver> attributeDrivers = new ArrayList<>();

    /**
     * Returns a type matcher.
     * @param kind type kind
     * @return the matcher
     */
    protected Matcher<Type> type(BasicTypeKind kind) {
        return new BaseMatcher<Type>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof BasicType) {
                    return ((BasicType) object).getKind() == kind;
                }
                return false;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(kind.name());
            }
        };
    }

    /**
     * Returns a model matcher.
     * @param name the model name
     * @return the matcher
     */
    protected Matcher<ModelSymbol> model(String name) {
        return new BaseMatcher<ModelSymbol>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof ModelSymbol) {
                    return ((ModelSymbol) object).getName().identifier.equals(name);
                }
                return false;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(name);
            }
        };
    }

    /**
     * Returns a property matcher.
     * @param name the property name
     * @return the matcher
     */
    protected Matcher<PropertySymbol> property(String name) {
        return new BaseMatcher<PropertySymbol>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof PropertySymbol) {
                    return ((PropertySymbol) object).getName().identifier.equals(name);
                }
                return false;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(name);
            }
        };
    }

    /**
     * Returns a property matcher.
     * @param modelName the model name
     * @param name the property name
     * @return the matcher
     */
    protected Matcher<PropertySymbol> property(String modelName, String name) {
        return new BaseMatcher<PropertySymbol>() {
            @Override
            public boolean matches(Object object) {
                if (object instanceof PropertySymbol) {
                    PropertySymbol property = (PropertySymbol) object;
                    return property.getName().identifier.equals(name)
                        && property.getOwner().getName().identifier.equals(modelName);
                }
                return false;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendText(MessageFormat.format(
                        "{0}.{1}",
                        modelName,
                        name));
            }
        };
    }

    /**
     * Returns a matcher which tests whether RHS is in LHS.
     * @param <T> the target data type
     * @param matcher RHS
     * @return the matcher
     */
    protected static <T> Matcher<Iterable<T>> has(Matcher<T> matcher) {
        return new BaseMatcher<Iterable<T>>() {
            @Override
            public boolean matches(Object item) {
                for (Object o : (Iterable<?>) item) {
                    if (matcher.matches(o)) {
                        return true;
                    }
                }
                return false;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("has ").appendDescriptionOf(matcher);
            }
        };
    }

    /**
     * Resolves context script.
     * @param lines explicit source lines
     * @return the resolved
     */
    protected DmdlSemantics resolve(String... lines) {
        try {
            return resolve0(lines);
        } catch (DmdlSemanticException e) {
            e.getDiagnostics().forEach(it -> log.error("{}", it));
            throw new AssertionError(e.getDiagnostics());
        }
    }

    /**
     * Assert semantic error should occur.
     * @param lines explicit source lines
     * @return error object
     */
    protected DmdlSemanticException shouldSemanticError(String... lines) {
        try {
            resolve0(lines);
            throw new AssertionError("error should be raised");
        } catch (DmdlSemanticException e) {
            return e;
        }
    }

    /**
     * Resolves context script.
     * @param lines explicit source lines
     * @return the resolved
     * @throws DmdlSemanticException if failed to resolve
     */
    protected DmdlSemantics resolve0(String... lines) throws DmdlSemanticException {
        AstScript script = parse(lines);
        DmdlAnalyzer result = new DmdlAnalyzer(typeDrivers, attributeDrivers);
        for (AstModelDefinition<?> model : script.models) {
            result.addModel(model);
        }
        DmdlSemantics resolved = result.resolve();
        return resolved;
    }

    /**
     * Parses context script.
     * @param lines explicit source lines
     * @return the parsed
     */
    protected AstScript parse(String... lines) {
        try {
            return parse0(lines);
        }
        catch (DmdlSyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Assert syntax error should occur.
     * @return error object
     * @param lines explicit source lines
     */
    protected DmdlSyntaxException shouldSyntaxError(String... lines) {
        try {
            parse0(lines);
            throw new AssertionError("error should be raised");
        } catch (DmdlSyntaxException e) {
            return e;
        }
    }

    /**
     * Parses context script.
     * @param lines explicit source lines
     * @return the parsed
     * @throws DmdlSyntaxException if failed to parse
     */
    protected AstScript parse0(String... lines) throws DmdlSyntaxException {
        return lines.length == 0 ? parseFile(currentTestName.getMethodName()) : parseLines(lines);
    }

    private AstScript parseFile(String resource) throws DmdlSyntaxException {
        try {
            String fileName = resource + ".txt";
            URL url = getClass().getResource(fileName);
            assertThat(fileName, url, is(not(nullValue())));

            URI uri;
            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                uri = null;
            }

            try (Reader r = new InputStreamReader(url.openStream(), "UTF-8");) {
                DmdlParser parser = new DmdlParser();
                AstScript script = parser.parse(r, uri);
                return script;
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static AstScript parseLines(String... lines) throws DmdlSyntaxException {
        try {
            try (Reader r = new StringReader(Arrays.stream(lines)
                    .map(s -> s.replace('\'', '"'))
                    .map(s -> s.replace('`', '"'))
                    .collect(Collectors.joining(System.lineSeparator())))) {
                DmdlParser parser = new DmdlParser();
                AstScript script = parser.parse(r, URI.create("testing"));
                return script;
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
