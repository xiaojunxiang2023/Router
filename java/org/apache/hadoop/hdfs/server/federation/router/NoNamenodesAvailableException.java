package org.apache.hadoop.hdfs.server.federation.router;

import java.io.IOException;


public class NoNamenodesAvailableException extends IOException {

    private static final long serialVersionUID = 1L;

    public NoNamenodesAvailableException(String nsId, IOException ioe) {
        super("No namenodes available under nameservice " + nsId, ioe);
    }
}
