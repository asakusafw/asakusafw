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
package com.asakusafw.utils.java.parser.javadoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocText;

/**
 * Mock {@link JavadocBlockParser}.
 */
public class MockJavadocBlockParser extends JavadocBlockParser {

    private String identifier = " MOCK!";
    private Pattern acceptable;

    /**
     * インスタンスを生成する。
     */
    public MockJavadocBlockParser() {
        super();
        this.acceptable = null;
    }

    /**
     * インスタンスを生成する。
     * @param inline インラインパーサ
     * @param inlineRest インラインパーサ
     */
    public MockJavadocBlockParser(JavadocBlockParser inline, JavadocBlockParser...inlineRest) {
        super(list(inline, inlineRest));
        this.acceptable = null;
    }

    private static <T> List<T> list(T t, T...rest) {
        List<T> list = new ArrayList<T>(rest.length + 1);
        list.add(t);
        for (T r: rest) {
            list.add(r);
        }
        return list;
    }

    @Override
    public boolean canAccept(String tag) {
        if (acceptable == null) {
            return true;
        }
        else {
            return acceptable.matcher(tag == null ? "<SYNOPSIS>" : tag).matches();
        }
    }

    /**
     * Sets the acceptable pattern.
     * @param acceptable acceptable pattern to set
     */
    public void setAcceptable(Pattern acceptable) {
        this.acceptable = acceptable;
    }

    /**
     * Sets the identifier.
     * @param identifier identifier to set
     * @throws IllegalArgumentException If {@code identifier} was {@code null}
     */
    public void setIdentifier(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier"); //$NON-NLS-1$
        }
        this.identifier = identifier;
    }

    /**
     * Returns the identifier.
     * @return The identifier
     */
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public IrDocBlock parse(String tag, JavadocScanner scanner) {
        IrDocBlock block = new IrDocBlock();
        block.setTag(tag);
        block.setFragments(Arrays.asList(new IrDocText(identifier)));
        return block;
    }
}
