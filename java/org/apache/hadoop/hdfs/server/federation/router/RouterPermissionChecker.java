package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;
import org.apache.hadoop.hdfs.server.namenode.FSPermissionChecker;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// 对 MountTable节点进行鉴权
public class RouterPermissionChecker extends FSPermissionChecker {
    static final Logger LOG =
            LoggerFactory.getLogger(RouterPermissionChecker.class);

    // 默认权限
    public static final short MOUNT_TABLE_PERMISSION_DEFAULT = 00755;

    private final String superUser;
    private final String superGroup;

    public RouterPermissionChecker(String user, String group,
                                   UserGroupInformation callerUgi) {
        super(user, group, callerUgi, null);
        this.superUser = user;
        this.superGroup = group;
    }

    public RouterPermissionChecker(String user, String group)
            throws IOException {
        super(user, group, UserGroupInformation.getCurrentUser(), null);
        this.superUser = user;
        this.superGroup = group;
    }

    // 对此节点进行鉴权
    public void checkPermission(MountTable mountTable, FsAction access)
            throws AccessControlException {
        if (isSuperUser()) {
            return;
        }

        FsPermission mode = mountTable.getMode();
        if (getUser().equals(mountTable.getOwnerName())
                && mode.getUserAction().implies(access)) {
            return;
        }

        if (isMemberOfGroup(mountTable.getGroupName())
                && mode.getGroupAction().implies(access)) {
            return;
        }

        if (!getUser().equals(mountTable.getOwnerName())
                && !isMemberOfGroup(mountTable.getGroupName())
                && mode.getOtherAction().implies(access)) {
            return;
        }

        throw new AccessControlException(
                "Permission denied while accessing mount table "
                        + mountTable.getSourcePath()
                        + ": user " + getUser() + " does not have " + access.toString()
                        + " permissions.");
    }

    // 检查当前用户是否为超级用户 或 超级用户组
    @Override
    public void checkSuperuserPrivilege() throws AccessControlException {

        UserGroupInformation ugi = null;
        try {
            ugi = NameNode.getRemoteUser();
        } catch (IOException ignored) {

        }
        if (ugi == null) {
            LOG.error("Cannot get the remote user name");
            throw new AccessControlException("Cannot get the remote user name");
        }

        if (ugi.getShortUserName().equals(superUser)) {
            return;
        }

        List<String> groups = Arrays.asList(ugi.getGroupNames());
        if (groups.contains(superGroup)) {
            return;
        }

        // 不是超级用户
        throw new AccessControlException(ugi.getUserName() + " is not a super user");
    }
}
