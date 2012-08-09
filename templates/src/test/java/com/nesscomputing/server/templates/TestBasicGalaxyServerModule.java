package com.nesscomputing.server.templates;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Stage;
import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.lifecycle.guice.LifecycleModule;

public class TestBasicGalaxyServerModule
{
    @Test
    public void testSimple()
    {
        final Config config = Config.getEmptyConfig();

        Guice.createInjector(Stage.PRODUCTION,
                             new LifecycleModule(),
                             new ConfigModule(config),
                             new BasicGalaxyServerModule(config));
    }
}
