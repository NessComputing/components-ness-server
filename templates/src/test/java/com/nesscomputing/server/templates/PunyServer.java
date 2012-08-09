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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.nesscomputing.config.Config;
import com.nesscomputing.httpserver.HttpServer;
import com.nesscomputing.server.StandaloneServer;

/**
 * This is the smallest REST server implementation that actually serves data.
 */
public class PunyServer extends StandaloneServer
{
    @Inject
    private HttpServer httpServer;

    @Override
    public Module getMainModule(final Config config)
    {
        return new PunyModule(config);
    }

    public static class PunyModule extends AbstractModule
    {
        private final Config config;

        PunyModule(final Config config)
        {
            this.config = config;
        }

        @Override
        public void configure()
        {
            install (new BasicGalaxyServerModule(config));

            bind(PunyResource.class);
        }
    }

    @VisibleForTesting
    int getPort()
    {
        return httpServer.getConnectors().get("internal-http").getPort();
    }

    @Path("/puny")
    public static class PunyResource
    {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response getPunyData()
        {
            return Response.ok(ImmutableMap.of("puny", "result")).build();
        }
    }
}
