package org.apache.hadoop.hdfs.server.federation.router;

import java.io.IOException;


public class SubClusterTimeoutException extends IOException {

    private static final long serialVersionUID = 1L;

    public SubClusterTimeoutException(String msg) {
        super(msg);
    }
}