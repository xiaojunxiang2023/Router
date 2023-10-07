package org.apache.hadoop.hdfs.server.federation.store;

import java.io.IOException;

// 当 StateStore 连接不上 或不可用的时候
public class StateStoreUnavailableException extends IOException {

    private static final long serialVersionUID = 1L;

    public StateStoreUnavailableException(String msg) {
        super(msg);
    }
}
