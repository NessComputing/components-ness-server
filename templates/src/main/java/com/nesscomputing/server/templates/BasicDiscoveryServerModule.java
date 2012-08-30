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
import com.sun.jersey.guice.JerseyServletModule;
import com.yammer.metrics.guice.InstrumentationModule;

import com.nesscomputing.config.Config;
import com.nesscomputing.httpserver.selftest.SelftestModule;
import com.nesscomputing.jdbi.argument.ArgumentFactoryModule;
import com.nesscomputing.jdbi.metrics.DatabaseMetricsModule;
import com.nesscomputing.jersey.NessJerseyBinder;
import com.nesscomputing.jersey.NessJerseyServletModule;
import com.nesscomputing.jersey.exceptions.NessJerseyExceptionMapperModule;
import com.nesscomputing.jersey.filter.BodySizeLimitResourceFilterFactory;
import com.nesscomputing.jersey.json.NessJacksonJsonProvider;
import com.nesscomputing.serverinfo.ServerInfoModule;

/**
 * Defines a basic server suitable for serving REST resources using JSON over HTTP when using the Discovery service.
 *
 * <ul>
 *   <li>Yammer metrics</li>
 *   <li>JDBI database configuration</li>
 *   <li>Jersey with exception handling</li>
 *   <li>selftest endpoint</li>
 * </ul>
 */
public class BasicDiscoveryServerModule extends AbstractModule
{
    private final Config config;
    private final String[] paths;

    public BasicDiscoveryServerModule(final Config config)
    {
        this(config, "/*");
    }

    public BasicDiscoveryServerModule(final Config config, final String... paths)
    {
        this.config = config;
        this.paths = paths;
    }

    @Override
    protected void configure()
    {
        install(new InstrumentationModule());

        install(new DatabaseMetricsModule());
        install(new ArgumentFactoryModule());

        install(new JerseyServletModule());
        install(new NessJerseyServletModule(config, paths));
        install (new NessJerseyExceptionMapperModule());

        NessJerseyBinder.bindResourceFilterFactory(binder()).to(BodySizeLimitResourceFilterFactory.class);

        bind (NessJacksonJsonProvider.class);

        install(new SelftestModule());
        install(new ServerInfoModule());
    }
}
