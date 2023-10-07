package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.service.ServiceStateException;
import org.apache.hadoop.thirdparty.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

// 定期执行的 Service，有一个 scheduler线程池
public abstract class PeriodicService extends AbstractService {

    private static final Logger LOG = LoggerFactory.getLogger(PeriodicService.class);

    private static final long DEFAULT_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);


    private long intervalMs;
    private final String serviceName;

    // scheduler线程池
    private final ScheduledExecutorService scheduler;

    // scheduler线程池是否在 Running
    private volatile boolean isRunning = false;

    private long runCount;
    private long errorCount;
    // 上一次成功执行的时间
    private long lastRun;

    public PeriodicService(String name) {
        this(name, DEFAULT_INTERVAL_MS);
    }

    public PeriodicService(String name, long interval) {
        super(name);
        this.serviceName = name;
        this.intervalMs = interval;

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(this.getName() + "-%d")
                .build();
        this.scheduler = Executors.newScheduledThreadPool(1, threadFactory);
    }

    protected void setIntervalMs(long interval) {
        if (getServiceState() == STATE.STARTED) {
            throw new ServiceStateException("Periodic service already started");
        } else {
            this.intervalMs = interval;
        }
    }

    protected long getIntervalMs() {
        return this.intervalMs;
    }

    protected long getErrorCount() {
        return this.errorCount;
    }

    protected long getRunCount() {
        return this.runCount;
    }

    protected long getLastUpdate() {
        return this.lastRun;
    }

    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
        LOG.info("Starting periodic service {}", this.serviceName);
        startPeriodic();
    }

    @Override
    protected void serviceStop() throws Exception {
        stopPeriodic();
        LOG.info("Stopping periodic service {}", this.serviceName);
        super.serviceStop();
    }

    protected synchronized void stopPeriodic() {
        if (this.isRunning) {
            LOG.info("{} is shutting down", this.serviceName);
            this.isRunning = false;
            this.scheduler.shutdownNow();
        }
    }

    protected synchronized void startPeriodic() {
        stopPeriodic();

        Runnable updateRunnable = () -> {
            LOG.debug("Running {} update task", serviceName);
            try {
                if (!isRunning) {
                    return;
                }
                periodicInvoke();
                runCount++;
                lastRun = Time.now();
            } catch (Exception ex) {
                errorCount++;
                LOG.warn(serviceName + " service threw an exception", ex);
            }
        };

        this.isRunning = true;
        this.scheduler.scheduleWithFixedDelay(updateRunnable, 0, this.intervalMs, TimeUnit.MILLISECONDS);
    }

    // 定期性执行的方法
    protected abstract void periodicInvoke();
}
