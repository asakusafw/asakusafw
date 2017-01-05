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
package com.asakusafw.yaess.jobqueue.client;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.localserver.RequestBasicAuth;
import org.apache.http.localserver.ResponseBasicUnauthorized;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.ExecutionPhase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test for {@link HttpJobClient}.
 */
public class HttpJobClientTest {

    static final Logger LOG = LoggerFactory.getLogger(HttpJobClientTest.class);

    private LocalTestServer server;

    private String baseUrl;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        BasicHttpProcessor proc = new BasicHttpProcessor();
        proc.addInterceptor(new ResponseDate());
        proc.addInterceptor(new ResponseServer());
        proc.addInterceptor(new ResponseContent());
        proc.addInterceptor(new ResponseConnControl());
        proc.addInterceptor(new RequestBasicAuth());
        proc.addInterceptor(new ResponseBasicUnauthorized());
        server = new LocalTestServer(proc, null);
        server.start();
        InetSocketAddress address = server.getServiceAddress();
        baseUrl = new URL("http", address.getHostName(), address.getPort(), "/").toExternalForm();
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    /**
     * register.
     * @throws Exception if failed
     */
    @Test
    public void register() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "initialized");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);
        JobScript script = new JobScript();
        script.setBatchId("b");
        script.setFlowId("f");
        script.setExecutionId("e");
        script.setPhase(ExecutionPhase.MAIN);
        script.setStageId("s");
        script.setMainClassName("Cls");
        script.setProperties(new HashMap<String, String>());
        script.setEnvironmentVariables(new HashMap<String, String>());

        JobId id = client.register(script);
        assertThat(id, is(new JobId("testing")));

        assertThat(handler.requestElement, is(notNullValue()));
        JsonObject object = handler.requestElement;
        assertThat(object.get("batchId").getAsString(), is("b"));
        assertThat(object.get("flowId").getAsString(), is("f"));
        assertThat(object.get("executionId").getAsString(), is("e"));
        assertThat(object.get("phaseId").getAsString(), is("main"));
        assertThat(object.get("stageId").getAsString(), is("s"));
        assertThat(object.get("properties").isJsonObject(), is(true));
        assertThat(object.get("env").isJsonObject(), is(true));
    }

    /**
     * register error.
     * @throws Exception if failed
     */
    @Test
    public void register_error() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "error");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);
        JobScript script = new JobScript();
        script.setBatchId("b");
        script.setFlowId("f");
        script.setPhase(ExecutionPhase.MAIN);
        script.setStageId("s");
        script.setExecutionId("e");
        script.setMainClassName("Cls");
        script.setProperties(new HashMap<String, String>());
        script.setEnvironmentVariables(new HashMap<String, String>());

        try {
            client.register(script);
            fail();
        } catch (IOException e) {
            LOG.debug("OK.", e);
        }
        assertThat(handler.requestElement, is(notNullValue()));
    }

    /**
     * register.
     * @throws Exception if failed
     */
    @Test
    public void register_missing() throws Exception {
        ErrorHandler handler = new ErrorHandler(404, null);
        server.register("/jobs", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);
        JobScript script = new JobScript();
        script.setBatchId("b");
        script.setFlowId("f");
        script.setPhase(ExecutionPhase.MAIN);
        script.setStageId("s");
        script.setExecutionId("e");
        script.setMainClassName("Cls");
        script.setProperties(new HashMap<String, String>());
        script.setEnvironmentVariables(new HashMap<String, String>());

        try {
            client.register(script);
            fail();
        } catch (IOException e) {
            LOG.debug("OK.", e);
        }
        assertThat(handler.requestElement, is(notNullValue()));
    }

    /**
     * register.
     * @throws Exception if failed
     */
    @Test
    public void register_no_connections() throws Exception {
        server.stop();

        HttpJobClient client = new HttpJobClient(baseUrl);
        JobScript script = new JobScript();
        script.setBatchId("b");
        script.setFlowId("f");
        script.setPhase(ExecutionPhase.MAIN);
        script.setStageId("s");
        script.setExecutionId("e");
        script.setMainClassName("Cls");
        script.setProperties(new HashMap<String, String>());
        script.setEnvironmentVariables(new HashMap<String, String>());

        try {
            client.register(script);
            fail();
        } catch (IOException e) {
            LOG.debug("OK.", e);
        }
    }

    /**
     * register.
     * @throws Exception if failed
     */
    @Test
    public void register_auth() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "initialized");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs", new AuthHandler(handler));

        HttpJobClient client = new HttpJobClient(baseUrl, "a", "b");
        JobScript script = new JobScript();
        script.setBatchId("b");
        script.setFlowId("f");
        script.setExecutionId("e");
        script.setPhase(ExecutionPhase.MAIN);
        script.setStageId("s");
        script.setMainClassName("Cls");
        script.setProperties(new HashMap<String, String>());
        script.setEnvironmentVariables(new HashMap<String, String>());

        client.register(script);
    }

    /**
     * register.
     * @throws Exception if failed
     */
    @Test
    public void register_unauth() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "initialized");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs", new AuthHandler(handler));

        HttpJobClient client = new HttpJobClient(baseUrl);
        JobScript script = new JobScript();
        script.setBatchId("b");
        script.setFlowId("f");
        script.setExecutionId("e");
        script.setPhase(ExecutionPhase.MAIN);
        script.setStageId("s");
        script.setMainClassName("Cls");
        script.setProperties(new HashMap<String, String>());
        script.setEnvironmentVariables(new HashMap<String, String>());

        try {
            client.register(script);
            fail();
        } catch (IOException e) {
            // ok.
            LOG.debug("OK.", e);
        }
    }

    /**
     * status (initialized).
     * @throws Exception if failed
     */
    @Test
    public void status_initialized() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "initialized");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);

        JobStatus status = client.getStatus(new JobId("testing"));
        assertThat(status.getKind(), is(JobStatus.Kind.INITIALIZED));

        assertThat(handler.requestElement, is(nullValue()));
    }

    /**
     * status (waiting).
     * @throws Exception if failed
     */
    @Test
    public void status_waiting() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "waiting");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);

        JobStatus status = client.getStatus(new JobId("testing"));
        assertThat(status.getKind(), is(JobStatus.Kind.WAITING));

        assertThat(handler.requestElement, is(nullValue()));
    }

    /**
     * status (running).
     * @throws Exception if failed
     */
    @Test
    public void status_running() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "running");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);

        JobStatus status = client.getStatus(new JobId("testing"));
        assertThat(status.getKind(), is(JobStatus.Kind.RUNNING));

        assertThat(handler.requestElement, is(nullValue()));
    }

    /**
     * status (completed).
     * @throws Exception if failed
     */
    @Test
    public void status_completed() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "completed");
        result.addProperty("jrid", "testing");
        result.addProperty("exitCode", "1");

        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);

        JobStatus status = client.getStatus(new JobId("testing"));
        assertThat(status.getKind(), is(JobStatus.Kind.COMPLETED));
        assertThat(status.getExitCode(), is(Integer.valueOf(1)));

        assertThat(handler.requestElement, is(nullValue()));
    }

    /**
     * status (error).
     * @throws Exception if failed
     */
    @Test
    public void status_error() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "error");
        result.addProperty("jrid", "testing");
        result.addProperty("message", "ERROR");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);

        JobStatus status = client.getStatus(new JobId("testing"));
        assertThat(status.getKind(), is(JobStatus.Kind.ERROR));

        assertThat(handler.requestElement, is(nullValue()));
    }

    /**
     * status (exception occurred).
     * @throws Exception if failed
     */
    @Test
    public void status_missing() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "initialized");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);

        try {
            client.getStatus(new JobId("other"));
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * submit.
     * @throws Exception if failed
     */
    @Test
    public void submit() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "waiting");
        result.addProperty("jrid", "testing");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing/execute", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);
        client.submit(new JobId("testing"));

        assertThat(handler.requestElement, is(nullValue()));
    }

    /**
     * submit.
     * @throws Exception if failed
     */
    @Test
    public void submit_error() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "error");
        result.addProperty("message", "ERROR");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing/execute", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);
        try {
            client.submit(new JobId("testing"));
            fail();
        } catch (IOException e) {
            // ok.
        }
        assertThat(handler.requestElement, is(nullValue()));
    }

    /**
     * submit.
     * @throws Exception if failed
     */
    @Test
    public void submit_missing() throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("status", "waiting");
        result.addProperty("jrid", "other");
        JsonHandler handler = new JsonHandler(result);
        server.register("/jobs/testing/execute", handler);

        HttpJobClient client = new HttpJobClient(baseUrl);
        try {
            client.submit(new JobId("other"));
            fail();
        } catch (IOException e) {
            // ok.
        }
        assertThat(handler.requestElement, is(nullValue()));
    }

    static JsonElement parse(String content) {
        return new JsonParser().parse(content);
    }

    private static class JsonHandler implements HttpRequestHandler {

        final JsonElement responseElement;

        volatile JsonObject requestElement;

        public JsonHandler(JsonElement element) {
            this.responseElement = element;
        }

        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            response.setStatusCode(200);
            response.setEntity(new StringEntity(
                    new Gson().toJson(responseElement).toString(), HttpJobClient.CONTENT_TYPE));
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String content = EntityUtils.toString(entity, "UTF-8");
                JsonElement element = parse(content);
                if (element instanceof JsonObject) {
                    requestElement = (JsonObject) element;
                }
            }
        }
    }

    private static class AuthHandler implements HttpRequestHandler {

        private final HttpRequestHandler delegate;

        public AuthHandler(HttpRequestHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            String credentials = (String) context.getAttribute("creds");
            if (credentials == null || credentials.equals("a:b") == false) {
                response.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
            } else {
                delegate.handle(request, response, context);
            }
        }
    }

    private static class ErrorHandler implements HttpRequestHandler {

        private final int status;

        private final String code;

        volatile JsonObject requestElement;

        public ErrorHandler(int status, String code) {
            this.status = status;
            this.code = code;
        }

        @Override
        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
            response.setStatusCode(status);
            if (code != null) {
                JsonObject object = new JsonObject();
                object.addProperty("error", code);
                object.addProperty("message", code);
                response.setEntity(new StringEntity(new Gson().toJson(object).toString(), HttpJobClient.CONTENT_TYPE));
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String content = EntityUtils.toString(entity, "UTF-8");
                JsonElement element = parse(content);
                if (element instanceof JsonObject) {
                    requestElement = (JsonObject) element;
                }
            }
        }
    }
}
