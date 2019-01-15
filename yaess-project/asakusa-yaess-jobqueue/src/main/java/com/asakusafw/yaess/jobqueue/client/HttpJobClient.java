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
package com.asakusafw.yaess.jobqueue.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionPhase;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

/**
 * An implementation of {@link JobClient} via HTTP(S) connections.
 * @since 0.2.6
 */
public class HttpJobClient implements JobClient {

    static final Logger LOG = LoggerFactory.getLogger(HttpJobClient.class);

    private static final Charset ENCODING = StandardCharsets.UTF_8;

    static final ContentType CONTENT_TYPE = ContentType.create("text/json", ENCODING);

    private static final GsonBuilder GSON_BUILDER;
    static {
        GSON_BUILDER = new GsonBuilder();
        GSON_BUILDER.registerTypeAdapter(JobStatus.Kind.class, new JobStatusKindAdapter());
        GSON_BUILDER.registerTypeAdapter(ExecutionPhase.class, new ExecutionPhaseAdapter());
        GSON_BUILDER.setFieldNamingStrategy(f -> Optional.ofNullable(f.getAnnotation(SerializedName.class))
                .map(SerializedName::value)
                .orElse(f.getName()));
    }

    private final String baseUri;

    private final String user;

    private final HttpClient http;

    /**
     * Creates a new instance.
     * @param baseUri the target base URL
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HttpJobClient(String baseUri) {
        if (baseUri == null) {
            throw new IllegalArgumentException("baseUri must not be null"); //$NON-NLS-1$
        }
        this.baseUri = normalize(baseUri);
        this.user = null;
        this.http = createClient();
    }

    /**
     * Creates a new instance.
     * @param baseUri the target base URL
     * @param user user name
     * @param password password
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HttpJobClient(String baseUri, String user, String password) {
        if (baseUri == null) {
            throw new IllegalArgumentException("baseUri must not be null"); //$NON-NLS-1$
        }
        if (user == null) {
            throw new IllegalArgumentException("user must not be null"); //$NON-NLS-1$
        }
        if (password == null) {
            throw new IllegalArgumentException("password must not be null"); //$NON-NLS-1$
        }
        this.baseUri = normalize(baseUri);
        this.user = user;
        DefaultHttpClient client = createClient();
        client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
        this.http = client;
    }

    private DefaultHttpClient createClient() {
        try {
            DefaultHttpClient client = new DefaultHttpClient(new PoolingClientConnectionManager());
            SSLSocketFactory socketFactory = TrustedSSLSocketFactory.create();
            Scheme sch = new Scheme("https", 443, socketFactory);
            client.getConnectionManager().getSchemeRegistry().register(sch);
            return client;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to initialize SSL socket factory: {0}",
                    baseUri), e);
        }
    }

    private static String normalize(String url) {
        assert url != null;
        if (url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }

    /**
     * Returns the base URI.
     * This URI must end with a slash.
     * @return the base URI
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Returns the target user name for auth.
     * @return the user name for auth, or {@code null} if there are no credentials
     */
    public String getUser() {
        return user;
    }

