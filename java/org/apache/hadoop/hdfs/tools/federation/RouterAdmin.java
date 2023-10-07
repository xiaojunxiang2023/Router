package org.apache.hadoop.hdfs.tools.federation;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.StorageType;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.server.federation.resolver.MountTableManager;
import org.apache.hadoop.hdfs.server.federation.resolver.RemoteLocation;
import org.apache.hadoop.hdfs.server.federation.resolver.RouterGenericManager;
import org.apache.hadoop.hdfs.server.federation.resolver.order.DestinationOrder;
import org.apache.hadoop.hdfs.server.federation.router.*;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;
import org.apache.hadoop.ipc.ProtobufRpcEngine2;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RefreshResponse;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.ipc.protocolPB.GenericRefreshProtocolClientSideTranslatorPB;
import org.apache.hadoop.ipc.protocolPB.GenericRefreshProtocolPB;
import org.apache.hadoop.ipc.protocolPB.RefreshCallQueueProtocolClientSideTranslatorPB;
import org.apache.hadoop.ipc.protocolPB.RefreshCallQueueProtocolPB;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static org.apache.hadoop.hdfs.server.federation.router.Quota.*;

/*
    RouterAdmin 的 Admin客户端命令
    
    使用的是 RPC客户端是 RouterClient, 仅仅是一些 getXxxManager$createRouterProxy
        真正是靠 getXxxManager$createRouterProxy去调用 xxxStore(xxxStore也是 xxxManager的之类)
 */
@Private
public class RouterAdmin extends Configured implements Tool {

    private static final Logger LOG = LoggerFactory.getLogger(RouterAdmin.class);
    private static final Pattern SLASHES = Pattern.compile("/+");

    private RouterClient client;

    public static void main(String[] argv) throws Exception {
        Configuration conf = new HdfsConfiguration();
        RouterAdmin admin = new RouterAdmin(conf);
        int res = ToolRunner.run(admin, argv);
        System.exit(res);
    }

    public RouterAdmin(Configuration conf) {
        super(conf);
    }

    public void printUsage() {
        String usage = getUsage(null);
        System.out.println(usage);
    }

    private void printUsage(String cmd) {
        String usage = getUsage(cmd);
        System.out.println(usage);
    }

    private String getUsage(String cmd) {
        if (cmd == null) {
            String[] commands =
                    {"-add", "-update", "-rm", "-ls", "-getDestination", "-setQuota",
                            "-setStorageTypeQuota", "-clrQuota", "-clrStorageTypeQuota",
                            "-safemode", "-nameservice", "-getDisabledNameservices",
                            "-refresh", "-refreshRouterArgs",
                            "-refreshSuperUserGroupsConfiguration", "-refreshCallQueue"};
            StringBuilder usage = new StringBuilder();
            usage.append("Usage: hdfs dfsrouteradmin :\n");
            for (int i = 0; i < commands.length; i++) {
                usage.append(getUsage(commands[i]));
                if (i + 1 < commands.length) {
                    usage.append("\n");
                }
            }
            return usage.toString();
        }
        switch (cmd) {
            case "-add":
                return "\t[-add <source> <nameservice1, nameservice2, ...> <destination> "
                        + "[-readonly] [-faulttolerant] "
                        + "[-order HASH|LOCAL|RANDOM|HASH_ALL|SPACE] "
                        + "-owner <owner> -group <group> -mode <mode>]";
            case "-update":
                return "\t[-update <source>"
                        + " [<nameservice1, nameservice2, ...> <destination>] "
                        + "[-readonly true|false] [-faulttolerant true|false] "
                        + "[-order HASH|LOCAL|RANDOM|HASH_ALL|SPACE] "
                        + "-owner <owner> -group <group> -mode <mode>]";
            case "-rm":
                return "\t[-rm <source>]";
            case "-ls":
                return "\t[-ls [-d] <path>]";
            case "-getDestination":
                return "\t[-getDestination <path>]";
            case "-setQuota":
                return "\t[-setQuota <path> -nsQuota <nsQuota> -ssQuota "
                        + "<quota in bytes or quota size string>]";
            case "-setStorageTypeQuota":
                return "\t[-setStorageTypeQuota <path> -storageType <storage type> "
                        + "<quota in bytes or quota size string>]";
            case "-clrQuota":
                return "\t[-clrQuota <path>]";
            case "-clrStorageTypeQuota":
                return "\t[-clrStorageTypeQuota <path>]";
            case "-safemode":
                return "\t[-safemode enter | leave | get]";
            case "-nameservice":
                return "\t[-nameservice enable | disable <nameservice>]";
            case "-getDisabledNameservices":
                return "\t[-getDisabledNameservices]";
            case "-refresh":
                return "\t[-refresh]";
            case "-refreshRouterArgs":
                return "\t[-refreshRouterArgs <host:ipc_port> <key> [arg1..argn]]";
            case "-refreshSuperUserGroupsConfiguration":
                return "\t[-refreshSuperUserGroupsConfiguration]";
            case "-refreshCallQueue":
                return "\t[-refreshCallQueue]";
        }
        return getUsage(null);
    }

