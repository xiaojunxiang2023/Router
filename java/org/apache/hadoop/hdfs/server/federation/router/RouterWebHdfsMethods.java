package org.apache.hadoop.hdfs.server.federation.router;

import com.sun.jersey.spi.container.ResourceFilters;
import org.apache.hadoop.hdfs.protocol.*;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.DatanodeReportType;
import org.apache.hadoop.hdfs.server.common.JspHelper;
import org.apache.hadoop.hdfs.server.federation.router.security.RouterSecurityManager;
import org.apache.hadoop.hdfs.server.namenode.web.resources.NamenodeWebHdfsMethods;
import org.apache.hadoop.hdfs.web.JsonUtil;
import org.apache.hadoop.hdfs.web.ParamFilter;
import org.apache.hadoop.hdfs.web.WebHdfsFileSystem;
import org.apache.hadoop.hdfs.web.resources.*;
import org.apache.hadoop.ipc.ExternalCall;
import org.apache.hadoop.ipc.RetriableException;
import org.apache.hadoop.net.Node;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.TokenIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.apache.hadoop.util.StringUtils.getTrimmedStringCollection;

// 拓展自 NamenodeWebHdfsMethods
@Path("")
@ResourceFilters(ParamFilter.class)
public class RouterWebHdfsMethods extends NamenodeWebHdfsMethods {
    private static final Logger LOG =
            LoggerFactory.getLogger(RouterWebHdfsMethods.class);

    private @Context
    HttpServletRequest request;
    private String remoteAddr;

    public RouterWebHdfsMethods(@Context HttpServletRequest request) {
        super(request);
        this.remoteAddr = JspHelper.getRemoteAddr(request);
    }

    @Override
    protected void init(final UserGroupInformation ugi,
                        final DelegationParam delegation,
                        final UserParam username, final DoAsParam doAsUser,
                        final UriFsPathParam path, final HttpOpParam<?> op,
                        final Param<?, ?>... parameters) {
        super.init(ugi, delegation, username, doAsUser, path, op, parameters);

        remoteAddr = JspHelper.getRemoteAddr(request);
    }

    @Override
    protected ClientProtocol getRpcClientProtocol() throws IOException {
        final Router router = getRouter();
        final RouterRpcServer routerRpcServer = router.getRpcServer();
        if (routerRpcServer == null) {
            throw new RetriableException("Router is in startup mode");
        }
        return routerRpcServer;
    }

    private void reset() {
        remoteAddr = null;
    }

    @Override
    protected String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    protected void queueExternalCall(ExternalCall call)
            throws IOException, InterruptedException {
        getRouter().getRpcServer().getServer().queueCall(call);
    }

    private Router getRouter() {
        return (Router) getContext().getAttribute("name.node");
    }

    private static RouterRpcServer getRPCServer(final Router router)
            throws IOException {
        final RouterRpcServer routerRpcServer = router.getRpcServer();
        if (routerRpcServer == null) {
            throw new RetriableException("Router is in startup mode");
        }
        return routerRpcServer;
    }

