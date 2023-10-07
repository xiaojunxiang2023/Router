package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.hdfs.server.federation.store.protocol.*;

import java.io.IOException;

public interface RouterStateManager {
    EnterSafeModeResponse enterSafeMode(EnterSafeModeRequest request)
            throws IOException;

    LeaveSafeModeResponse leaveSafeMode(LeaveSafeModeRequest request)
            throws IOException;

    GetSafeModeResponse getSafeMode(GetSafeModeRequest request)
            throws IOException;
}
