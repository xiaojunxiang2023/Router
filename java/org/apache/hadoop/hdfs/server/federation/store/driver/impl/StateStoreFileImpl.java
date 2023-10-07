package org.apache.hadoop.hdfs.server.federation.store.driver.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.thirdparty.com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StateStoreDriver implementation based on a local file.
 */
public class StateStoreFileImpl extends StateStoreFileBaseImpl {

    private static final Logger LOG =
            LoggerFactory.getLogger(StateStoreFileImpl.class);

    /** Configuration keys. */
    public static final String FEDERATION_STORE_FILE_DIRECTORY =
            RBFConfigKeys.FEDERATION_STORE_PREFIX + "driver.file.directory";

    /** Root directory for the state store. */
    private String rootDirectory;


    @Override
    protected boolean exists(String path) {
        File test = new File(path);
        return test.exists();
    }

    @Override
    protected boolean mkdir(String path) {
        File dir = new File(path);
        return dir.mkdirs();
    }

    @Override
    protected boolean rename(String src, String dst) {
        try {
            Files.move(new File(src), new File(dst));
            return true;
        } catch (IOException e) {
            LOG.error("Cannot rename {} to {}", src, dst, e);
            return false;
        }
    }

    @Override
    protected boolean remove(String path) {
        File file = new File(path);
        return file.delete();
    }

    @Override
    protected String getRootDir() {
        if (this.rootDirectory == null) {
            String dir = getConf().get(FEDERATION_STORE_FILE_DIRECTORY);
            if (dir == null) {
                File tempDirBase =
                        new File(System.getProperty("java.io.tmpdir"));
                File tempDir = null;
                try {
                    tempDir = java.nio.file.Files.createTempDirectory(
                            tempDirBase.toPath(), System.currentTimeMillis() + "-").toFile();
                } catch (IOException e) {
                    // fallback to the base upon exception.
                    LOG.debug("Unable to create a temporary directory. Fall back to " +
                            " the default system temp directory {}", tempDirBase, e);
                    tempDir = tempDirBase;
                }
                dir = tempDir.getAbsolutePath();
                LOG.warn("The root directory is not available, using {}", dir);
            }
            this.rootDirectory = dir;
        }
        return this.rootDirectory;
    }

    @Override
    protected <T extends BaseRecord> BufferedReader getReader(String filename) {
        BufferedReader reader = null;
        try {
            LOG.debug("Loading file: {}", filename);
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
        } catch (Exception ex) {
            LOG.error("Cannot open read stream for record {}", filename, ex);
        }
        return reader;
    }

    @Override
    protected <T extends BaseRecord> BufferedWriter getWriter(String filename) {
        BufferedWriter writer = null;
        try {
            LOG.debug("Writing file: {}", filename);
            File file = new File(filename);
            FileOutputStream fos = new FileOutputStream(file, false);
            OutputStreamWriter osw =
                    new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer = new BufferedWriter(osw);
        } catch (IOException e) {
            LOG.error("Cannot open write stream for record {}", filename, e);
        }
        return writer;
    }

    @Override
    public void close() throws Exception {
        setInitialized(false);
    }

    @Override
    protected List<String> getChildren(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (ArrayUtils.isNotEmpty(files)) {
            List<String> ret = new ArrayList<>(files.length);
            for (File file : files) {
                String filename = file.getName();
                ret.add(filename);
            }
            return ret;
        }
        return Collections.emptyList();
    }
}