/**
 * Copyright 2012 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.Collection;

import com.asakusafw.yaess.jobqueue.client.JobClient;

/**
 * Provides a {@link JobClient}.
 * @since 0.2.6
 */
public class JobClientProvider {

    private final JobClient[] clients;

    private final boolean[] blackList;

    private int location;

    /**
     * Creates a new instance.
     * @param clients the components
     * @throws IllegalArgumentException if {@code clients} was null or empty
     */
    public JobClientProvider(Collection<? extends JobClient> clients) {
        if (clients == null) {
            throw new IllegalArgumentException("clients must not be null"); //$NON-NLS-1$
        }
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("clients must not be empty"); //$NON-NLS-1$
        }
        this.clients = clients.toArray(new JobClient[clients.size()]);
        this.blackList = new boolean[clients.size()];
        this.location = 0;
    }

    /**
     * Returns the number of available clients.
     * @return the number of available clients
     */
    public int count() {
        return clients.length;
    }

    /**
     * Returns a client.
     * @return a client.
     */
    public JobClient get() {
        JobClient[] cs = clients;
        boolean[] bs = blackList;
        assert cs.length >= 1;
        while (true) {
            synchronized (cs) {
                for (int i = 0; i < cs.length; i++) {
                    JobClient client = cs[location];
                    boolean enabled = blackList[location] == false;
                    location = (location + 1) % cs.length;
                    if (enabled) {
                        return client;
                    }
                }
            }
            // clear black list
            synchronized (cs) {
                Arrays.fill(bs, false);
            }
        }
    }

    /**
     * Tells the error occurred while using the client.
     * @param client the client which was failed
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void setError(JobClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null"); //$NON-NLS-1$
        }
        int index = -1;
        JobClient[] cs = clients;
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == client) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            boolean[] bs = blackList;
            synchronized (cs) {
                bs[index] = true;
            }
        }
    }
}
