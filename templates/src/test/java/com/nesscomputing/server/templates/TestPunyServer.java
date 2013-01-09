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

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nesscomputing.config.Config;
import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.httpclient.HttpClient;
import com.nesscomputing.httpclient.guice.HttpClientModule;
import com.nesscomputing.httpclient.response.JsonContentConverter;
import com.nesscomputing.lifecycle.junit.LifecycleRule;
import com.nesscomputing.lifecycle.junit.LifecycleRunner;
import com.nesscomputing.lifecycle.junit.LifecycleStatement;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;
import com.nesscomputing.testing.lessio.AllowNetworkListen;

@AllowNetworkAccess(endpoints={"127.0.0.1:*"})
@AllowNetworkListen(ports={0})
@RunWith(LifecycleRunner.class)
public class TestPunyServer
{
    @LifecycleRule
    public final LifecycleStatement lifecycleRule = LifecycleStatement.serviceDiscoveryLifecycle();

    @Inject
    public HttpClient httpClient;

    @Before
    public void setUp()
    {
        final Injector inj = Guice.createInjector(Stage.PRODUCTION,
                                                  new Module() {
                                                      @Override
                                                      public void configure(final Binder binder) {
                                                          binder.disableCircularProxies();
                                                          binder.requireExplicitBindings();
                                                      }
                                                  },
                                                  ConfigModule.forTesting(),
                                                  new HttpClientModule(),
                                                  lifecycleRule.getLifecycleModule());

        inj.injectMembers(this);

    }


    @Test
    public void testPunyServer() throws Exception
    {
        final PunyServer punyServer = new PunyServer() {
            @Override
            public Config getConfig()
            {
                return Config.getFixedConfig("galaxy.internal.port.http", "0");
            }
        };

        punyServer.startServer();
        Assert.assertTrue(punyServer.isStarted());

        final String baseUrl = String.format("http://localhost:%d/puny", punyServer.getPort());

        final Map<String, Object> result = httpClient.get(baseUrl, JsonContentConverter.getResponseHandler(new TypeReference<Map<String, Object>>() {}, new ObjectMapper())).perform();

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        final Object data = result.get("puny");
        Assert.assertEquals("result", data);

        punyServer.stopServer();
        Assert.assertTrue(punyServer.isStopped());
    }
}
