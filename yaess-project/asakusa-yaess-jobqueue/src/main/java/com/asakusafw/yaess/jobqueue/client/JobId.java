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
package com.asakusafw.yaess.jobqueue.client;

import java.io.Serializable;
import java.text.MessageFormat;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a registered job ID.
 * @since 0.2.6
 */
public final class JobId implements Serializable {

    private static final long serialVersionUID = -3642312498785847619L;

    @SerializedName("jrid")
    private String token;

    /**
     * Creates a new instance for deserializer.
     */
    public JobId() {
        return;
    }

    /**
     * Creates a new instance.
     * @param token the token
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JobId(String token) {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null"); //$NON-NLS-1$
        }
        this.token = token;
    }

    /**
     * Sets the token for this ID.
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns the token which represents this ID.
     * @return the token
     */
    public String getToken() {
        return token;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JobId other = (JobId) obj;
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "JobId({0})",
                token);
    }
}
