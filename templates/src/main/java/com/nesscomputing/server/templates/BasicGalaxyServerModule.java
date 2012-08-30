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

import com.nesscomputing.config.Config;
import com.nesscomputing.galaxy.GalaxyConfigModule;
import com.nesscomputing.httpserver.HttpServerModule;
import com.nesscomputing.jackson.NessJacksonModule;
import com.nesscomputing.scopes.threaddelegate.ThreadDelegatedScopeModule;
import com.nesscomputing.tracking.guice.TrackingModule;

/**
 * Defines a basic server suitable for serving REST resources using JSON over HTTP.
 *
 * <ul>
 *   <li>Galaxy support</li>
 *   <li>Http Server</li>
 *   <li>Yammer metrics</li>
 *   <li>JDBI database configuration</li>
 *   <li>Jackson</li>
 *   <li>Jersey with exception handling</li>
 *   <li>selftest endpoint</li>
 * </ul>
 */
public class BasicGalaxyServerModule extends AbstractModule
{
    private final Config config;
    private final String[] paths;

    public BasicGalaxyServerModule(final Config config)
    {
        this(config, "/*");
    }

    public BasicGalaxyServerModule(final Config config, final String... paths)
    {
        this.config = config;
        this.paths = paths;
    }

    @Override
    protected void configure()
    {
        install(new GalaxyConfigModule());
        install(new HttpServerModule(config));

        install (new NessJacksonModule());

        install(new ThreadDelegatedScopeModule());
        install(new TrackingModule());

        install(new BasicDiscoveryServerModule(config, paths));
    }
}
