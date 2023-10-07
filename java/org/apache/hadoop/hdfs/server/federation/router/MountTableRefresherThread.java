package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.hdfs.server.federation.resolver.MountTableManager;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RefreshMountTableEntriesRequest;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RefreshMountTableEntriesResponse;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

// 更新 Router的挂载表缓存
// 并不是一个定期性服务, 刷新完缓存就销毁线程
public class MountTableRefresherThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(MountTableRefresherThread.class);
    private boolean success;
    
    // 目标待更新的 Router
    private final String adminAddress;
    private CountDownLatch countDownLatch;
    private final MountTableManager manager;

    public MountTableRefresherThread(MountTableManager manager, String adminAddress) {
        this.manager = manager;
        this.adminAddress = adminAddress;
        setName("MountTableRefresh_" + adminAddress);
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            SecurityUtil.doAsLoginUser(() -> {
                if (UserGroupInformation.isSecurityEnabled()) {
                    UserGroupInformation.getLoginUser().checkTGTAndReloginFromKeytab();
                }
                RefreshMountTableEntriesResponse refreshMountTableEntries = manager.refreshMountTableEntries(RefreshMountTableEntriesRequest.newInstance());
                success = refreshMountTableEntries.getResult();
                return true;
            });
        } catch (IOException e) {
            LOG.error("Failed to refresh mount table entries cache at router {}", adminAddress, e);
        } finally {
            countDownLatch.countDown();
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public String toString() {
        return "MountTableRefreshThread [success=" + success + ", adminAddress="
                + adminAddress + "]";
    }

    public String getAdminAddress() {
        return adminAddress;
    }
}
