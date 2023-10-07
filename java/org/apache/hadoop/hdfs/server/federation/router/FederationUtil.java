package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSUtil;
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.hdfs.server.federation.resolver.ActiveNamenodeResolver;
import org.apache.hadoop.hdfs.server.federation.resolver.FileSubclusterResolver;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreService;
import org.apache.hadoop.hdfs.web.URLConnectionFactory;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.delegation.AbstractDelegationTokenSecretManager;
import org.apache.hadoop.util.VersionInfo;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumSet;

public final class FederationUtil {

    private static final Logger LOG =
            LoggerFactory.getLogger(FederationUtil.class);

    private FederationUtil() {
    }

    // 获得 jmx
    public static JSONArray getJmx(String beanQuery, String webAddress,
                                   URLConnectionFactory connectionFactory, String scheme) {
        JSONArray ret = null;
        BufferedReader reader = null;
        try {
            String host = webAddress;
            int port = -1;
            if (webAddress.indexOf(":") > 0) {
                String[] webAddressSplit = webAddress.split(":");
                host = webAddressSplit[0];
                port = Integer.parseInt(webAddressSplit[1]);
            }
            URL jmxURL = new URL(scheme, host, port, "/jmx?qry=" + beanQuery);
            LOG.debug("JMX URL: {}", jmxURL);
            // Create a URL connection
            URLConnection conn = connectionFactory.openConnection(
                    jmxURL, UserGroupInformation.isSecurityEnabled());
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(5 * 1000);
            InputStream in = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(in, "UTF-8");
            reader = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jmxOutput = sb.toString();

            // Parse JSON
            JSONObject json = new JSONObject(jmxOutput);
            ret = json.getJSONArray("beans");
        } catch (IOException e) {
            LOG.error("Cannot read JMX bean {} from server {}",
                    beanQuery, webAddress, e);
        } catch (JSONException e) {
            // We shouldn't need more details if the JSON parsing fails.
            LOG.error("Cannot parse JMX output for {} from server {}: {}",
                    beanQuery, webAddress, e.getMessage());
        } catch (Exception e) {
            LOG.error("Cannot parse JMX output for {} from server {}",
                    beanQuery, webAddress, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Problem closing {}", webAddress, e);
                }
            }
        }
        return ret;
    }

    // 获得 version
    public static String getVersion() {
        return VersionInfo.getVersion();
    }

    // 获得编译信息
    public static String getCompileInfo() {
        return VersionInfo.getDate() + " by " + VersionInfo.getUser() + " from "
                + VersionInfo.getBranch();
    }

    // 获得 挂载表解析器
    public static FileSubclusterResolver newFileSubclusterResolver(
            Configuration conf, Router router) {
        Class<? extends FileSubclusterResolver> clazz = conf.getClass(
                RBFConfigKeys.FEDERATION_FILE_RESOLVER_CLIENT_CLASS,
                RBFConfigKeys.FEDERATION_FILE_RESOLVER_CLIENT_CLASS_DEFAULT,
                FileSubclusterResolver.class);
        return newInstance(conf, router, Router.class, clazz);
    }

    public static ActiveNamenodeResolver newActiveNamenodeResolver(
            Configuration conf, StateStoreService stateStore) {
        Class<? extends ActiveNamenodeResolver> clazz = conf.getClass(
                RBFConfigKeys.FEDERATION_NAMENODE_RESOLVER_CLIENT_CLASS,
                RBFConfigKeys.FEDERATION_NAMENODE_RESOLVER_CLIENT_CLASS_DEFAULT,
                ActiveNamenodeResolver.class);
        return newInstance(conf, stateStore, StateStoreService.class, clazz);
    }

    // 创建一个 DelegationToken管理器
    public static AbstractDelegationTokenSecretManager<DelegationTokenIdentifier>
    newSecretManager(Configuration conf) {
        Class<? extends AbstractDelegationTokenSecretManager> clazz =
                conf.getClass(
                        RBFConfigKeys.DFS_ROUTER_DELEGATION_TOKEN_DRIVER_CLASS,
                        RBFConfigKeys.DFS_ROUTER_DELEGATION_TOKEN_DRIVER_CLASS_DEFAULT,
                        AbstractDelegationTokenSecretManager.class);
        return newInstance(conf, null, null, clazz);
    }

    private static <T, R> T newInstance(final Configuration conf,
                                        final R context, final Class<R> contextClass, final Class<T> clazz) {
        try {
            if (contextClass == null) {
                if (conf == null) {
                    Constructor<T> constructor = clazz.getConstructor();
                    return constructor.newInstance();
                } else {
                    Constructor<T> constructor = clazz.getConstructor(Configuration.class);
                    return constructor.newInstance(conf);
                }
            } else {
                Constructor<T> constructor = clazz.getConstructor(Configuration.class, contextClass);
                return constructor.newInstance(conf, context);
            }
        } catch (ReflectiveOperationException e) {
            LOG.error("Could not instantiate: {}", clazz.getSimpleName(), e);
            return null;
        }
    }

    // 给 hdfsFileStatus设置 children字段
    public static HdfsFileStatus updateMountPointStatus(HdfsFileStatus dirStatus, int children) {
        EnumSet<HdfsFileStatus.Flags> flags = DFSUtil.getFlags(dirStatus.isEncrypted(), dirStatus.isErasureCoded(),
                dirStatus.isSnapshotEnabled(), dirStatus.hasAcl());
        EnumSet.noneOf(HdfsFileStatus.Flags.class);
        return new HdfsFileStatus.Builder().atime(dirStatus.getAccessTime())
                .blocksize(dirStatus.getBlockSize()).children(children)
                .ecPolicy(dirStatus.getErasureCodingPolicy())
                .feInfo(dirStatus.getFileEncryptionInfo()).fileId(dirStatus.getFileId())
                .group(dirStatus.getGroup()).isdir(dirStatus.isDir())
                .length(dirStatus.getLen()).mtime(dirStatus.getModificationTime())
                .owner(dirStatus.getOwner()).path(dirStatus.getLocalNameInBytes())
                .perm(dirStatus.getPermission()).replication(dirStatus.getReplication())
                .storagePolicy(dirStatus.getStoragePolicy())
                .symlink(dirStatus.getSymlinkInBytes()).flags(flags).build();
    }
}
