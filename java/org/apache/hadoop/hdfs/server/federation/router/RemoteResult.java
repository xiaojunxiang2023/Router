package org.apache.hadoop.hdfs.server.federation.router;

import java.io.IOException;

public class RemoteResult<T extends RemoteLocationContext, R> {
    // RemoteLocation 
    private final T loc;
    private final R result;
    private final boolean resultSet;
    private final IOException ioe;

    public RemoteResult(T location, R r) {
        this.loc = location;
        this.result = r;
        this.resultSet = true;
        this.ioe = null;
    }

    public RemoteResult(T location, IOException e) {
        this.loc = location;
        this.result = null;
        this.resultSet = false;
        this.ioe = e;
    }

    public T getLocation() {
        return loc;
    }

    public boolean hasResult() {
        return resultSet;
    }

    public R getResult() {
        return result;
    }

    public boolean hasException() {
        return getException() != null;
    }

    public IOException getException() {
        return ioe;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("loc=").append(getLocation());
        if (hasResult()) {
            sb.append(" result=").append(getResult());
        }
        if (hasException()) {
            sb.append(" exception=").append(getException());
        }
        return sb.toString();
    }
}