    // RestAPI 接口
    @Override
    protected Response put(
            final UserGroupInformation ugi,
            final DelegationParam delegation,
            final UserParam username,
            final DoAsParam doAsUser,
            final String fullpath,
            final PutOpParam op,
            final DestinationParam destination,
            final OwnerParam owner,
            final GroupParam group,
            final PermissionParam permission,
            final UnmaskedPermissionParam unmaskedPermission,
            final OverwriteParam overwrite,
            final BufferSizeParam bufferSize,
            final ReplicationParam replication,
            final BlockSizeParam blockSize,
            final ModificationTimeParam modificationTime,
            final AccessTimeParam accessTime,
            final RenameOptionSetParam renameOptions,
            final CreateParentParam createParent,
            final TokenArgumentParam delegationTokenArgument,
            final AclPermissionParam aclPermission,
            final XAttrNameParam xattrName,
            final XAttrValueParam xattrValue,
            final XAttrSetFlagParam xattrSetFlag,
            final SnapshotNameParam snapshotName,
            final OldSnapshotNameParam oldSnapshotName,
            final ExcludeDatanodesParam exclDatanodes,
            final CreateFlagParam createFlagParam,
            final NoRedirectParam noredirectParam,
            final StoragePolicyParam policyName,
            final ECPolicyParam ecpolicy,
            final NameSpaceQuotaParam namespaceQuota,
            final StorageSpaceQuotaParam storagespaceQuota,
            final StorageTypeParam storageType
    ) throws IOException, URISyntaxException {

        switch (op.getValue()) {
            case CREATE: {
                final Router router = getRouter();
                final URI uri = redirectURI(router, ugi, delegation, username,
                        doAsUser, fullpath, op.getValue(), -1L,
                        exclDatanodes.getValue(), permission, unmaskedPermission,
                        overwrite, bufferSize, replication, blockSize, createParent,
                        createFlagParam);
                if (!noredirectParam.getValue()) {
                    return Response.temporaryRedirect(uri)
                            .type(MediaType.APPLICATION_OCTET_STREAM).build();
                } else {
                    final String js = JsonUtil.toJsonString("Location", uri);
                    return Response.ok(js).type(MediaType.APPLICATION_JSON).build();
                }
            }
            case MKDIRS:
            case CREATESYMLINK:
            case RENAME:
            case SETREPLICATION:
            case SETOWNER:
            case SETPERMISSION:
            case SETTIMES:
            case RENEWDELEGATIONTOKEN:
            case CANCELDELEGATIONTOKEN:
            case MODIFYACLENTRIES:
            case REMOVEACLENTRIES:
            case REMOVEDEFAULTACL:
            case REMOVEACL:
            case SETACL:
            case SETXATTR:
            case REMOVEXATTR:
            case ALLOWSNAPSHOT:
            case CREATESNAPSHOT:
            case RENAMESNAPSHOT:
            case DISALLOWSNAPSHOT:
            case SETSTORAGEPOLICY:
            case ENABLEECPOLICY:
            case DISABLEECPOLICY:
            case SATISFYSTORAGEPOLICY: {
                return super.put(ugi, delegation, username, doAsUser, fullpath, op,
                        destination, owner, group, permission, unmaskedPermission,
                        overwrite, bufferSize, replication, blockSize, modificationTime,
                        accessTime, renameOptions, createParent, delegationTokenArgument,
                        aclPermission, xattrName, xattrValue, xattrSetFlag, snapshotName,
                        oldSnapshotName, exclDatanodes, createFlagParam, noredirectParam,
                        policyName, ecpolicy, namespaceQuota, storagespaceQuota, storageType);
            }
            default:
                throw new UnsupportedOperationException(op + " is not supported");
        }
    }

    @Override
    protected Response post(
            final UserGroupInformation ugi,
            final DelegationParam delegation,
            final UserParam username,
            final DoAsParam doAsUser,
            final String fullpath,
            final PostOpParam op,
            final ConcatSourcesParam concatSrcs,
            final BufferSizeParam bufferSize,
            final ExcludeDatanodesParam excludeDatanodes,
            final NewLengthParam newLength,
            final NoRedirectParam noRedirectParam
    ) throws IOException, URISyntaxException {
        switch (op.getValue()) {
            case APPEND: {
                final Router router = getRouter();
                final URI uri = redirectURI(router, ugi, delegation, username,
                        doAsUser, fullpath, op.getValue(), -1L,
                        excludeDatanodes.getValue(), bufferSize);
                if (!noRedirectParam.getValue()) {
                    return Response.temporaryRedirect(uri)
                            .type(MediaType.APPLICATION_OCTET_STREAM).build();
                } else {
                    final String js = JsonUtil.toJsonString("Location", uri);
                    return Response.ok(js).type(MediaType.APPLICATION_JSON).build();
                }
            }
            case CONCAT:
            case TRUNCATE:
            case UNSETSTORAGEPOLICY: {
                return super.post(ugi, delegation, username, doAsUser, fullpath, op,
                        concatSrcs, bufferSize, excludeDatanodes, newLength,
                        noRedirectParam);
            }
            default:
                throw new UnsupportedOperationException(op + " is not supported");
        }
    }

