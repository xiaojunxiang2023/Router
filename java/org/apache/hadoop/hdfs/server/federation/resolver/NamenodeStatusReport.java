package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.ha.HAServiceProtocol.HAServiceState;
import org.apache.hadoop.hdfs.server.protocol.NamespaceInfo;

// 汇报 namenode状态信息 的描述
public class NamenodeStatusReport {

    private String nameserviceId = "";
    private String namenodeId = "";
    private String clusterId = "";
    private String blockPoolId = "";
    private String rpcAddress = "";
    private String serviceAddress = "";
    private String lifelineAddress = "";
    private String webAddress = "";
    private String webScheme = "";

    private HAServiceState status = HAServiceState.STANDBY;
    private boolean safeMode = false;

    private int liveDatanodes = -1;
    private int deadDatanodes = -1;
    private int staleDatanodes = -1;
    private int decomDatanodes = -1;
    private int liveDecomDatanodes = -1;
    private int deadDecomDatanodes = -1;

    private int inMaintenanceLiveDataNodes = -1;
    private int inMaintenanceDeadDataNodes = -1;
    private int enteringMaintenanceDataNodes = -1;

    private long availableSpace = -1;
    private long numOfFiles = -1;
    private long numOfBlocks = -1;
    private long numOfBlocksMissing = -1;
    private long numOfBlocksPendingReplication = -1;
    private long numOfBlocksUnderReplicated = -1;
    private long numOfBlocksPendingDeletion = -1;
    private long totalSpace = -1;
    private long providedSpace = -1;

    private boolean registrationValid = false;
    private boolean statsValid = false;
    private boolean haStateValid = false;

    public NamenodeStatusReport(String ns, String nn, String rpc, String service,
                                String lifeline, String webScheme, String web) {
        this.nameserviceId = ns;
        this.namenodeId = nn;
        this.rpcAddress = rpc;
        this.serviceAddress = service;
        this.lifelineAddress = lifeline;
        this.webScheme = webScheme;
        this.webAddress = web;
    }

    public boolean statsValid() {
        return this.statsValid;
    }

    public boolean registrationValid() {
        return this.registrationValid;
    }

    /**
     * If the HA state is valid.
     *
     * @return If the HA state is valid.
     */
    public boolean haStateValid() {
        return this.haStateValid;
    }

    public FederationNamenodeServiceState getState() {
        if (!registrationValid) {
            return FederationNamenodeServiceState.UNAVAILABLE;
        } else if (haStateValid) {
            return FederationNamenodeServiceState.getState(status);
        } else {
            return FederationNamenodeServiceState.ACTIVE;
        }
    }

    public String getNameserviceId() {
        return this.nameserviceId;
    }

    public String getNamenodeId() {
        return this.namenodeId;
    }

    public String getClusterId() {
        return this.clusterId;
    }

    public String getBlockPoolId() {
        return this.blockPoolId;
    }

    public String getRpcAddress() {
        return this.rpcAddress;
    }

    public String getServiceAddress() {
        return this.serviceAddress;
    }

    public String getLifelineAddress() {
        return this.lifelineAddress;
    }

    public String getWebAddress() {
        return this.webAddress;
    }

    public String getWebScheme() {
        return this.webScheme;
    }

    public void setHAServiceState(HAServiceState state) {
        this.status = state;
        this.haStateValid = true;
    }

    public void setNamespaceInfo(NamespaceInfo info) {
        this.clusterId = info.getClusterID();
        this.blockPoolId = info.getBlockPoolID();
        this.registrationValid = true;
    }

    public void setSafeMode(boolean safemode) {
        this.safeMode = safemode;
    }

    public boolean getSafemode() {
        return this.safeMode;
    }

    public void setNamesystemInfo(long available, long total,
                                  long numFiles, long numBlocks, long numBlocksMissing,
                                  long numBlocksPendingReplication, long numBlocksUnderReplicated,
                                  long numBlocksPendingDeletion, long providedSpace) {
        this.totalSpace = total;
        this.availableSpace = available;
        this.numOfBlocks = numBlocks;
        this.numOfBlocksMissing = numBlocksMissing;
        this.numOfBlocksPendingReplication = numBlocksPendingReplication;
        this.numOfBlocksUnderReplicated = numBlocksUnderReplicated;
        this.numOfBlocksPendingDeletion = numBlocksPendingDeletion;
        this.numOfFiles = numFiles;
        this.statsValid = true;
        this.providedSpace = providedSpace;
    }

    public void setDatanodeInfo(int numLive, int numDead, int numStale,
                                int numDecom, int numLiveDecom, int numDeadDecom,
                                int numInMaintenanceLive, int numInMaintenanceDead,
                                int numEnteringMaintenance) {
        this.liveDatanodes = numLive;
        this.deadDatanodes = numDead;
        this.staleDatanodes = numStale;
        this.decomDatanodes = numDecom;
        this.liveDecomDatanodes = numLiveDecom;
        this.deadDecomDatanodes = numDeadDecom;
        this.inMaintenanceLiveDataNodes = numInMaintenanceLive;
        this.inMaintenanceDeadDataNodes = numInMaintenanceDead;
        this.enteringMaintenanceDataNodes = numEnteringMaintenance;
        this.statsValid = true;
    }

    public int getNumLiveDatanodes() {
        return this.liveDatanodes;
    }

    public int getNumDeadDatanodes() {
        return this.deadDatanodes;
    }

    public int getNumStaleDatanodes() {
        return this.staleDatanodes;
    }

    public int getNumDecommissioningDatanodes() {
        return this.decomDatanodes;
    }


    public int getNumDecomLiveDatanodes() {
        return this.liveDecomDatanodes;
    }

    public int getNumDecomDeadDatanodes() {
        return this.deadDecomDatanodes;
    }


    public int getNumInMaintenanceLiveDataNodes() {
        return this.inMaintenanceLiveDataNodes;
    }

    public int getNumInMaintenanceDeadDataNodes() {
        return this.inMaintenanceDeadDataNodes;
    }

    public int getNumEnteringMaintenanceDataNodes() {
        return this.enteringMaintenanceDataNodes;
    }


    public long getNumBlocks() {
        return this.numOfBlocks;
    }

    public long getNumFiles() {
        return this.numOfFiles;
    }

    public long getTotalSpace() {
        return this.totalSpace;
    }

    public long getAvailableSpace() {
        return this.availableSpace;
    }

    public long getProvidedSpace() {
        return this.providedSpace;
    }

    public long getNumBlocksMissing() {
        return this.numOfBlocksMissing;
    }

    public long getNumOfBlocksPendingReplication() {
        return this.numOfBlocksPendingReplication;
    }

    public long getNumOfBlocksUnderReplicated() {
        return this.numOfBlocksUnderReplicated;
    }

    public long getNumOfBlocksPendingDeletion() {
        return this.numOfBlocksPendingDeletion;
    }

    public void setRegistrationValid(boolean isValid) {
        this.registrationValid = isValid;
    }

    @Override
    public String toString() {
        return String.format("%s-%s:%s",
                nameserviceId, namenodeId, serviceAddress);
    }
}