    @Override
    public JobId register(JobScript script) throws IOException, InterruptedException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        HttpPost request = new HttpPost();
        URI uri = createUri("jobs");
        request.setURI(uri);
        request.setEntity(createEntity(script));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering a job: method=post, uri={}, script={}", uri, script);
        }
        HttpResponse response = http.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JobStatus status = extractJobStatus(request, response);
            if (status.getKind() == JobStatus.Kind.ERROR) {
                throw toException(request, response, status, MessageFormat.format(
                        "Failed to register a job ({1}): {0}",
                        script,
                        status.getErrorMessage()));
            }
            return new JobId(status.getJobId());
        } else {
            throw toException(request, response, MessageFormat.format(
                    "Failed to register a job: {0}",
                    script));
        }
    }

    @Override
    public JobStatus getStatus(JobId id) throws IOException, InterruptedException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        HttpGet request = new HttpGet();
        URI uri = createUri(String.format("jobs/%s", id.getToken()));
        request.setURI(uri);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Obtaining information about job: method=get, uri={}", uri);
        }
        HttpResponse response = http.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JobStatus status = extractJobStatus(request, response);
            return status;
        } else {
            throw toException(request, response, MessageFormat.format(
                    "Failed to obtain the job status: {0} ({1})",
                    id.getToken(),
                    request.getURI()));
        }
    }

    @Override
    public void submit(JobId id) throws IOException, InterruptedException {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        HttpPut request = new HttpPut();
        URI uri = createUri(String.format("jobs/%s/execute", id.getToken()));
        request.setURI(uri);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Submitting job: method=put, uri={}", uri);
        }
        HttpResponse response = http.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JobStatus status = extractJobStatus(request, response);
            if (status.getKind() == JobStatus.Kind.ERROR) {
                throw toException(request, response, status, MessageFormat.format(
                        "Failed to submit job: {0} ({1})",
                        id.getToken(),
                        request.getURI()));
            }
        } else {
            throw toException(request, response, MessageFormat.format(
                    "Failed to submit job: {0} ({1})",
                    id.getToken(),
                    request.getURI()));
        }
    }

    private URI createUri(String path) {
        return URI.create(baseUri + path);
    }

    private JobStatus extractJobStatus(HttpUriRequest request, HttpResponse response) throws IOException {
        assert request != null;
        assert response != null;
        JobStatus status = extractContent(JobStatus.class, request, response);
        if (status.getKind() == null) {
            throw new IOException(MessageFormat.format(
                    "status was not specified: {0}",
                    request.getURI()));
        }
        if (status.getKind() != JobStatus.Kind.ERROR && status.getJobId() == null) {
            throw new IOException(MessageFormat.format(
                    "job request ID was not specified: {0}",
                    request.getURI()));
        }
        if (status.getKind() == JobStatus.Kind.COMPLETED && status.getExitCode() == null) {
            throw new IOException(MessageFormat.format(
                    "exit code was not specified: {0}",
                    request.getURI()));
        }
        return status;
    }

    private <T> T extractContent(Class<T> type, HttpUriRequest request, HttpResponse response) throws IOException {
        assert request != null;
        assert response != null;
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new IOException(MessageFormat.format(
                    "Response message was invalid (empty): {0} ({1})",
                    request.getURI(),
                    response.getStatusLine()));
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(entity.getContent(), ENCODING));) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(reader);
            if ((element instanceof JsonObject) == false) {
                throw new IOException(MessageFormat.format(
                        "Response message was not a valid json object: {0} ({1})",
                        request.getURI(),
                        response.getStatusLine()));
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("response: {}", new Object[] {
                        element
                });
            }
            return GSON_BUILDER.create().fromJson(element, type);
        } catch (RuntimeException e) {
            throw new IOException(MessageFormat.format(
                    "Response message was invalid (not JSON): {0} ({1})",
                    request.getURI(),
                    response.getStatusLine()), e);
        }
    }

    private IOException toException(HttpUriRequest request, HttpResponse response, String message) {
        assert request != null;
        assert response != null;
        assert message != null;
        try {
            JobStatus status = extractJobStatus(request, response);
            return toException(request, response, status, message);
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Failed to analyze an error response (uri={0}, status={1})",
                        request.getURI(),
                        response.getStatusLine()), e);
            }
        }
        return new IOException(MessageFormat.format(
                "{0} (uri={1}, status={2})",
                message,
                request.getURI(),
                response));
    }

    private IOException toException(HttpUriRequest request, HttpResponse response, JobStatus status, String message) {
        assert request != null;
        assert response != null;
        assert status != null;
        assert message != null;
        return new IOException(MessageFormat.format(
                "{0} (uri={1}, status={2}, servercode={3}, servermessage={4})",
                message,
                request.getURI(),
                response.getStatusLine(),
                status.getErrorCode(),
                status.getErrorMessage()));
    }

    private HttpEntity createEntity(JobScript script) {
        assert script != null;
        String json = GSON_BUILDER.create().toJson(script);
        LOG.trace("request: {}", json);
        return new StringEntity(json, CONTENT_TYPE);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "HttpJobClient({0})",
                baseUri);
    }

    private static final class JobStatusKindAdapter implements JsonDeserializer<JobStatus.Kind> {

        JobStatusKindAdapter() {
            return;
        }

        @Override
        public JobStatus.Kind deserialize(
                JsonElement json,
                Type type,
                JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive) json;
                if (primitive.isString()) {
                    JobStatus.Kind kind = JobStatus.Kind.findFromSymbol(primitive.getAsString());
                    if (kind != null) {
                        return kind;
                    }
                }
            }
            throw new JsonParseException(MessageFormat.format(
                    "Invalid JobStatus.Kind: {0}",
                    json));
        }
    }

    private static final class ExecutionPhaseAdapter implements JsonSerializer<ExecutionPhase> {

        ExecutionPhaseAdapter() {
            return;
        }

        @Override
        public JsonElement serialize(ExecutionPhase src, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(src.getSymbol());
        }
    }

    private static final class TrustedSSLSocketFactory extends SSLSocketFactory {

        private static final String SSL_CONTEXT = "SSL";

        private final SSLContext context;

        private TrustedSSLSocketFactory(SSLContext context) {
            super(context);
            this.context = context;
        }

        static TrustedSSLSocketFactory create() throws NoSuchAlgorithmException, KeyManagementException {
            SSLContext context = SSLContext.getInstance(SSL_CONTEXT);
            context.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return;
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            } }, null);
            return new TrustedSSLSocketFactory(context);
        }

        @Override
        public Socket createSocket() throws IOException {
            return context.getSocketFactory().createSocket();
        }
    }

}
