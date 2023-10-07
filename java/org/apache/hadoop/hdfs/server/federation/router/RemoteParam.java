package org.apache.hadoop.hdfs.server.federation.router;

import java.util.Map;

public class RemoteParam {

    // RemoteLocationContext -> 
    // ?何意
    private final Map<?, ?> paramMap;

    public RemoteParam() {
        this.paramMap = null;
    }

    public RemoteParam(Map<? extends RemoteLocationContext, ?> map) {
        this.paramMap = map;
    }

    public Object getParameterForContext(RemoteLocationContext context) {
        if (context == null) {
            return null;
        } else if (this.paramMap != null) {
            return this.paramMap.get(context);
        } else {
            // 降级到从 context自身取
            return context.getDest();
        }
    }

    @Override
    public String toString() {
        return "RemoteParam(" + this.paramMap + ")";
    }
}
