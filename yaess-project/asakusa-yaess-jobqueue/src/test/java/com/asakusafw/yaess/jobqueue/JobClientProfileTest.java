/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.yaess.jobqueue;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;
import com.asakusafw.yaess.jobqueue.client.HttpJobClient;
import com.asakusafw.yaess.jobqueue.client.JobClient;

/**
 * Test for {@link JobClientProfile}.
 */
public class JobClientProfileTest {

    /**
     * simple conversion.
     * @throws Exception if failed
     */
    @Test
    public void convert() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        "1.url", "http://www.example.com/jobqueue"
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile profile = JobClientProfile.convert(original);
        assertThat(profile.getPrefix(), is("testing"));
        assertThat(profile.getTimeout(), is(JobClientProfile.DEFAULT_TIMEOUT));
        assertThat(profile.getPollingInterval(), is(JobClientProfile.DEFAULT_POLLING_INTERVAL));
        List<JobClient> clients = profile.getClients();
        assertThat(clients.size(), is(1));
        assertThat(clients.get(0), instanceOf(HttpJobClient.class));

        HttpJobClient c0 = (HttpJobClient) clients.get(0);
        assertThat(c0.getBaseUri(), is("http://www.example.com/jobqueue/"));
        assertThat(c0.getUser(), is(nullValue()));
    }

    /**
     * simple conversion.
     * @throws Exception if failed
     */
    @Test
    public void convert_explicit() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        JobClientProfile.KEY_TIMEOUT,
                        String.valueOf(JobClientProfile.DEFAULT_TIMEOUT + 1),
                        JobClientProfile.KEY_POLLING_INTERVAL,
                        String.valueOf(JobClientProfile.DEFAULT_POLLING_INTERVAL + 2),
                        "1.url", "http://www.example.com/jobqueue/1",
                        "2.url", "http://www.example.com/jobqueue/2",
                        "2.user", "u2",
                        "2.password", "p2",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile profile = JobClientProfile.convert(original);
        assertThat(profile.getPrefix(), is("testing"));
        assertThat(profile.getTimeout(), is(JobClientProfile.DEFAULT_TIMEOUT + 1));
        assertThat(profile.getPollingInterval(), is(JobClientProfile.DEFAULT_POLLING_INTERVAL + 2));
        List<JobClient> clients = profile.getClients();
        assertThat(clients.size(), is(2));
        assertThat(clients.get(0), instanceOf(HttpJobClient.class));
        assertThat(clients.get(1), instanceOf(HttpJobClient.class));

        HttpJobClient c0 = (HttpJobClient) clients.get(0);
        assertThat(c0.getBaseUri(), is("http://www.example.com/jobqueue/1/"));
        assertThat(c0.getUser(), is(nullValue()));

        HttpJobClient c1 = (HttpJobClient) clients.get(1);
        assertThat(c1.getBaseUri(), is("http://www.example.com/jobqueue/2/"));
        assertThat(c1.getUser(), is("u2"));
    }

    /**
     * converts with resolving variabes.
     * @throws Exception if failed
     */
    @Test
    public void convert_resolve() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        JobClientProfile.KEY_TIMEOUT, "${timeout}",
                        JobClientProfile.KEY_POLLING_INTERVAL, "${pollingInterval}",
                        "1.url", "${url}",
                        "1.user", "${user}",
                        "1.password", "${password}",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                        "timeout", "1234",
                        "pollingInterval", "567",
                        "url", "http://www.example.com/jq",
                        "user", "u",
                        "password", "p",
                }))));
        JobClientProfile profile = JobClientProfile.convert(original);
        assertThat(profile.getTimeout(), is(1234L));
        assertThat(profile.getPollingInterval(), is(567L));
        List<JobClient> clients = profile.getClients();
        assertThat(clients.size(), is(1));
        assertThat(clients.get(0), instanceOf(HttpJobClient.class));

        HttpJobClient c0 = (HttpJobClient) clients.get(0);
        assertThat(c0.getBaseUri(), is("http://www.example.com/jq/"));
        assertThat(c0.getUser(), is("u"));
    }

    /**
     * invalid timeout.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_malform_timeout() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        JobClientProfile.KEY_TIMEOUT, "?",
                        "1.url", "http://www.example.com/jobqueue/",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    /**
     * invalid timeout.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_invalid_timeout() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        JobClientProfile.KEY_TIMEOUT, "-1",
                        "1.url", "http://www.example.com/jobqueue/",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    /**
     * invalid interval.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_malform_interval() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        JobClientProfile.KEY_POLLING_INTERVAL, "?",
                        "1.url", "http://www.example.com/jobqueue/",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    /**
     * invalid interval.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_invalid_interval() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        JobClientProfile.KEY_POLLING_INTERVAL, "-1",
                        "1.url", "http://www.example.com/jobqueue/",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    /**
     * missing client.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_missing_client() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    /**
     * missing client url.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_missing_client_url() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        "1.user", "u",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    /**
     * invalid client key prefix.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_invalid_client_prefix() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        "?.url", "http://www.example.com/jobqueue/",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    /**
     * invalid variables.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_unresolved() throws Exception {
        ServiceProfile<?> original = new ServiceProfile<HadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map(new String[] {
                        "1.url", "${__UNDEF__}",
                }),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map(new String[] {
                }))));
        JobClientProfile.convert(original);
    }

    private Map<String, String> map(String[] keyValuePairs) {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> results = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            results.put(keyValuePairs[i + 0], keyValuePairs[i + 1]);
        }
        return results;
    }
}
