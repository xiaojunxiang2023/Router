package org.apache.hadoop.hdfs.protocolPB;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.hdfs.HDFSPolicyProvider;
import org.apache.hadoop.security.authorize.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 获得所有的 Service
@InterfaceAudience.Private
public class RouterPolicyProvider extends HDFSPolicyProvider {

    // PROTOCOL_ACL, 对协议的客户端 进行鉴权
    private static final Service[] RBF_SERVICES = new Service[]{
            new Service(CommonConfigurationKeys.SECURITY_ROUTER_ADMIN_PROTOCOL_ACL,
                    RouterAdminProtocol.class)};

    private final Service[] services;

    public RouterPolicyProvider() {
        List<Service> list = new ArrayList<>();
        // 添加 Service
        list.addAll(Arrays.asList(super.getServices()));
        list.addAll(Arrays.asList(RBF_SERVICES));
        services = list.toArray(new Service[0]);
    }

    @Override
    public Service[] getServices() {
        return Arrays.copyOf(services, services.length);
    }
}
