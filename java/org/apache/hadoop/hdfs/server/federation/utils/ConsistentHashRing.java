package org.apache.hadoop.hdfs.server.federation.utils;

import org.apache.hadoop.io.MD5Hash;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// 哈希环，一种负载均衡算法，一致性哈希
// 但是感觉, 仅仅使用到了取 hash值的随机性而已, 并没有环的思想
public class ConsistentHashRing {
    private static final String SEPARATOR = "/";
    private static final String VIRTUAL_NODE_FORMAT = "%s" + SEPARATOR + "%d";


    // 虚拟节点初始位置参考 -> 想要添加这样虚拟节点的数量(默认100)
    private final Map<String, Integer> entryToVirtualNodes = new HashMap<>();

    // str = location/i
    // hash(str) -> str
    private final SortedMap<String, String> ring = new TreeMap<>();
    
    
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public ConsistentHashRing(Set<String> locations) {
        for (String location : locations) {
            addLocation(location);
        }
    }

    public void addLocation(String location) {
        addLocation(location, 100);
    }
    
    public void addLocation(String location, int numVirtualNodes) {
        writeLock.lock();
        try {
            entryToVirtualNodes.put(location, numVirtualNodes);
            for (int i = 0; i < numVirtualNodes; i++) {
                String key = String.format(VIRTUAL_NODE_FORMAT, location, i);
                String hash = getHash(key);
                ring.put(hash, key);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeLocation(String location) {
        writeLock.lock();
        try {
            Integer numVirtualNodes = entryToVirtualNodes.remove(location);
            for (int i = 0; i < numVirtualNodes; i++) {
                String key = String.format(VIRTUAL_NODE_FORMAT, location, i);
                String hash = getHash(key);
                ring.remove(hash);
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    // 获得下一个该使用的节点位置
    // 例如传参 3.txt，结果获得了 5.txt
    // 传参 3.txt/索引 始终返回 3.txt
    public String getLocation(String item) {
        readLock.lock();
        try {
            if (ring.isEmpty()) {
                return null;
            }
            String hash = getHash(item);
            if (!ring.containsKey(hash)) {
                // tailMap 即只取大于等于形参值 的节点
                SortedMap<String, String> tailMap = ring.tailMap(hash);
                hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
            }
            String virtualNode = ring.get(hash);
            int index = virtualNode.lastIndexOf(SEPARATOR);
            if (index >= 0) {
                return virtualNode.substring(0, index);
            } else {
                // 这里根本进不来
                return virtualNode;
            }
        } finally {
            readLock.unlock();
        }
    }

    public String getHash(String key) {
        return MD5Hash.digest(key).toString();
    }

    public Set<String> getLocations() {
        return entryToVirtualNodes.keySet();
    }
}
