package org.apache.hadoop.hdfs.server.federation.router.security.token;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.security.token.delegation.AbstractDelegationTokenIdentifier;
import org.apache.hadoop.security.token.delegation.ZKDelegationTokenSecretManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// DelegationToken的管理器, 使用 ZooKeeper作为存储介质
public class ZKDelegationTokenSecretManagerImpl extends ZKDelegationTokenSecretManager<AbstractDelegationTokenIdentifier> {

    private static final Logger LOG = LoggerFactory.getLogger(ZKDelegationTokenSecretManagerImpl.class);

    public ZKDelegationTokenSecretManagerImpl(Configuration conf) {
        super(conf);
        try {
            super.startThreads();
        } catch (IOException e) {
            LOG.error("Error starting threads for zkDelegationTokens", e);
        }
        LOG.info("Zookeeper delegation token secret manager instantiated");
    }

    @Override
    public DelegationTokenIdentifier createIdentifier() {
        return new DelegationTokenIdentifier();
    }
}
