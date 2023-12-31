package org.apache.hadoop.hdfs.server.federation.store.driver.impl;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link StateStoreDriver} implementation based on a filesystem. The common
 * implementation uses HDFS as a backend. The path can be specified setting
 * dfs.federation.router.driver.fs.path=hdfs://host:port/path/to/store.
 */
public class StateStoreFileSystemImpl extends StateStoreFileBaseImpl {

    private static final Logger LOG =
            LoggerFactory.getLogger(StateStoreFileSystemImpl.class);

    /** Configuration keys. */
    public static final String FEDERATION_STORE_FS_PATH =
            RBFConfigKeys.FEDERATION_STORE_PREFIX + "driver.fs.path";

    /** File system to back the State Store. */
    private FileSystem fs;

    /** Working path in the filesystem. */
    private String workPath;

    @Override
    protected boolean exists(String path) {
        try {
            return fs.exists(new Path(path));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected boolean mkdir(String path) {
        try {
            return fs.mkdirs(new Path(path));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected boolean rename(String src, String dst) {
        try {
            if (fs instanceof DistributedFileSystem) {
                DistributedFileSystem dfs = (DistributedFileSystem) fs;
                dfs.rename(new Path(src), new Path(dst), Options.Rename.OVERWRITE);
                return true;
            } else {
                // Replace should be atomic but not available
                if (fs.exists(new Path(dst))) {
                    fs.delete(new Path(dst), true);
                }
                return fs.rename(new Path(src), new Path(dst));
            }
        } catch (Exception e) {
            LOG.error("Cannot rename {} to {}", src, dst, e);
            return false;
        }
    }

    @Override
    protected boolean remove(String path) {
        try {
            return fs.delete(new Path(path), true);
        } catch (Exception e) {
            LOG.error("Cannot remove {}", path, e);
            return false;
        }
    }

    @Override
    protected String getRootDir() {
        if (this.workPath == null) {
            String rootPath = getConf().get(FEDERATION_STORE_FS_PATH);
            URI workUri;
            try {
                workUri = new URI(rootPath);
                fs = FileSystem.get(workUri, getConf());
            } catch (Exception ex) {
                return null;
            }
            this.workPath = rootPath;
        }
        return this.workPath;
    }

    @Override
    public void close() throws Exception {
        if (fs != null) {
            fs.close();
        }
    }

    @Override
    protected <T extends BaseRecord> BufferedReader getReader(String pathName) {
        BufferedReader reader = null;
        Path path = new Path(pathName);
        try {
            FSDataInputStream fdis = fs.open(path);
            InputStreamReader isr =
                    new InputStreamReader(fdis, StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
        } catch (IOException ex) {
            LOG.error("Cannot open read stream for {}", path, ex);
        }
        return reader;
    }

    @Override
    protected <T extends BaseRecord> BufferedWriter getWriter(String pathName) {
        BufferedWriter writer = null;
        Path path = new Path(pathName);
        try {
            FSDataOutputStream fdos = fs.create(path, true);
            OutputStreamWriter osw =
                    new OutputStreamWriter(fdos, StandardCharsets.UTF_8);
            writer = new BufferedWriter(osw);
        } catch (IOException ex) {
            LOG.error("Cannot open write stream for {}", path, ex);
        }
        return writer;
    }

    @Override
    protected List<String> getChildren(String pathName) {
        Path path = new Path(workPath, pathName);
        try {
            FileStatus[] files = fs.listStatus(path);
            List<String> ret = new ArrayList<>(files.length);
            for (FileStatus file : files) {
                Path filePath = file.getPath();
                String fileName = filePath.getName();
                ret.add(fileName);
            }
            return ret;
        } catch (Exception e) {
            LOG.error("Cannot get children for {}", pathName, e);
            return Collections.emptyList();
        }
    }
}
