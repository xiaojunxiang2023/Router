import org.apache.hadoop.hdfs.tools.federation.RouterAdmin;
import org.junit.Test;

/**
 * @Author: Learner src
 * @Date: 2023/09/25日  14:37分
 * @Description:
 */
public class _6_RouterAdmin_Start {
    @Test
    public void startRouterAdmin() throws Exception {
        String[] args = new String[]{"-saaafemode"};
        RouterAdmin.main(args);
        Thread.sleep(System.currentTimeMillis());
    }
}
