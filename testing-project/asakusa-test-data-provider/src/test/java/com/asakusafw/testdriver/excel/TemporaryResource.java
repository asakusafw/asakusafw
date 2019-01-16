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
package com.asakusafw.testdriver.excel;

import java.io.Closeable;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A temporary resource manager.
 */
public class TemporaryResource extends ExternalResource {

    static final Logger LOG = LoggerFactory.getLogger(TemporaryResource.class);

    private final Deque<Closeable> resources = new LinkedList<>();

    /**
     * Adds a resource to be closed after test was finished.
     * @param <T> the resource type
     * @param resource the resource object
     * @return the resource
     */
    public <T extends Closeable> T bless(T resource) {
        resources.push(resource);
        return resource;
    }

    @Override
    protected void after() {
        while (resources.isEmpty() == false) {
            try {
                resources.pop().close();
            } catch (IOException e) {
                LOG.warn("error occurred while closing resource", e);
            }
        }
    }
}