    @Override
    protected Response get(
            final UserGroupInformation ugi,
            final DelegationParam delegation,
            final UserParam username,
            final DoAsParam doAsUser,
            final String fullpath,
            final GetOpParam op,
            final OffsetParam offset,
            final LengthParam length,
            final RenewerParam renewer,
            final BufferSizeParam bufferSize,
            final List<XAttrNameParam> xattrNames,
            final XAttrEncodingParam xattrEncoding,
            final ExcludeDatanodesParam excludeDatanodes,
            final FsActionParam fsAction,
            final SnapshotNameParam snapshotName,
            final OldSnapshotNameParam oldSnapshotName,
            final TokenKindParam tokenKind,
            final TokenServiceParam tokenService,
            final NoRedirectParam noredirectParam,
            final StartAfterParam startAfter
    ) throws IOException, URISyntaxException {
        try {
            final Router router = getRouter();

            switch (op.getValue()) {
                case OPEN: {
                    final URI uri = redirectURI(router, ugi, delegation, username,
                            doAsUser, fullpath, op.getValue(), offset.getValue(),
                            excludeDatanodes.getValue(), offset, length, bufferSize);
                    if (!noredirectParam.getValue()) {
                        return Response.temporaryRedirect(uri)
                                .type(MediaType.APPLICATION_OCTET_STREAM).build();
                    } else {
                        final String js = JsonUtil.toJsonString("Location", uri);
                        return Response.ok(js).type(MediaType.APPLICATION_JSON).build();
                    }
                }
                case GETFILECHECKSUM: {
                    final URI uri = redirectURI(router, ugi, delegation, username,
                            doAsUser, fullpath, op.getValue(), -1L, null);
                    if (!noredirectParam.getValue()) {
                        return Response.temporaryRedirect(uri)
                                .type(MediaType.APPLICATION_OCTET_STREAM).build();
                    } else {
                        final String js = JsonUtil.toJsonString("Location", uri);
                        return Response.ok(js).type(MediaType.APPLICATION_JSON).build();
                    }
                }
                case GETDELEGATIONTOKEN:
                case GET_BLOCK_LOCATIONS:
                case GETFILESTATUS:
                case LISTSTATUS:
                case GETCONTENTSUMMARY:
                case GETHOMEDIRECTORY:
                case GETACLSTATUS:
                case GETXATTRS:
                case LISTXATTRS:
                case CHECKACCESS: {
                    return super.get(ugi, delegation, username, doAsUser, fullpath, op,
                            offset, length, renewer, bufferSize, xattrNames, xattrEncoding,
                            excludeDatanodes, fsAction, snapshotName, oldSnapshotName,
                            tokenKind, tokenService, noredirectParam, startAfter);
                }
                default:
                    throw new UnsupportedOperationException(op + " is not supported");
            }
        } finally {
            reset();
        }
    }

    // Case CREATE创建文件，重定向到 DataNode
    private URI redirectURI(final Router router, final UserGroupInformation ugi,
                            final DelegationParam delegation, final UserParam username,
                            final DoAsParam doAsUser, final String path, final HttpOpParam.Op op,
                            final long openOffset, final String excludeDatanodes,
                            final Param<?, ?>... parameters) throws URISyntaxException, IOException {
        final DatanodeInfo dn = chooseDatanode(router, path, op, openOffset, excludeDatanodes);

        if (dn == null) {
            throw new IOException("Failed to find datanode, suggest to check cluster"
                    + " health. excludeDatanodes=" + excludeDatanodes);
        }

        final String delegationQuery;
        // 拼接 delegationQuery
        if (!UserGroupInformation.isSecurityEnabled()) {
            delegationQuery = Param.toSortedString("&", doAsUser, username);
        } else if (delegation.getValue() != null) {
            delegationQuery = "&" + delegation;
        } else {
            final Token<? extends TokenIdentifier> t = generateDelegationToken(
                    ugi, ugi.getUserName());
            delegationQuery = "&delegation=" + t.encodeToUrlString();
        }

        final String redirectQuery = op.toQueryString() + delegationQuery
                + "&namenoderpcaddress=" + router.getRouterId()
                + Param.toSortedString("&", parameters);
        final String uripath = WebHdfsFileSystem.PATH_PREFIX + path;

        int port = "http".equals(getScheme()) ? dn.getInfoPort() :
                dn.getInfoSecurePort();
        final URI uri = new URI(getScheme(), null, dn.getHostName(), port, uripath,
                redirectQuery, null);

        if (LOG.isTraceEnabled()) {
            LOG.trace("redirectURI={}", uri);
        }
        return uri;
    }

