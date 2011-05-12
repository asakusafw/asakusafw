package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.asakusafw.compiler.flow.Location;

public class FlowPartTestDriverUtils {
    
    private FlowPartTestDriverUtils() {};
    
    public static Location createInputLocation(TestDriverContext driverContext, String tableName) {
        Location location = Location.fromPath(driverContext.getClusterWorkDir(), '/')
                .append(driverContext.getExecutionId()).append("input")
                .append(normalize(tableName));
        return location;
    }

    public static Location createOutputLocation(TestDriverContext driverContext, String tableName) {
        Location location = Location.fromPath(driverContext.getClusterWorkDir(), '/')
                .append(driverContext.getExecutionId()).append("output")
                .append(normalize(tableName)).asPrefix();
        return location;
    }

    public static Location createTempLocation(TestDriverContext driverContext) {
        Location location = Location.fromPath(driverContext.getClusterWorkDir(), '/')
                .append(driverContext.getExecutionId()).append("temp");
        return location;
    }
    
    public static URI toUri(String path, String fragment) throws URISyntaxException {
        URI resource = new File(path).toURI();
        URI uri = new URI(
                resource.getScheme(),
                resource.getUserInfo(),
                resource.getHost(),
                resource.getPort(),
                resource.getPath(),
                resource.getQuery(),
                fragment);
        return uri;
    }    

    private static String normalize(String targetName) {
        // MultipleInputs/Outputsではアルファベットと数字だけしかつかえない
        StringBuilder buf = new StringBuilder();
        for (char c : targetName.toCharArray()) {
            // 0 はエスケープ記号に
            if ('1' <= c && c <= '9' || 'A' <= c && c <= 'Z' || 'a' <= c
                    && c <= 'z') {
                buf.append(c);
            } else if (c <= 0xff) {
                buf.append('0');
                buf.append(String.format("%02x", (int) c));
            } else {
                buf.append("0u");
                buf.append(String.format("%04x", (int) c));
            }
        }
        return buf.toString();
    }
    
}
