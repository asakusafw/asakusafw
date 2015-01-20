/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

/**
 * Test for {@link ServiceProfile}.
 */
public class ServiceProfileTest {

    /**
     * Simple loading.
     */
    @Test
    public void load() {
        Properties prop = new Properties();
        prop.setProperty("mock1", MockService.class.getName());
        prop.setProperty("mock2", "");
        ClassLoader cl = getClass().getClassLoader();
        ServiceProfile<Service> service = ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(cl));
        assertThat(service.getPrefix(), is("mock1"));
        assertThat(service.getServiceClass(), is((Object) MockService.class));
        assertThat(service.getConfiguration().size(), is(0));
        assertThat(service.getContext().getClassLoader(), is(cl));
    }

    /**
     * Load service profile with configuration.
     */
    @Test
    public void load_with_config() {
        Properties prop = new Properties();
        prop.setProperty("mock1", MockService.class.getName());
        prop.setProperty("mock1.hoge", "foo");
        prop.setProperty("mock1.bar.1", "moga");
        prop.setProperty("mock2.hoge", "hoge");
        prop.setProperty("mock3.bar", "bar");
        ClassLoader cl = getClass().getClassLoader();
        ServiceProfile<Service> service = ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(cl));
        assertThat(service.getPrefix(), is("mock1"));
        assertThat(service.getConfiguration().size(), is(2));
        assertThat(service.getConfiguration().get("hoge"), is("foo"));
        assertThat(service.getConfiguration().get("bar.1"), is("moga"));
    }

    /**
     * Attempts to load a service profile without class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void load_invalid_empty() {
        Properties prop = new Properties();
        ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Attempts to load a service profile with unknown class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void load_invalid_class() {
        Properties prop = new Properties();
        prop.setProperty("mock1", "__UNKNOWN__");
        ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Attempts to load a service profile with invalid service class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void load_invalid_service() {
        Properties prop = new Properties();
        prop.setProperty("mock1", String.class.getName());
        ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Attempts to load a service profile with inconsistent service class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void load_invalid_base() {
        Properties prop = new Properties();
        prop.setProperty("mock1", MockService.class.getName());
        ServiceProfile.load(prop, "mock1", CoreProfile.class, ProfileContext.system(getClass().getClassLoader()));
    }

    /**
     * Creates a new instance.
     * @throws Exception if failed
     */
    @Test
    public void newInstance() throws Exception {
        Properties prop = new Properties();
        prop.setProperty("mock1", MockService.class.getName());
        prop.setProperty("mock1.hoge", "foo");
        prop.setProperty("mock1.bar.1", "moga");
        ClassLoader cl = getClass().getClassLoader();
        ServiceProfile<Service> service = ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(cl));
        Service instance = service.newInstance();
        assertThat(instance, instanceOf(MockService.class));

        MockService mock = (MockService) instance;
        assertThat(mock.serviceProfile.getPrefix(), is("mock1"));
        assertThat(mock.serviceProfile.getConfiguration().size(), is(2));
        assertThat(mock.serviceProfile.getConfiguration().get("hoge"), is("foo"));
        assertThat(mock.serviceProfile.getConfiguration().get("bar.1"), is("moga"));
    }

    /**
     * Attempts to create a new instance but its class has no public constructors.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void newInstance_fail_new() throws Exception {
        Properties prop = new Properties();
        prop.setProperty("mock1", PrivateService.class.getName());
        ClassLoader cl = getClass().getClassLoader();
        ServiceProfile<Service> service = ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(cl));
        service.newInstance();
    }

    /**
     * Attempts to create a new instance but its configuration must fail.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void newInstance_fail_configure() throws Exception {
        Properties prop = new Properties();
        prop.setProperty("mock1", InvalidService.class.getName());
        ClassLoader cl = getClass().getClassLoader();
        ServiceProfile<Service> service = ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(cl));
        service.newInstance();
    }

    /**
     * Simple store.
     */
    @Test
    public void storeTo() {
        Properties prop = new Properties();
        prop.setProperty("mock1", MockService.class.getName());
        ClassLoader cl = getClass().getClassLoader();
        ServiceProfile<Service> service = ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(cl));

        Properties target = new Properties();
        service.storeTo(target);
        assertThat(target, is(prop));
    }

    /**
     * Simple store.
     */
    @Test
    public void storeTo_with_configuration() {
        Properties prop = new Properties();
        prop.setProperty("mock1", MockService.class.getName());
        prop.setProperty("mock1.hoge", "foo");
        prop.setProperty("mock1.bar.1", "moga");
        ClassLoader cl = getClass().getClassLoader();
        ServiceProfile<Service> service = ServiceProfile.load(prop, "mock1", Service.class, ProfileContext.system(cl));

        Properties target = new Properties();
        service.storeTo(target);
        assertThat(target, is(prop));
    }
}
