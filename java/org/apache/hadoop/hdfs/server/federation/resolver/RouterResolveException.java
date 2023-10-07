package org.apache.hadoop.hdfs.server.federation.resolver;

import java.io.IOException;

public class RouterResolveException extends IOException {
    private static final long serialVersionUID = 1L;

    public RouterResolveException(String msg) {
        super(msg);
    }
}
