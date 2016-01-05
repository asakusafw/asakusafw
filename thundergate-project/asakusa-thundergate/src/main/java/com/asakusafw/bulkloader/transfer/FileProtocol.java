/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.bulkloader.transfer;

import java.text.MessageFormat;
import java.util.Properties;

import com.asakusafw.thundergate.runtime.cache.CacheInfo;

/**
 * File protocol abstraction.
 * @since 0.2.3
 */
public class FileProtocol {

    /**
     * The key name of {@link #getKind() protocol kind}.
     * This holds constant name of {@link FileProtocol.Kind}.
     */
    public static final String KEY_KIND = "kind";

    /**
     * The key name of {@link #getLocation() the content location}.
     */
    public static final String KEY_LOCATION = "location";

    private final Kind kind;

    private final String location;

    private final CacheInfo info;

    /**
     * Creates a new instance.
     * @param kind the protocol kind
     * @param location target contents location
     * @param info cache info, or {@code null} if the protocol does not require it
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileProtocol(Kind kind, String location, CacheInfo info) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (location == null) {
            throw new IllegalArgumentException("location must not be null"); //$NON-NLS-1$
        }
        if (kind.hasCacheInfo() && info == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Cache info is mandatory for the protocol {0}",
                    kind));
        }
        this.kind = kind;
        this.location = location;
        this.info = info;
    }

    /**
     * Returns the kind of this protocol.
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Returns the location of target contents.
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the cache info.
     * @return the cache info, or {@code null} if this protocol has no information about cache
     */
    public CacheInfo getInfo() {
        return info;
    }

    /**
     * Loads {@link FileProtocol} from {@link Properties} created by using {@link #storeTo(Properties)}.
     * @param properties the source properties
     * @return the loaded object
     * @throws IllegalArgumentException if source properties object is invalid
     */
    public static FileProtocol loadFrom(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        String kindString = loadProperty(properties, KEY_KIND);
        String location = loadProperty(properties, KEY_LOCATION);
        Kind kind;
        try {
            kind = Kind.valueOf(kindString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid protocol kind in \"{0}\": {1}",
                    KEY_KIND,
                    kindString), e);
        }
        CacheInfo info;
        if (kind.hasCacheInfo()) {
            info = CacheInfo.loadFrom(properties);
        } else {
            info = null;
        }
        return new FileProtocol(kind, location, info);
    }

    private static String loadProperty(Properties properties, String key) {
        assert properties != null;
        assert key != null;
        String property = properties.getProperty(key);
        if (property == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid cache protocol: {0} is missing",
                    key));
        }
        return property.trim();
    }

    /**
     * Stores this object into the target properties object.
     * @param properties target properties object
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #loadFrom(Properties)
     */
    public void storeTo(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        properties.setProperty(KEY_KIND, kind.name());
        properties.setProperty(KEY_LOCATION, location);
        if (kind.hasCacheInfo()) {
            assert info != null;
            info.storeTo(properties);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CacheProtocol [kind=");
        builder.append(kind);
        builder.append(", location=");
        builder.append(location);
        builder.append(", info=");
        builder.append(info);
        builder.append("]");
        return builder.toString();
    }

    /**
     * The protocol kind.
     * @since 0.2.3
     */
    public enum Kind {

        /**
         * Requests cache info on {@link FileProtocol#getLocation()}.
         * File content will be ignored in this protocol kind.
         */
        GET_CACHE_INFO(false),

        /**
         * Requests to delete cache data on {@link FileProtocol#getLocation()}.
         * File content will be ignored in this protocol kind.
         */
        DELETE_CACHE(false),

        /**
         * Returns the requested cache info on {@link FileProtocol#getLocation()}.
         * This message is a response for {@link #GET_CACHE_INFO}.
         * File content will be ignored in this protocol kind.
         */
        RESPONSE_CACHE_INFO(true),

        /**
         * Returns the requested to delete cache data on {@link FileProtocol#getLocation()}.
         * This message is a response for {@link #DELETE_CACHE}.
         * File content will be ignored in this protocol kind.
         */
        RESPONSE_DELETED(false),

        /**
         * Attempts to fetch/delete cache but it is not found on {@link FileProtocol#getLocation()}.
         * This message is a response for {@link #GET_CACHE_INFO} and {@link #DELETE_CACHE}.
         * File content will be ignored in this protocol kind.
         */
        RESPONSE_NOT_FOUND(false),

        /**
         * Attempts to fetch/delete cache but it is failed {@link FileProtocol#getLocation()}.
         * This message is a response for {@link #GET_CACHE_INFO} and {@link #DELETE_CACHE}.
         * File content will be ignored in this protocol kind.
         */
        RESPONSE_ERROR(false),

        /**
         * Extracts body as normal content.
         * This case {@link FileProtocol#getLocation()} holds target file path.
         */
        CONTENT(false),

        /**
         * Extracts body as patch data and create a new cache using them.
         * This case {@link FileProtocol#getLocation()} holds the target cache directory.
         */
        CREATE_CACHE(true),

        /**
         * Extracts body as patch data and apply them with the current cache.
         * This case {@link FileProtocol#getLocation()} holds the target cache directory.
         */
        UPDATE_CACHE(true),

        ;
        private final boolean hasCacheInfo;

        private Kind(boolean hasCacheInfo) {
            this.hasCacheInfo = hasCacheInfo;
        }

        /**
         * Returns whether this protocol kind has additional {@link CacheInfo}.
         * @return {@code true} if this protocol has {@link CacheInfo}, otherwise {@code false}
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public boolean hasCacheInfo() {
            return hasCacheInfo;
        }
    }
}
