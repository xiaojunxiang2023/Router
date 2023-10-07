import org.apache.hadoop.hdfs.server.federation.router.DFSRouter;
import org.junit.Test;

/**
 * @Author: Learner src
 * @Date: 2023/09/25日  14:37分
 * @Description:
 */
public class _5_DFSRouter_Start {
    @Test
    public void startDFSRouter() throws Exception {
        String[] args = new String[]{};
        DFSRouter.main(args);
        Thread.sleep(System.currentTimeMillis());
    }
}
