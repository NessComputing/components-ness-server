package com.nesscomputing.server;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.TimeSpan;

interface JvmPauseAlarmConfig
{
    /**
     * Turn the pause alarm on or off at config time.
     */
    @Config("ness.jvm-pause.enabled")
    @Default("true")
    boolean isPauseAlarmEnabled();

    /**
     * The pause alarm will check this often to see if the JVM was taking a nap.
     * This time should be significantly smaller than the pause time, or you
     * may regret it...
     */
    @Config("ness.jvm-pause.check-time")
    @Default("50ms")
    TimeSpan getCheckTime();

    /**
     * Report pauses that last longer than this amount.
     */
    @Config("ness.jvm-pause.pause-time")
    @Default("200ms")
    TimeSpan getPauseAlarmTime();
}
