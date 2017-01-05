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
package com.asakusafw.testdriver.excel;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.rule.ValuePredicate;

/**
 * A repository for {@link ExcelSheetRuleExtension}.
 * @since 0.7.0
 */
class ExcelSheetRuleExtensionRepository implements ExcelSheetRuleExtension {

    private static final Map<ClassLoader, Reference<ExcelSheetRuleExtensionRepository>> CACHE = new WeakHashMap<>();

    /**
     * Returns a {@link ExcelSheetRuleExtensionRepository} for the target context.
     * @param context the target context
     * @return a repository object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ExcelSheetRuleExtensionRepository get(VerifyContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Reference<ExcelSheetRuleExtensionRepository> ref = CACHE.get(context.getTestContext().getClassLoader());
        ExcelSheetRuleExtensionRepository cached = ref == null ? null : ref.get();
        if (cached != null) {
            return cached;
        }
        ExcelSheetRuleExtensionRepository created = newInstance(context);
        setCached(context, created);
        return created;
    }

    static void setCached(VerifyContext context, ExcelSheetRuleExtensionRepository repository) {
        Reference<ExcelSheetRuleExtensionRepository> ref;
        if (repository.getClass().getClassLoader() == ClassLoader.getSystemClassLoader()) {
            ref = new SoftReference<>(repository);
        } else {
            ref = new WeakReference<>(repository);
        }
        CACHE.put(context.getTestContext().getClassLoader(), ref);
    }

    private static ExcelSheetRuleExtensionRepository newInstance(VerifyContext context) {
        ClassLoader classLoader = context.getTestContext().getClassLoader();
        List<ExcelSheetRuleExtension> extensions = new ArrayList<>();
        for (ExcelSheetRuleExtension extension : ServiceLoader.load(ExcelSheetRuleExtension.class, classLoader)) {
            extensions.add(extension);
        }
        return new ExcelSheetRuleExtensionRepository(extensions);
    }

    private final List<? extends ExcelSheetRuleExtension> extensions;

    /**
     * Creates a new instance.
     * @param extensions the extensions
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    ExcelSheetRuleExtensionRepository(List<? extends ExcelSheetRuleExtension> extensions) {
        if (extensions == null) {
            throw new IllegalArgumentException("extensions must not be null"); //$NON-NLS-1$
        }
        this.extensions = extensions;
    }

    @Override
    public ValuePredicate<?> resolve(
            VerifyContext context,
            PropertyName name,
            PropertyType type,
            String expression) throws ExcelRuleExtractor.FormatException {
        for (ExcelSheetRuleExtension extension : extensions) {
            ValuePredicate<?> resolved = extension.resolve(context, name, type, expression);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }
}
