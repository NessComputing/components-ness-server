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
package com.nesscomputing.server.templates;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.junit.Test;

import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.galaxy.GalaxyConfigModule;
import com.nesscomputing.httpserver.HttpServerModule;
import com.nesscomputing.jackson.NessJacksonModule;
import com.nesscomputing.lifecycle.guice.LifecycleModule;
import com.nesscomputing.scopes.threaddelegate.ThreadDelegatedScopeModule;
import com.nesscomputing.tracking.guice.TrackingModule;

public class TestBasicDiscoveryServerModule
{
    @Test
    public void testSimple()
    {
        final Config config = Config.getEmptyConfig();

        Guice.createInjector(Stage.PRODUCTION,
                             getPlumbing(config),
                             new LifecycleModule(),
                             new ConfigModule(config),
                             new BasicDiscoveryServerModule(config));
    }

    private Module getPlumbing(final Config config)
    {
        return new AbstractModule() {
            @Override
            public void configure()
            {
                // Copied from DiscoveryStandaloneServer to avoid circular dep.
                install(new GalaxyConfigModule());
                install(new HttpServerModule(config));
                install(new NessJacksonModule());
                install(new ThreadDelegatedScopeModule());
                install(new TrackingModule());
            }
        };
    }
}
