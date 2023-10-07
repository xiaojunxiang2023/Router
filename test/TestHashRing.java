import org.apache.hadoop.hdfs.server.federation.utils.ConsistentHashRing;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: hellosrc
 * @Date: 2023/10/05日  16:10分
 * @Description:
 */
public class TestHashRing {
    @Test
    public void test01() {
        Set<String> locationSet = new HashSet<>();
        locationSet.add("1.txt");
        locationSet.add("2.txt");
        locationSet.add("3.txt");
        locationSet.add("4.txt");
        locationSet.add("5.txt");
        ConsistentHashRing hashRing = new ConsistentHashRing(locationSet);
        System.out.println(hashRing.getLocation("1.txt"));
        System.out.println(hashRing.getLocation("1.txt"));
        System.out.println("===========");
        hashRing.addLocation("6.txt");
        hashRing.addLocation("7.txt");
        hashRing.addLocation("8.txt");
        System.out.println(hashRing.getLocation("1.txt"));
        System.out.println("===========");
        System.out.println(hashRing.getLocation("1.txt/3"));
    }
}
