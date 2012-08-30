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

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.apache.commons.lang3.time.StopWatch;

import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.jmx.JmxModule;
import com.nesscomputing.jmx.starter.guice.JmxStarterModule;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.log.jmx.guice.JmxLoggingModule;
import com.nesscomputing.log4j.ConfigureStandaloneLogging;
import com.nesscomputing.logging.AssimilateForeignLogging;
import com.nesscomputing.logging.Log;
import com.nesscomputing.server.info.ServerInfo;

/**
 * Standalone main class.
 *
 * Environment properties:
 *
 * <ul>
 *  <li>ness.config.location - An URI to load configuration from</li>
 *  <li>ness.config - A configuration path to load from the config URI</li>
 *  <li>log4j.configuration - An URI to load the logging configuration from.</li>
 * </ul>
 *
 * This installs a basic set of Guice modules that every server should use:
 * <ul>
 *  <li>configuration</li>
 *  <li>lifecycle</li>
 *  <li>log control over jmx</li>
 *  <li>jmx export</li>
 *  <li>jmx starter</li>
 * </ul>
 */
public abstract class StandaloneServer
{
    private static final Log LOG = Log.findLog();

    private final Thread shutdownThread = new Thread("Server Shutdown Thread")
    {
        @Override
        public void run() {
            LOG.info("Shutting Service down");
            doStopServer(true);
        }
    };

    @Inject
    private Lifecycle lifecycle;

    private boolean started = false;
    private boolean stopped = false;

    public StandaloneServer()
    {
        // Hook up logging.
        ConfigureStandaloneLogging.configure(getServerType());

        // Suck java.util.logging into log4j
        AssimilateForeignLogging.assimilate();
    }

    /**
     * Returns the main guice module for the server.
     */
    protected abstract Module getMainModule(final Config config);

    /**
     * Returns the server type. Must be set so that the server info contains
     * the right server type.
     */
    protected abstract String getServerType();

    public void startServer()
    {
        Preconditions.checkState(!started, "Server was already started, double-start denied!");

        ServerInfo.add(ServerInfo.SERVER_SERVICE, getServerType());

        final Object binaryVersion = ServerInfo.get(ServerInfo.SERVER_BINARY);

        if (binaryVersion != null) {
            LOG.info("Service startup begins, version: %s-%s (server type: %s), running in %s mode.", binaryVersion,
                                                                                 ServerInfo.get(ServerInfo.SERVER_VERSION),
                                                                                 ServerInfo.get(ServerInfo.SERVER_SERVICE),
                                                                                 ServerInfo.get(ServerInfo.SERVER_TYPE));
        }
        else {
            LOG.info("Service startup begins (server type: %s)", ServerInfo.get(ServerInfo.SERVER_SERVICE));
        }

        final StopWatch timer = new StopWatch();
        timer.start();

        final Injector injector = getInjector();

        injector.injectMembers(this);

        timer.stop();
        final long injectorTime = timer.getTime();
        timer.reset();

        Preconditions.checkNotNull(lifecycle, "No Lifecycle Object was injected!");

        Runtime.getRuntime().addShutdownHook(shutdownThread);

        LOG.info("Starting Service");
        timer.start();
        lifecycle.executeTo(getStartStage());
        timer.stop();

        started = true;
        LOG.info("Service startup completed; %d ms in module initialization and %d ms to start lifecycle.", injectorTime, timer.getTime());
    }

    public void stopServer()
    {
        doStopServer(false);
    }

    private final void doStopServer(boolean fromHook) {
        Preconditions.checkState(!stopped, "Server was already stopped, double-stop denied!");

        Preconditions.checkNotNull(lifecycle, "No Lifecycle Object was injected!");

        LOG.info("Stopping Service");
        lifecycle.executeTo(getStopStage());
        if (!fromHook) {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        }

        stopped = true;
    }

    public boolean isStarted()
    {
        return started;
    }

    public boolean isStopped()
    {
        return stopped;
    }

    /**
     * Can be overridden in tests.
     */
    public Module getPlumbingModules(final Config config)
    {
        return new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.install(new ConfigModule(config));
                binder.install(getLifecycleModule());

                binder.install(new JmxModule());
                binder.install(new JmxStarterModule(config));
                binder.install(new JmxLoggingModule(getServerType()));
            }
        };
    }

    /**
     * Can be overridden in tests.
     */
    public Config getConfig()
    {
        return Config.getConfig();
    }

    public final Injector getInjector()
    {
        final Config config = getConfig();

        // Initialize Guice off the main module. Add a tiny
        // bit of special sauce to ensure explicit bindings.

        final Injector injector = Guice.createInjector(
            Stage.PRODUCTION,
            getPlumbingModules(config),
            getMainModule(config),

            new Module() {
                @Override
                public void configure(final Binder binder) {
                    binder.requireExplicitBindings();
                    binder.disableCircularProxies();
                }
            });

        return injector;
    }

    protected LifecycleStage getStartStage()
    {
        return LifecycleStage.START_STAGE;
    }

    protected LifecycleStage getStopStage()
    {
        return LifecycleStage.STOP_STAGE;
    }

    protected Module getLifecycleModule()
    {
        return new LifecycleModule();
    }

    protected String getServerToken()
    {
        return UUID.randomUUID().toString();
    }
}
