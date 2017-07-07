/*
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
package com.asakusafw.gradle.tasks.internal

import static org.junit.Assert.*

import java.util.concurrent.Callable

import org.junit.Test

/**
 * Test for {@link ResolutionUtils}.
 */
class ResolutionUtilsTest {

    /**
     * Resolves string value.
     */
    @Test
    public void resolve_string() {
        assert ResolutionUtils.resolveToString('Hello, world!') == 'Hello, world!'
    }

    /**
     * Resolves closure value.
     */
    @Test
    public void resolve_closure() {
        assert ResolutionUtils.resolveToString({ 'Hello, world!' }) == 'Hello, world!'
    }

    /**
     * Resolves closure value.
     */
    @Test
    public void resolve_callable() {
        assert ResolutionUtils.resolveToString({ 'Hello, world!' } as Callable<?>) == 'Hello, world!'
    }

    /**
     * Resolves other value.
     */
    @Test
    public void resolve_object() {
        assert ResolutionUtils.resolveToString(100) == String.valueOf(100)
    }

    /**
     * Resolves null.
     */
    @Test
    public void resolve_null() {
        assert ResolutionUtils.resolveToString(null) == null
    }

    /**
     * Resolves list of strings.
     */
    @Test
    public void resolve_list_string() {
        List<String> values = ResolutionUtils.resolveToStringList([
            'Hello1',
            'Hello2',
            'Hello3',
        ])
        assert values == ['Hello1', 'Hello2', 'Hello3']
    }

    /**
     * Resolves list w/ nulls.
     */
    @Test
    public void resolve_list_w_null() {
        List<String> values = ResolutionUtils.resolveToStringList([
            'Hello1',
            null,
            'Hello3',
        ])
        assert values == ['Hello1', 'Hello3']
    }

    /**
     * Resolves list w/ many types.
     */
    @Test
    public void resolve_list_types() {
        List<String> values = ResolutionUtils.resolveToStringList([
            100,
            { 'Hello2' },
            { 'Hello3' } as Callable<?>,
            [ 'n1', { 'n2' } ],
        ])
        assert values == ['100', 'Hello2', 'Hello3', 'n1', 'n2']
    }

    /**
     * Resolves map of strings.
     */
    @Test
    public void resolve_map_string() {
        Map<String, String> values = ResolutionUtils.resolveToStringMap([
            'k1' : 'v1',
            'k2' : 'v2',
            'k3' : 'v3',
        ])
        assert values == ['k1' : 'v1', 'k2' : 'v2', 'k3' : 'v3']
    }

    /**
     * Resolves map w/ null keys.
     */
    @Test
    public void resolve_map_w_null_key() {
        Map<Object, Object> origin = [:]
        origin.put 'k1', 'v1'
        origin.put null, 'v2'
        origin.put 'k3', 'v3'

        Map<String, String> values = ResolutionUtils.resolveToStringMap(origin)
        assert values == ['k1' : 'v1', 'k3' : 'v3']
    }

    /**
     * Resolves map of many types.
     */
    @Test
    public void resolve_map_any() {
        Map<Object, Object> origin = [:]
        origin.put({ 'k1' }, { 100 })
        origin.put({ 'k2' } as Callable<?>, { null })
        origin.put({ null }, 'v3')

        Map<String, String> values = ResolutionUtils.resolveToStringMap(origin)
        assert values == ['k1' : '100']
    }
}