    private void validateMax(String[] arg) {
        switch (arg[0]) {
            case "-ls":
                if (arg.length > 3) {
                    throw new IllegalArgumentException(
                            "Too many arguments, Max=2 argument allowed");
                }
                break;
            case "-getDestination":
                if (arg.length > 2) {
                    throw new IllegalArgumentException(
                            "Too many arguments, Max=1 argument allowed only");
                }
                break;
            case "-safemode":
                if (arg.length > 2) {
                    throw new IllegalArgumentException(
                            "Too many arguments, Max=1 argument allowed only");
                }
                break;
            case "-nameservice":
                if (arg.length > 3) {
                    throw new IllegalArgumentException(
                            "Too many arguments, Max=2 arguments allowed");
                }
                break;
            case "-getDisabledNameservices":
                if (arg.length > 1) {
                    throw new IllegalArgumentException("No arguments allowed");
                }
                break;
            case "-refreshSuperUserGroupsConfiguration":
                if (arg.length > 1) {
                    throw new IllegalArgumentException("No arguments allowed");
                }
                break;
            case "-refreshCallQueue":
                if (arg.length > 1) {
                    throw new IllegalArgumentException("No arguments allowed");
                }
                break;
        }
    }

    private boolean validateMin(String[] argv) {
        String cmd = argv[0];
        if ("-add".equals(cmd)) {
            return argv.length >= 4;
        } else if ("-update".equals(cmd)) {
            return argv.length >= 4;
        } else if ("-rm".equals(cmd)) {
            return argv.length >= 2;
        } else if ("-getDestination".equals(cmd)) {
            return argv.length >= 2;
        } else if ("-setQuota".equals(cmd)) {
            return argv.length >= 4;
        } else if ("-setStorageTypeQuota".equals(cmd)) {
            return argv.length >= 5;
        } else if ("-clrQuota".equals(cmd)) {
            return argv.length >= 2;
        } else if ("-clrStorageTypeQuota".equals(cmd)) {
            return argv.length >= 2;
        } else if ("-safemode".equals(cmd)) {
            return argv.length >= 2;
        } else if ("-nameservice".equals(cmd)) {
            return argv.length >= 3;
        } else if ("-refreshRouterArgs".equals(cmd)) {
            return argv.length >= 2;
        }
        return true;
    }

