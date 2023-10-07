package org.apache.hadoop.hdfs.server.federation.router;

import java.io.IOException;

public class ConnectionNullException extends IOException {

    private static final long serialVersionUID = 1L;

    public ConnectionNullException(String msg) {
        super(msg);
    }
}
