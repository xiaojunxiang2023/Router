package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.hdfs.server.federation.store.protocol.*;

import java.io.IOException;

public interface NameserviceManager {

    DisableNameserviceResponse disableNameservice(
            DisableNameserviceRequest request) throws IOException;

    EnableNameserviceResponse enableNameservice(EnableNameserviceRequest request)
            throws IOException;

    GetDisabledNameservicesResponse getDisabledNameservices(
            GetDisabledNameservicesRequest request) throws IOException;
}