    @Override
    public int run(String[] argv) throws Exception {
        if (argv.length < 1) {
            System.err.println("Not enough parameters specified");
            printUsage();
            return -1;
        }

        int exitCode = -1;
        int i = 0;
        String cmd = argv[i++];

        if (!validateMin(argv)) {
            System.err.println("Not enough parameters specificed for cmd " + cmd);
            printUsage(cmd);
            return exitCode;
        }
        String address = null;
        try {
            address = getConf().getTrimmed(
                    RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_KEY,
                    RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_DEFAULT);
            InetSocketAddress routerSocket = NetUtils.createSocketAddr(address);
            client = new RouterClient(routerSocket, getConf());
        } catch (RPC.VersionMismatch v) {
            System.err.println(
                    "Version mismatch between client and server... command aborted");
            return exitCode;
        } catch (IOException e) {
            System.err.println("Bad connection to Router... command aborted");
            return exitCode;
        }

        Exception debugException = null;
        exitCode = 0;
        try {
            validateMax(argv);
            if ("-add".equals(cmd)) {
                if (addMount(argv, i)) {
                    System.out.println("Successfully added mount point " + argv[i]);
                } else {
                    exitCode = -1;
                }
            } else if ("-update".equals(cmd)) {
                if (updateMount(argv, i)) {
                    System.out.println("Successfully updated mount point " + argv[i]);
                    System.out.println(
                            "WARN: Changing order/destinations may lead to inconsistencies");
                } else {
                    exitCode = -1;
                }
            } else if ("-rm".equals(cmd)) {
                while (i < argv.length) {
                    try {
                        if (removeMount(argv[i])) {
                            System.out.println("Successfully removed mount point " + argv[i]);
                        }
                    } catch (IOException e) {
                        exitCode = -1;
                        System.err
                                .println(cmd.substring(1) + ": " + e.getLocalizedMessage());
                    }
                    i++;
                }
            } else if ("-ls".equals(cmd)) {
                listMounts(argv, i);
            } else if ("-getDestination".equals(cmd)) {
                getDestination(argv[i]);
            } else if ("-setQuota".equals(cmd)) {
                if (setQuota(argv, i)) {
                    System.out.println(
                            "Successfully set quota for mount point " + argv[i]);
                }
            } else if ("-setStorageTypeQuota".equals(cmd)) {
                if (setStorageTypeQuota(argv, i)) {
                    System.out.println(
                            "Successfully set storage type quota for mount point " + argv[i]);
                }
            } else if ("-clrQuota".equals(cmd)) {
                while (i < argv.length) {
                    if (clrQuota(argv[i])) {
                        System.out
                                .println("Successfully clear quota for mount point " + argv[i]);
                        i++;
                    }
                }
            } else if ("-clrStorageTypeQuota".equals(cmd)) {
                while (i < argv.length) {
                    if (clrStorageTypeQuota(argv[i])) {
                        System.out.println("Successfully clear storage type quota for mount"
                                + " point " + argv[i]);
                        i++;
                    }
                }
            } else if ("-safemode".equals(cmd)) {
                manageSafeMode(argv[i]);
            } else if ("-nameservice".equals(cmd)) {
                String subcmd = argv[i];
                String nsId = argv[i + 1];
                manageNameservice(subcmd, nsId);
            } else if ("-getDisabledNameservices".equals(cmd)) {
                getDisabledNameservices();
            } else if ("-refresh".equals(cmd)) {
                refresh(address);
            } else if ("-refreshRouterArgs".equals(cmd)) {
                exitCode = genericRefresh(argv, i);
            } else if ("-refreshSuperUserGroupsConfiguration".equals(cmd)) {
                exitCode = refreshSuperUserGroupsConfiguration();
            } else if ("-refreshCallQueue".equals(cmd)) {
                exitCode = refreshCallQueue();
            } else {
                throw new IllegalArgumentException("Unknown Command: " + cmd);
            }
        } catch (IllegalArgumentException arge) {
            debugException = arge;
            exitCode = -1;
            System.err.println(cmd.substring(1) + ": " + arge.getLocalizedMessage());
            printUsage(cmd);
        } catch (RemoteException e) {
            // This is a error returned by the server.
            // Print out the first line of the error message, ignore the stack trace.
            exitCode = -1;
            debugException = e;
            try {
                String[] content;
                content = e.getLocalizedMessage().split("\n");
                System.err.println(cmd.substring(1) + ": " + content[0]);
                e.printStackTrace();
            } catch (Exception ex) {
                System.err.println(cmd.substring(1) + ": " + ex.getLocalizedMessage());
                e.printStackTrace();
                debugException = ex;
            }
        } catch (IOException ioe) {
            exitCode = -1;
            System.err.println(cmd.substring(1) + ": " + ioe.getLocalizedMessage());
            printUsage(cmd);
        } catch (Exception e) {
            exitCode = -1;
            debugException = e;
            System.err.println(cmd.substring(1) + ": " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        if (debugException != null) {
            LOG.debug("Exception encountered", debugException);
        }
        return exitCode;
    }

    private int refreshSuperUserGroupsConfiguration()
            throws IOException {
        RouterGenericManager proxy = client.getRouterGenericManager();
        String address = getConf().getTrimmed(
                RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_KEY,
                RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_DEFAULT);
        if (proxy.refreshSuperUserGroupsConfiguration()) {
            System.out.println(
                    "Successfully updated superuser proxy groups on router " + address);
            return 0;
        }
        return -1;
    }

    private void refresh(String address) throws IOException {
        if (refreshRouterCache()) {
            System.out.println(
                    "Successfully updated mount table cache on router " + address);
        }
    }

    private boolean refreshRouterCache() throws IOException {
        RefreshMountTableEntriesResponse response =
                client.getMountTableManager().refreshMountTableEntries(
                        RefreshMountTableEntriesRequest.newInstance());
        return response.getResult();
    }


    public boolean addMount(String[] parameters, int i) throws IOException {
        String mount = parameters[i++];
        String[] nss = parameters[i++].split(",");
        String dest = parameters[i++];

        // 可选参数
        boolean readOnly = false;
        boolean faultTolerant = false;
        String owner = null;
        String group = null;
        FsPermission mode = null;
        DestinationOrder order = DestinationOrder.HASH;
        while (i < parameters.length) {
            switch (parameters[i]) {
                case "-readonly":
                    readOnly = true;
                    break;
                case "-faulttolerant":
                    faultTolerant = true;
                    break;
                case "-order":
                    i++;
                    try {
                        order = DestinationOrder.valueOf(parameters[i]);
                    } catch (Exception e) {
                        System.err.println("Cannot parse order: " + parameters[i]);
                    }
                    break;
                case "-owner":
                    i++;
                    owner = parameters[i];
                    break;
                case "-group":
                    i++;
                    group = parameters[i];
                    break;
                case "-mode":
                    i++;
                    short modeValue = Short.parseShort(parameters[i], 8);
                    mode = new FsPermission(modeValue);
                    break;
                default:
                    printUsage("-add");
                    return false;
            }

            i++;
        }

        return addMount(mount, nss, dest, readOnly, faultTolerant, order,
                new ACLEntity(owner, group, mode));
    }

    public boolean addMount(String mount, String[] nss, String dest,
                            boolean readonly, boolean faultTolerant, DestinationOrder order,
                            ACLEntity aclInfo)
            throws IOException {
        mount = normalizeFileSystemPath(mount);
        // Get the existing entry
        MountTableManager mountTable = client.getMountTableManager();
        MountTable existingEntry = getMountEntry(mount, mountTable);

        if (existingEntry == null) {
            // Create and add the entry if it doesn't exist
            Map<String, String> destMap = new LinkedHashMap<>();
            for (String ns : nss) {
                destMap.put(ns, dest);
            }
            MountTable newEntry = MountTable.newInstance(mount, destMap);
            if (readonly) {
                newEntry.setReadOnly(true);
            }
            if (faultTolerant) {
                newEntry.setFaultTolerant(true);
            }
            if (order != null) {
                newEntry.setDestOrder(order);
            }

            if (aclInfo.getOwner() != null) {
                newEntry.setOwnerName(aclInfo.getOwner());
            }

            if (aclInfo.getGroup() != null) {
                newEntry.setGroupName(aclInfo.getGroup());
            }

            if (aclInfo.getMode() != null) {
                newEntry.setMode(aclInfo.getMode());
            }

            newEntry.validate();

            AddMountTableEntryRequest request =
                    AddMountTableEntryRequest.newInstance(newEntry);
            AddMountTableEntryResponse addResponse =
                    mountTable.addMountTableEntry(request);
            boolean added = addResponse.getStatus();
            if (!added) {
                System.err.println("Cannot add mount point " + mount);
            }
            return added;
        } else {
            for (String nsId : nss) {
                if (!existingEntry.addDestination(nsId, dest)) {
                    System.err.println("Cannot add destination at " + nsId + " " + dest);
                    return false;
                }
            }
            if (readonly) {
                existingEntry.setReadOnly(true);
            }
            if (faultTolerant) {
                existingEntry.setFaultTolerant(true);
            }
            if (order != null) {
                existingEntry.setDestOrder(order);
            }

            // Update ACL info of mount table entry
            if (aclInfo.getOwner() != null) {
                existingEntry.setOwnerName(aclInfo.getOwner());
            }

            if (aclInfo.getGroup() != null) {
                existingEntry.setGroupName(aclInfo.getGroup());
            }

            if (aclInfo.getMode() != null) {
                existingEntry.setMode(aclInfo.getMode());
            }

            existingEntry.validate();

            UpdateMountTableEntryRequest updateRequest =
                    UpdateMountTableEntryRequest.newInstance(existingEntry);
            UpdateMountTableEntryResponse updateResponse =
                    mountTable.updateMountTableEntry(updateRequest);
            boolean updated = updateResponse.getStatus();
            if (!updated) {
                System.err.println("Cannot update mount point " + mount);
            }
            return updated;
        }
    }

    public boolean updateMount(String[] parameters, int i) throws IOException {
        String mount = parameters[i++];
        mount = normalizeFileSystemPath(mount);
        MountTableManager mountTable = client.getMountTableManager();
        MountTable existingEntry = getMountEntry(mount, mountTable);
        if (existingEntry == null) {
            throw new IOException(mount + " doesn't exist.");
        }
        // Check if the destination needs to be updated.

        if (!parameters[i].startsWith("-")) {
            String[] nss = parameters[i++].split(",");
            String dest = parameters[i++];
            Map<String, String> destMap = new LinkedHashMap<>();
            for (String ns : nss) {
                destMap.put(ns, dest);
            }
            final List<RemoteLocation> locations = new LinkedList<>();
            for (Entry<String, String> entry : destMap.entrySet()) {
                String nsId = entry.getKey();
                String path = normalizeFileSystemPath(entry.getValue());
                RemoteLocation location = new RemoteLocation(nsId, path, mount);
                locations.add(location);
            }
            existingEntry.setDestinations(locations);
        }
        try {
            while (i < parameters.length) {
                switch (parameters[i]) {
                    case "-readonly":
                        i++;
                        existingEntry.setReadOnly(getBooleanValue(parameters[i]));
                        break;
                    case "-faulttolerant":
                        i++;
                        existingEntry.setFaultTolerant(getBooleanValue(parameters[i]));
                        break;
                    case "-order":
                        i++;
                        try {
                            existingEntry.setDestOrder(DestinationOrder.valueOf(parameters[i]));
                            break;
                        } catch (Exception e) {
                            throw new Exception("Cannot parse order: " + parameters[i]);
                        }
                    case "-owner":
                        i++;
                        existingEntry.setOwnerName(parameters[i]);
                        break;
                    case "-group":
                        i++;
                        existingEntry.setGroupName(parameters[i]);
                        break;
                    case "-mode":
                        i++;
                        short modeValue = Short.parseShort(parameters[i], 8);
                        existingEntry.setMode(new FsPermission(modeValue));
                        break;
                    default:
                        printUsage("-update");
                        return false;
                }
                i++;
            }
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            String msg = "Unable to parse arguments: " + e.getMessage();
            if (e instanceof ArrayIndexOutOfBoundsException) {
                msg = "Unable to parse arguments: no value provided for "
                        + parameters[i - 1];
            }
            throw new IOException(msg);
        }
        UpdateMountTableEntryRequest updateRequest =
                UpdateMountTableEntryRequest.newInstance(existingEntry);
        UpdateMountTableEntryResponse updateResponse =
                mountTable.updateMountTableEntry(updateRequest);
        boolean updated = updateResponse.getStatus();
        if (!updated) {
            System.err.println("Cannot update mount point " + mount);
        }
        return updated;
    }

    private boolean getBooleanValue(String value) throws Exception {
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new IllegalArgumentException("Invalid argument: " + value
                + ". Please specify either true or false.");
    }

    private MountTable getMountEntry(String mount, MountTableManager mountTable)
            throws IOException {
        GetMountTableEntriesRequest getRequest =
                GetMountTableEntriesRequest.newInstance(mount);
        GetMountTableEntriesResponse getResponse =
                mountTable.getMountTableEntries(getRequest);
        List<MountTable> results = getResponse.getEntries();
        MountTable existingEntry = null;
        for (MountTable result : results) {
            if (mount.equals(result.getSourcePath())) {
                existingEntry = result;
            }
        }
        return existingEntry;
    }

    public boolean removeMount(String path) throws IOException {
        path = normalizeFileSystemPath(path);
        MountTableManager mountTable = client.getMountTableManager();
        RemoveMountTableEntryRequest request =
                RemoveMountTableEntryRequest.newInstance(path);
        RemoveMountTableEntryResponse response =
                mountTable.removeMountTableEntry(request);
        boolean removed = response.getStatus();
        if (!removed) {
            System.out.println("Cannot remove mount point " + path);
        }
        return removed;
    }

    public void listMounts(String[] argv, int i) throws IOException {
        String path;
        boolean detail = false;
        if (argv.length == 1) {
            path = "/";
        } else if (argv[i].equals("-d")) { // Check if -d parameter is specified.
            detail = true;
            if (argv.length == 2) {
                path = "/"; // If no path is provide with -ls -d.
            } else {
                path = argv[++i];
            }
        } else {
            path = argv[i];
        }

        path = normalizeFileSystemPath(path);
        MountTableManager mountTable = client.getMountTableManager();
        GetMountTableEntriesRequest request =
                GetMountTableEntriesRequest.newInstance(path);
        GetMountTableEntriesResponse response =
                mountTable.getMountTableEntries(request);
        List<MountTable> entries = response.getEntries();
        printMounts(entries, detail);
    }

    private static void printMounts(List<MountTable> entries, boolean detail) {
        System.out.println("Mount Table Entries:");
        if (detail) {
            System.out.printf("%-25s %-25s %-25s %-25s %-10s %-30s %-10s %-10s %-15s%n",
                    "Source", "Destinations", "Owner", "Group", "Mode", "Quota/Usage",
                    "Order", "ReadOnly", "FaultTolerant");
        } else {
            System.out.printf("%-25s %-25s %-25s %-25s %-10s %-30s%n",
                    "Source", "Destinations", "Owner", "Group", "Mode", "Quota/Usage");
        }
        for (MountTable entry : entries) {
            StringBuilder destBuilder = new StringBuilder();
            for (RemoteLocation location : entry.getDestinations()) {
                if (destBuilder.length() > 0) {
                    destBuilder.append(",");
                }
                destBuilder.append(String.format("%s->%s", location.getNameserviceId(),
                        location.getDest()));
            }
            System.out.printf("%-25s %-25s", entry.getSourcePath(),
                    destBuilder.toString());

            System.out.printf(" %-25s %-25s %-10s",
                    entry.getOwnerName(), entry.getGroupName(), entry.getMode());

            System.out.printf(" %-30s", entry.getQuota());

            if (detail) {
                System.out.printf(" %-10s", entry.getDestOrder());

                System.out.printf(" %-10s", entry.isReadOnly() ? "Read-Only" : "");

                System.out.printf(" %-15s",
                        entry.isFaultTolerant() ? "Fault-Tolerant" : "");
            }
            System.out.println();
        }
    }

    private void getDestination(String path) throws IOException {
        path = normalizeFileSystemPath(path);
        MountTableManager mountTable = client.getMountTableManager();
        GetDestinationRequest request =
                GetDestinationRequest.newInstance(path);
        GetDestinationResponse response = mountTable.getDestination(request);
        System.out.println("Destination: " +
                StringUtils.join(",", response.getDestinations()));
    }

    private boolean setQuota(String[] parameters, int i) throws IOException {
        long nsQuota = HdfsConstants.QUOTA_DONT_SET;
        long ssQuota = HdfsConstants.QUOTA_DONT_SET;

        String mount = parameters[i++];
        while (i < parameters.length) {
            if (parameters[i].equals("-nsQuota")) {
                i++;
                try {
                    nsQuota = Long.parseLong(parameters[i]);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Cannot parse nsQuota: " + parameters[i]);
                }
            } else if (parameters[i].equals("-ssQuota")) {
                i++;
                try {
                    ssQuota = StringUtils.TraditionalBinaryPrefix
                            .string2long(parameters[i]);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Cannot parse ssQuota: " + parameters[i]);
                }
            } else {
                throw new IllegalArgumentException(
                        "Invalid argument : " + parameters[i]);
            }

            i++;
        }

        if (nsQuota <= 0 || ssQuota <= 0) {
            throw new IllegalArgumentException(
                    "Input quota value should be a positive number.");
        }

        if (nsQuota == HdfsConstants.QUOTA_DONT_SET &&
                ssQuota == HdfsConstants.QUOTA_DONT_SET) {
            throw new IllegalArgumentException(
                    "Must specify at least one of -nsQuota and -ssQuota.");
        }

        return updateQuota(mount, nsQuota, ssQuota);
    }

    private boolean setStorageTypeQuota(String[] parameters, int i)
            throws IOException {
        long[] typeQuota = new long[StorageType.values().length];
        eachByStorageType(
                t -> typeQuota[t.ordinal()] = HdfsConstants.QUOTA_DONT_SET);

        String mount = parameters[i++];
        if (parameters[i].equals("-storageType")) {
            i++;
            StorageType type = StorageType.parseStorageType(parameters[i++]);
            typeQuota[type.ordinal()] = Long.parseLong(parameters[i]);
        } else {
            throw new IllegalArgumentException("Invalid argument : " + parameters[i]);
        }

        if (orByStorageType(t -> typeQuota[t.ordinal()] <= 0)) {
            throw new IllegalArgumentException(
                    "Input quota value should be a positive number.");
        }

        if (andByStorageType(
                t -> typeQuota[t.ordinal()] == HdfsConstants.QUOTA_DONT_SET)) {
            throw new IllegalArgumentException(
                    "Must specify at least one of -nsQuota and -ssQuota.");
        }

        return updateStorageTypeQuota(mount, typeQuota);
    }

    private boolean clrQuota(String mount) throws IOException {
        return updateQuota(mount, HdfsConstants.QUOTA_RESET,
                HdfsConstants.QUOTA_RESET);
    }

    private boolean clrStorageTypeQuota(String mount) throws IOException {
        long[] typeQuota = new long[StorageType.values().length];
        eachByStorageType(t -> typeQuota[t.ordinal()] = HdfsConstants.QUOTA_RESET);
        return updateStorageTypeQuota(mount, typeQuota);
    }

    private boolean updateQuota(String mount, long nsQuota, long ssQuota)
            throws IOException {
        // Get existing entry
        MountTableManager mountTable = client.getMountTableManager();
        GetMountTableEntriesRequest getRequest = GetMountTableEntriesRequest
                .newInstance(mount);
        GetMountTableEntriesResponse getResponse = mountTable
                .getMountTableEntries(getRequest);
        List<MountTable> results = getResponse.getEntries();
        MountTable existingEntry = null;
        for (MountTable result : results) {
            if (mount.equals(result.getSourcePath())) {
                existingEntry = result;
                break;
            }
        }

        if (existingEntry == null) {
            throw new IOException(mount + " doesn't exist in mount table.");
        } else {
            long nsCount = existingEntry.getQuota().getFileAndDirectoryCount();
            long ssCount = existingEntry.getQuota().getSpaceConsumed();
            // If nsQuota and ssQuota were reset, clear nsQuota and ssQuota.
            if (nsQuota == HdfsConstants.QUOTA_RESET &&
                    ssQuota == HdfsConstants.QUOTA_RESET) {
                nsCount = RouterQuotaUsage.QUOTA_USAGE_COUNT_DEFAULT;
                ssCount = RouterQuotaUsage.QUOTA_USAGE_COUNT_DEFAULT;
            } else {
                // If nsQuota or ssQuota was unset, use the value in mount table.
                if (nsQuota == HdfsConstants.QUOTA_DONT_SET) {
                    nsQuota = existingEntry.getQuota().getQuota();
                }
                if (ssQuota == HdfsConstants.QUOTA_DONT_SET) {
                    ssQuota = existingEntry.getQuota().getSpaceQuota();
                }
            }

            RouterQuotaUsage updatedQuota = new RouterQuotaUsage.Builder()
                    .fileAndDirectoryCount(nsCount).quota(nsQuota)
                    .spaceConsumed(ssCount).spaceQuota(ssQuota).build();
            existingEntry.setQuota(updatedQuota);
        }

        UpdateMountTableEntryRequest updateRequest =
                UpdateMountTableEntryRequest.newInstance(existingEntry);
        UpdateMountTableEntryResponse updateResponse = mountTable
                .updateMountTableEntry(updateRequest);
        return updateResponse.getStatus();
    }

    private boolean updateStorageTypeQuota(String mount, long[] typeQuota)
            throws IOException {
        // Get existing entry
        MountTableManager mountTable = client.getMountTableManager();
        GetMountTableEntriesRequest getRequest = GetMountTableEntriesRequest
                .newInstance(mount);
        GetMountTableEntriesResponse getResponse = mountTable
                .getMountTableEntries(getRequest);
        List<MountTable> results = getResponse.getEntries();
        MountTable existingEntry = null;
        for (MountTable result : results) {
            if (mount.equals(result.getSourcePath())) {
                existingEntry = result;
                break;
            }
        }

        if (existingEntry == null) {
            throw new IOException(mount + " doesn't exist in mount table.");
        } else {
            final RouterQuotaUsage quotaUsage = existingEntry.getQuota();
            long[] typeCount = new long[StorageType.values().length];
            eachByStorageType(
                    t -> typeCount[t.ordinal()] = quotaUsage.getTypeQuota(t));
            // If all storage type quota were reset, clear the storage type quota.
            if (andByStorageType(
                    t -> typeQuota[t.ordinal()] == HdfsConstants.QUOTA_RESET)) {
                eachByStorageType(t -> typeCount[t.ordinal()] =
                        RouterQuotaUsage.QUOTA_USAGE_COUNT_DEFAULT);
            } else {
                // If nsQuota or ssQuota was unset, use the value in mount table.
                eachByStorageType(t -> {
                    if (typeQuota[t.ordinal()] == HdfsConstants.QUOTA_DONT_SET) {
                        typeQuota[t.ordinal()] = quotaUsage.getTypeQuota(t);
                    }
                });
            }

            RouterQuotaUsage updatedQuota = new RouterQuotaUsage.Builder()
                    .typeQuota(typeQuota).typeConsumed(typeCount).build();
            existingEntry.setQuota(updatedQuota);
        }

        UpdateMountTableEntryRequest updateRequest =
                UpdateMountTableEntryRequest.newInstance(existingEntry);
        UpdateMountTableEntryResponse updateResponse = mountTable
                .updateMountTableEntry(updateRequest);
        return updateResponse.getStatus();
    }

    private void manageSafeMode(String cmd) throws IOException {
        switch (cmd) {
            case "enter":
                if (enterSafeMode()) {
                    System.out.println("Successfully enter safe mode.");
                }
                break;
            case "leave":
                if (leaveSafeMode()) {
                    System.out.println("Successfully leave safe mode.");
                }
                break;
            case "get":
                boolean result = getSafeMode();
                System.out.println("Safe Mode: " + result);
                break;
            default:
                throw new IllegalArgumentException("Invalid argument: " + cmd);
        }
    }

    private boolean enterSafeMode() throws IOException {
        RouterStateManager stateManager = client.getRouterStateManager();
        EnterSafeModeResponse response = stateManager.enterSafeMode(
                EnterSafeModeRequest.newInstance());
        return response.getStatus();
    }

    private boolean leaveSafeMode() throws IOException {
        RouterStateManager stateManager = client.getRouterStateManager();
        LeaveSafeModeResponse response = stateManager.leaveSafeMode(
                LeaveSafeModeRequest.newInstance());
        return response.getStatus();
    }

    private boolean getSafeMode() throws IOException {
        RouterStateManager stateManager = client.getRouterStateManager();
        GetSafeModeResponse response = stateManager.getSafeMode(
                GetSafeModeRequest.newInstance());
        return response.isInSafeMode();
    }

    private void manageNameservice(String cmd, String nsId) throws IOException {
        if (cmd.equals("enable")) {
            if (enableNameservice(nsId)) {
                System.out.println("Successfully enabled nameservice " + nsId);
            } else {
                System.err.println("Cannot enable " + nsId);
            }
        } else if (cmd.equals("disable")) {
            if (disableNameservice(nsId)) {
                System.out.println("Successfully disabled nameservice " + nsId);
            } else {
                System.err.println("Cannot disable " + nsId);
            }
        } else {
            throw new IllegalArgumentException("Unknown command: " + cmd);
        }
    }

    private boolean disableNameservice(String nsId) throws IOException {
        NameserviceManager nameserviceManager = client.getNameserviceManager();
        DisableNameserviceResponse response =
                nameserviceManager.disableNameservice(
                        DisableNameserviceRequest.newInstance(nsId));
        return response.getStatus();
    }

    private boolean enableNameservice(String nsId) throws IOException {
        NameserviceManager nameserviceManager = client.getNameserviceManager();
        EnableNameserviceResponse response =
                nameserviceManager.enableNameservice(
                        EnableNameserviceRequest.newInstance(nsId));
        return response.getStatus();
    }

    private void getDisabledNameservices() throws IOException {
        NameserviceManager nameserviceManager = client.getNameserviceManager();
        GetDisabledNameservicesRequest request =
                GetDisabledNameservicesRequest.newInstance();
        GetDisabledNameservicesResponse response =
                nameserviceManager.getDisabledNameservices(request);
        System.out.println("List of disabled nameservices:");
        for (String nsId : response.getNameservices()) {
            System.out.println(nsId);
        }
    }

    public int genericRefresh(String[] argv, int i) throws IOException {
        String hostport = argv[i++];
        String identifier = argv[i++];
        String[] args = Arrays.copyOfRange(argv, i, argv.length);

        Configuration conf = getConf();

        conf.set(CommonConfigurationKeys.HADOOP_SECURITY_SERVICE_USER_NAME_KEY,
                conf.get(DFSConfigKeys.DFS_NAMENODE_KERBEROS_PRINCIPAL_KEY, ""));

        Class<?> xface = GenericRefreshProtocolPB.class;
        InetSocketAddress address = NetUtils.createSocketAddr(hostport);
        UserGroupInformation ugi = UserGroupInformation.getCurrentUser();

        RPC.setProtocolEngine(conf, xface, ProtobufRpcEngine2.class);
        GenericRefreshProtocolPB proxy = (GenericRefreshProtocolPB) RPC.getProxy(
                xface, RPC.getProtocolVersion(xface), address, ugi, conf,
                NetUtils.getDefaultSocketFactory(conf), 0);

        Collection<RefreshResponse> responses = null;
        try (GenericRefreshProtocolClientSideTranslatorPB xlator =
                     new GenericRefreshProtocolClientSideTranslatorPB(proxy)) {
            responses = xlator.refresh(identifier, args);

            int returnCode = 0;
            System.out.println("Refresh Responses:\n");
            for (RefreshResponse response : responses) {
                System.out.println(response.toString());

                if (returnCode == 0 && response.getReturnCode() != 0) {
                    returnCode = response.getReturnCode();
                } else if (returnCode != 0 && response.getReturnCode() != 0) {
                    returnCode = -1;
                }
            }
            return returnCode;
        } finally {
            if (responses == null) {
                System.out.println("Failed to get response.\n");
                return -1;
            }
        }
    }

    private int refreshCallQueue() throws IOException {
        Configuration conf = getConf();
        String hostport = getConf().getTrimmed(
                RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_KEY,
                RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_DEFAULT);

        // Create the client
        Class<?> xface = RefreshCallQueueProtocolPB.class;
        InetSocketAddress address = NetUtils.createSocketAddr(hostport);
        UserGroupInformation ugi = UserGroupInformation.getCurrentUser();

        RPC.setProtocolEngine(conf, xface, ProtobufRpcEngine2.class);
        RefreshCallQueueProtocolPB proxy = (RefreshCallQueueProtocolPB) RPC.getProxy(
                xface, RPC.getProtocolVersion(xface), address, ugi, conf,
                NetUtils.getDefaultSocketFactory(conf), 0);

        int returnCode = -1;
        try (RefreshCallQueueProtocolClientSideTranslatorPB xlator =
                     new RefreshCallQueueProtocolClientSideTranslatorPB(proxy)) {
            xlator.refreshCallQueue();
            System.out.println("Refresh call queue successfully for " + hostport);
            returnCode = 0;
        } catch (IOException ioe) {
            System.out.println("Refresh call queue unsuccessfully for " + hostport);
        }
        return returnCode;
    }

    public static String normalizeFileSystemPath(final String str) {
        String path = SLASHES.matcher(str).replaceAll("/");
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    static class ACLEntity {
        private final String owner;
        private final String group;
        private final FsPermission mode;

        ACLEntity(String owner, String group, FsPermission mode) {
            this.owner = owner;
            this.group = group;
            this.mode = mode;
        }

        public String getOwner() {
            return owner;
        }

        public String getGroup() {
            return group;
        }

        public FsPermission getMode() {
            return mode;
        }
    }

}
