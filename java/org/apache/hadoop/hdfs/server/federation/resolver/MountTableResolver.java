package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.federation.resolver.order.DestinationOrder;
import org.apache.hadoop.hdfs.server.federation.router.Router;
import org.apache.hadoop.hdfs.server.federation.store.MountTableStore;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreCache;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreService;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreUnavailableException;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetMountTableEntriesRequest;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetMountTableEntriesResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;
import org.apache.hadoop.hdfs.tools.federation.RouterAdmin;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.thirdparty.com.google.common.cache.Cache;
import org.apache.hadoop.thirdparty.com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.hadoop.hdfs.DFSUtil.isParentEntry;
import static org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys.*;

// 解析器，从 path到 destination
public class MountTableResolver
        implements FileSubclusterResolver, StateStoreCache {

    private static final Logger LOG = LoggerFactory.getLogger(MountTableResolver.class);

    private final Router router;
    private final StateStoreService stateStore;
    private MountTableStore mountTableStore;

    private boolean init = false;
    private boolean disabled = false;

    // 从 path到 destination（MountTable）
    // 挂载表 tree, path路径字典序
    private final TreeMap<String, MountTable> tree = new TreeMap<>();

    // 从 path到 destination（PathLocation）  PathLocation = src + MountTable
    private final Cache<String, PathLocation> locationCache;

    private String defaultNameService = "";
    private boolean defaultNSEnable = true;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();


    @VisibleForTesting
    public MountTableResolver(Configuration conf) {
        this(conf, (StateStoreService) null);
    }

    public MountTableResolver(Configuration conf, Router routerService) {
        this(conf, routerService, null);
    }

    public MountTableResolver(Configuration conf, StateStoreService store) {
        this(conf, null, store);
    }

    public MountTableResolver(Configuration conf, Router routerService, StateStoreService store) {
        this.router = routerService;
        if (store != null) {
            this.stateStore = store;
        } else if (this.router != null) {
            this.stateStore = this.router.getStateStore();
        } else {
            this.stateStore = null;
        }

        boolean mountTableCacheEnable = conf.getBoolean(FEDERATION_MOUNT_TABLE_CACHE_ENABLE, FEDERATION_MOUNT_TABLE_CACHE_ENABLE_DEFAULT);
        if (mountTableCacheEnable) {
            int maxCacheSize = conf.getInt(FEDERATION_MOUNT_TABLE_MAX_CACHE_SIZE, FEDERATION_MOUNT_TABLE_MAX_CACHE_SIZE_DEFAULT);
            this.locationCache = CacheBuilder.newBuilder()
                    .maximumSize(maxCacheSize)
                    .build();
        } else {
            this.locationCache = null;
        }

        // 订阅一个外部缓存，以至于 StateStoreService定期为自己更新缓存
        registerCacheExternal();
        initDefaultNameService(conf);
    }

    private void registerCacheExternal() {
        if (this.stateStore != null) {
            this.stateStore.registerCacheExternal(this);
        }
    }

    private void initDefaultNameService(Configuration conf) {
        this.defaultNSEnable = conf.getBoolean(DFS_ROUTER_DEFAULT_NAMESERVICE_ENABLE, DFS_ROUTER_DEFAULT_NAMESERVICE_ENABLE_DEFAULT);

        if (!this.defaultNSEnable) {
            LOG.warn("Default name service is disabled.");
            return;
        }
        this.defaultNameService = conf.get(DFS_ROUTER_DEFAULT_NAMESERVICE, "");

        if (this.defaultNameService.equals("")) {
            this.defaultNSEnable = false;
            LOG.warn("Default name service is not set.");
        } else {
            LOG.info("Default name service: {}, enabled to read or write",
                    this.defaultNameService);
        }
    }

    protected Router getRouter() {
        return this.router;
    }

    protected MountTableStore getMountTableStore() throws IOException {
        if (this.mountTableStore == null) {
            this.mountTableStore = this.stateStore.getRegisteredRecordStore(MountTableStore.class);
            if (this.mountTableStore == null) {
                throw new IOException("State Store does not have an interface for " + MountTableStore.class);
            }
        }
        return this.mountTableStore;
    }

    // 添加一个挂载点
    public void addEntry(final MountTable entry) {
        writeLock.lock();
        try {
            String srcPath = entry.getSourcePath();
            // 往 tree里添加数据
            this.tree.put(srcPath, entry);
            // 删除 Location缓存
            invalidateLocationCache(srcPath);
        } finally {
            writeLock.unlock();
        }
        this.init = true;
    }

    // 删除一个挂载点
    public void removeEntry(final String srcPath) {
        writeLock.lock();
        try {
            // 从 tree里删除数据
            this.tree.remove(srcPath);
            // 删除 Location缓存
            invalidateLocationCache(srcPath);
        } finally {
            writeLock.unlock();
        }
    }

    // 对 map的 key进行遍历，删除 path路径下(自己 以及子目录文件)的所有缓存
    private void invalidateLocationCache(final String path) {
        LOG.debug("Invalidating {} from {}", path, locationCache);

        ConcurrentMap<String, PathLocation> map = locationCache.asMap();
        Set<Entry<String, PathLocation>> entries = map.entrySet();
        Iterator<Entry<String, PathLocation>> it = entries.iterator();
        while (it.hasNext()) {
            Entry<String, PathLocation> entry = it.next();
            String key = entry.getKey();
            PathLocation loc = entry.getValue();
            String src = loc.getSourcePath();
            if (src != null) {
                if (isParentEntry(key, path)) {
                    LOG.debug("Removing {}", src);
                    it.remove();
                }
            } else {
                String dest = loc.getDefaultLocation().getDest();
                if (dest.startsWith(path)) {
                    LOG.debug("Removing default cache {}", dest);
                    it.remove();
                }
            }
        }
        LOG.debug("Location cache after invalidation: {}", locationCache);
    }


    @Override
    public boolean loadCache(boolean force) {
        try {
            // 先更新 MountTableStore的缓存
            MountTableStore mountTable = this.getMountTableStore();
            mountTable.loadCache(force);

            GetMountTableEntriesRequest request = GetMountTableEntriesRequest.newInstance("/");
            GetMountTableEntriesResponse response = mountTable.getMountTableEntries(request);
            List<MountTable> records = response.getEntries();
            // 删除 Location缓存，以及 tree
            refreshEntries(records);
        } catch (IOException e) {
            LOG.error("Cannot fetch mount table entries from State Store", e);
            return false;
        }
        return true;
    }

    @VisibleForTesting
    public void refreshEntries(final Collection<MountTable> entries) {
        writeLock.lock();
        try {
            // New entries
            Map<String, MountTable> newEntries = new ConcurrentHashMap<>();
            for (MountTable entry : entries) {
                String srcPath = entry.getSourcePath();
                newEntries.put(srcPath, entry);
            }

            // 即旧的获取 tree.values()
            Set<String> oldEntries = new TreeSet<>(Collections.reverseOrder());
            for (MountTable entry : getTreeValues("/")) {
                String srcPath = entry.getSourcePath();
                oldEntries.add(srcPath);
            }

            // 删除节点
            for (String srcPath : oldEntries) {
                if (!newEntries.containsKey(srcPath)) {
                    // 从 tree中删除
                    this.tree.remove(srcPath);
                    // 删除 Location缓存
                    invalidateLocationCache(srcPath);
                    LOG.info("Removed stale mount point {} from resolver", srcPath);
                }
            }

            // upsert节点
            for (MountTable entry : entries) {
                String srcPath = entry.getSourcePath();
                if (!oldEntries.contains(srcPath)) {
                    // Add node, it does not exist
                    this.tree.put(srcPath, entry);
                    invalidateLocationCache(srcPath);
                    LOG.info("Added new mount point {} to resolver", srcPath);
                } else {
                    // Node exists, check for updates
                    MountTable existingEntry = this.tree.get(srcPath);
                    if (existingEntry != null && !existingEntry.equals(entry)) {
                        LOG.info("Entry has changed from \"{}\" to \"{}\"",
                                existingEntry, entry);
                        this.tree.put(srcPath, entry);
                        invalidateLocationCache(srcPath);
                        LOG.info("Updated mount point {} in resolver", srcPath);
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
        this.init = true;
    }

    // 清空 tree 和 Location缓存
    public void clear() {
        LOG.info("Clearing all mount location caches");
        writeLock.lock();
        try {
            if (this.locationCache != null) {
                this.locationCache.invalidateAll();
            }
            this.tree.clear();
        } finally {
            writeLock.unlock();
        }
    }

    // 从 Location缓存 中获得 destinations [PathLocation]
    @Override
    public PathLocation getDestinationForPath(final String path)
            throws IOException {
        verifyMountTable();
        readLock.lock();
        try {
            if (this.locationCache == null) {
                return lookupLocation(path);
            }
            Callable<? extends PathLocation> meh = new Callable<PathLocation>() {
                @Override
                public PathLocation call() throws Exception {
                    return lookupLocation(path);
                }
            };
            return this.locationCache.get(path, meh);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            final IOException ioe;
            if (cause instanceof IOException) {
                ioe = (IOException) cause;
            } else {
                ioe = new IOException(cause);
            }
            throw ioe;
        } finally {
            readLock.unlock();
        }
    }

    // 把 path包装成 PathLocation
    public PathLocation lookupLocation(final String str) throws IOException {
        PathLocation ret;
        final String path = RouterAdmin.normalizeFileSystemPath(str);
        MountTable entry = findDeepest(path);
        if (entry != null) {
            ret = buildLocation(path, entry);
        } else {
            if (!defaultNSEnable) {
                throw new RouterResolveException("Cannot find locations for " + path
                        + ", because the default nameservice is disabled to read or write");
            }
            RemoteLocation remoteLocation = new RemoteLocation(defaultNameService, path, path);
            List<RemoteLocation> locations =
                    Collections.singletonList(remoteLocation);
            ret = new PathLocation(null, locations);
        }
        return ret;
    }


    // 把 path和 MountTable封装成一个 PathLocation
    private PathLocation buildLocation(final String path, final MountTable entry) throws IOException {
        String srcPath = entry.getSourcePath();
        if (!path.startsWith(srcPath)) {
            LOG.error("Cannot build location, {} not a child of {}", path, srcPath);
            return null;
        }

        List<RemoteLocation> dests = entry.getDestinations();
        if (getClass() == MountTableResolver.class && dests.size() > 1) {
            throw new IOException("Cannnot build location, "
                    + getClass().getSimpleName()
                    + " should not resolve multiple destinations for " + path);
        }

        String remainingPath = path.substring(srcPath.length());
        if (remainingPath.startsWith(Path.SEPARATOR)) {
            remainingPath = remainingPath.substring(1);
        }

        List<RemoteLocation> locations = new LinkedList<>();
        for (RemoteLocation oneDst : dests) {
            String nsId = oneDst.getNameserviceId();
            String newPath = oneDst.getDest();
            if (!newPath.endsWith(Path.SEPARATOR) && !remainingPath.isEmpty()) {
                newPath += Path.SEPARATOR;
            }
            newPath += remainingPath;
            RemoteLocation remoteLocation = new RemoteLocation(nsId, newPath, path);
            locations.add(remoteLocation);
        }
        DestinationOrder order = entry.getDestOrder();
        return new PathLocation(srcPath, locations, order);
    }


    // 得到 path对应的挂载点
    public MountTable getMountPoint(final String path) throws IOException {
        verifyMountTable();
        return findDeepest(RouterAdmin.normalizeFileSystemPath(path));
    }

    // 获得 path孩子 的挂载点s
    @Override
    public List<String> getMountPoints(final String str) throws IOException {
        verifyMountTable();
        final String path = RouterAdmin.normalizeFileSystemPath(str);

        Set<String> children = new TreeSet<>();
        readLock.lock();
        try {
            String to = path + Character.MAX_VALUE;
            SortedMap<String, MountTable> subMap = this.tree.subMap(path, to);

            boolean exists = false;
            for (String subPath : subMap.keySet()) {
                String child = subPath;

                if (!path.equals(Path.SEPARATOR)) {
                    int ini = path.length();
                    child = subPath.substring(ini);
                }

                if (child.isEmpty()) {
                    exists = true;
                } else if (child.startsWith(Path.SEPARATOR)) {
                    exists = true;
                    child = child.substring(1);
                    int fin = child.indexOf(Path.SEPARATOR);
                    if (fin > -1) {
                        child = child.substring(0, fin);
                    }
                    if (!child.isEmpty()) {
                        children.add(child);
                    }
                }
            }
            if (!exists) {
                return null;
            }
            return new LinkedList<>(children);
        } finally {
            readLock.unlock();
        }
    }

    // 即 tree.values()
    public List<MountTable> getMounts(final String path) throws IOException {
        verifyMountTable();
        return getTreeValues(RouterAdmin.normalizeFileSystemPath(path));
    }

    private void verifyMountTable() throws StateStoreUnavailableException {
        if (!this.init || disabled) {
            throw new StateStoreUnavailableException("Mount Table not initialized");
        }
    }

    @Override
    public String toString() {
        readLock.lock();
        try {
            return this.tree.toString();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getDefaultNamespace() {
        return this.defaultNameService;
    }

    // 核心方法，寻找挂载点，如果找不到自己那就 看看有没有记录父路径
    private MountTable findDeepest(final String path) {
        readLock.lock();
        try {
            // 返回小于等于自己的 key（path是字符串，即字典序）
            Entry<String, MountTable> entry = this.tree.floorEntry(path);
            // 父路径
            while (entry != null && !isParentEntry(path, entry.getKey())) {
                // 寻找与自己血缘关系最近的
                entry = this.tree.lowerEntry(entry.getKey());
            }
            if (entry == null) {
                return null;
            }
            return entry.getValue();
        } finally {
            readLock.unlock();
        }
    }

    // 获取这个路径对应的所有挂载点
    private List<MountTable> getTreeValues(final String path) {
        LinkedList<MountTable> ret = new LinkedList<>();
        readLock.lock();
        try {
            String to = path + Character.MAX_VALUE;
            SortedMap<String, MountTable> subMap = this.tree.subMap(path, to);
            ret.addAll(subMap.values());
        } finally {
            readLock.unlock();
        }
        return ret;
    }

    protected long getCacheSize() throws IOException {
        if (this.locationCache != null) {
            return this.locationCache.size();
        }
        throw new IOException("localCache is null");
    }

    @VisibleForTesting
    public String getDefaultNameService() {
        return defaultNameService;
    }

    @VisibleForTesting
    public void setDefaultNameService(String defaultNameService) {
        this.defaultNameService = defaultNameService;
    }

    @VisibleForTesting
    public boolean isDefaultNSEnable() {
        return defaultNSEnable;
    }

    @VisibleForTesting
    public void setDefaultNSEnable(boolean defaultNSRWEnable) {
        this.defaultNSEnable = defaultNSRWEnable;
    }

    @VisibleForTesting
    public void setDisabled(boolean disable) {
        this.disabled = disable;
    }
}