    private DatanodeInfo chooseDatanode(final Router router,
                                        final String path, final HttpOpParam.Op op, final long openOffset,
                                        final String excludeDatanodes) throws IOException {
        final RouterRpcServer rpcServer = getRPCServer(router);
        UserGroupInformation loginUser = UserGroupInformation.getLoginUser();
        RouterRpcServer.setCurrentUser(loginUser);

        DatanodeInfo[] dns = null;
        try {
            dns = rpcServer.getDatanodeReport(DatanodeReportType.LIVE);
        } catch (IOException e) {
            LOG.error("Cannot get the datanodes from the RPC server", e);
        } finally {
            RouterRpcServer.resetCurrentUser();
        }

        HashSet<Node> excludes = new HashSet<Node>();
        if (excludeDatanodes != null) {
            Collection<String> collection =
                    getTrimmedStringCollection(excludeDatanodes);
            for (DatanodeInfo dn : dns) {
                if (collection.contains(dn.getName())) {
                    excludes.add(dn);
                }
            }
        }

        if (op == GetOpParam.Op.OPEN ||
                op == PostOpParam.Op.APPEND ||
                op == GetOpParam.Op.GETFILECHECKSUM) {
            final ClientProtocol cp = getRpcClientProtocol();
            final HdfsFileStatus status = cp.getFileInfo(path);
            if (status == null) {
                throw new FileNotFoundException("File " + path + " not found.");
            }
            final long len = status.getLen();
            if (op == GetOpParam.Op.OPEN) {
                if (openOffset < 0L || (openOffset >= len && len > 0)) {
                    throw new IOException("Offset=" + openOffset
                            + " out of the range [0, " + len + "); " + op + ", path=" + path);
                }
            }

            if (len > 0) {
                final long offset = op == GetOpParam.Op.OPEN ? openOffset : len - 1;
                final LocatedBlocks locations = cp.getBlockLocations(path, offset, 1);
                final int count = locations.locatedBlockCount();
                if (count > 0) {
                    LocatedBlock location0 = locations.get(0);
                    return bestNode(location0.getLocations(), excludes);
                }
            }
        }

        return getRandomDatanode(dns, excludes);
    }

    private static DatanodeInfo getRandomDatanode(
            final DatanodeInfo[] dns, final HashSet<Node> excludes) {
        DatanodeInfo dn = null;

        if (dns == null) {
            return dn;
        }

        int numDNs = dns.length;
        int availableNodes = 0;
        if (excludes.isEmpty()) {
            availableNodes = numDNs;
        } else {
            for (DatanodeInfo di : dns) {
                if (!excludes.contains(di)) {
                    availableNodes++;
                }
            }
        }

        if (availableNodes > 0) {
            while (dn == null || excludes.contains(dn)) {
                Random rnd = new Random();
                int idx = rnd.nextInt(numDNs);
                dn = dns[idx];
            }
        }
        return dn;
    }

    @Override
    public Credentials createCredentials(
            final UserGroupInformation ugi,
            final String renewer) throws IOException {
        final Router router = (Router) getContext().getAttribute("name.node");
        return RouterSecurityManager.createCredentials(router, ugi, renewer != null ? renewer : ugi.getShortUserName());
    }
}
