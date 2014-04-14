/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mogwee.executors.LoggingExecutor;

import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.OnStage;
import com.nesscomputing.logging.Log;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

@Singleton
class JvmPauseAlarm implements Runnable
{
    private static final Log LOG = Log.findLog();

    private static final PeriodFormatter printFormat = new PeriodFormatterBuilder().printZeroAlways()
                    .appendSeconds().appendSuffix(" second", "seconds")
                    .appendSeparator(" and ").printZeroRarelyLast()
                    .appendMillis().appendSuffix(" millisecond", " milliseconds").toFormatter();

    private final ExecutorService executor = new LoggingExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(),
        new ThreadFactoryBuilder().setNameFormat("jvm-pause-alarm").setDaemon(true).build());


    private final JvmPauseAlarmConfig config;

    private final long sleepTime;
    private final long alarmTime;

    private volatile boolean running = true;

    @Inject
    JvmPauseAlarm(JvmPauseAlarmConfig config)
    {
        this.config = checkNotNull(config, "config is null");

        this.sleepTime = config.getCheckTime().getMillis();
        this.alarmTime = config.getPauseAlarmTime().getMillis();
    }

    @OnStage(LifecycleStage.START)
    public void start()
    {
        if (!config.isPauseAlarmEnabled()) {
            return;
        }

        executor.submit(this);
    }

    @OnStage(LifecycleStage.STOP)
    public void stop()
    {
        this.running = false;
        executor.shutdown();
    }

    @Override
    public void run()
    {
        LOG.info("Watching JVM for GC pausing.  Checking every %s for pauses of at least %s.", config.getCheckTime(), config.getPauseAlarmTime());

        do {
            final long startNanos = System.nanoTime();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }

            final long pauseMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

            if (pauseMs > alarmTime) {
                final Period gcPeriod = new Period(pauseMs);
                LOG.warn("Detected pause of %s!", printFormat.print(gcPeriod));
            }
        } while(running);
    }
}
