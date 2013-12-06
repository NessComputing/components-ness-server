package com.nesscomputing.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.OnStage;
import com.nesscomputing.logging.Log;

@Singleton
class JvmPauseAlarm implements Runnable
{
    private static final Log LOG = Log.findLog();
    private static final long S_THRESHOLD = 1000;

    private final JvmPauseAlarmConfig config;

    @Inject
    JvmPauseAlarm(JvmPauseAlarmConfig config)
    {
        this.config = config;
    }

    @OnStage(LifecycleStage.START)
    public void start()
    {
        if (!config.isPauseAlarmEnabled()) {
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("jvm-pause-alarm").build());
        executor.submit(this);
        executor.shutdown();
    }

    @Override
    public void run()
    {
        try {
            safeRun();
        } catch (Exception e) {
            LOG.error(e, "Exiting due to exception");
            throw Throwables.propagate(e);
        }
    }

    private void safeRun()
    {
        final long sleepTime = config.getCheckTime().getMillis();
        final long alarmTime = config.getPauseAlarmTime().getMillis();

        LOG.info("Watching JVM for GC pausing.  Checking every %s for pauses of at least %s.",
                config.getCheckTime(), config.getPauseAlarmTime());

        long lastUpdate = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.info("Exiting due to interrupt");
                return;
            }

            final long now = System.currentTimeMillis();
            final long pauseMs = now - lastUpdate;

            if (pauseMs > alarmTime) {
                LOG.warn("Detected pause of %s!", pauseMs > S_THRESHOLD ? String.format("%.1fs", pauseMs / 1000.0) : pauseMs + "ms");
            }

            lastUpdate = now;
        }
    }
}
