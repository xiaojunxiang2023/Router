package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.hdfs.server.federation.router.RemoteLocationContext;

// 远程路径 destinations，对（ns_id + dest）的描述
public class RemoteLocation extends RemoteLocationContext {

    private final String nameserviceId;
    private final String namenodeId;
    private final String dstPath;
    private final String srcPath;

    public RemoteLocation(String nsId, String dPath, String sPath) {
        this(nsId, null, dPath, sPath);
    }

    public RemoteLocation(String nsId, String nnId, String dPath, String sPath) {
        this.nameserviceId = nsId;
        this.namenodeId = nnId;
        this.dstPath = dPath;
        this.srcPath = sPath;
    }

    @Override
    public String getNameserviceId() {
        String ret = this.nameserviceId;
        if (this.namenodeId != null) {
            ret += "-" + this.namenodeId;
        }
        return ret;
    }

    @Override
    public String getDest() {
        return this.dstPath;
    }

    @Override
    public String getSrc() {
        return this.srcPath;
    }

    @Override
    public String toString() {
        return getNameserviceId() + "->" + this.dstPath;
    }
}