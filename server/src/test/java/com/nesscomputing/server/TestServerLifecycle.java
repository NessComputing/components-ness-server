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

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nesscomputing.config.Config;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;
import com.nesscomputing.testing.lessio.AllowNetworkListen;

@AllowNetworkListen(ports= {0})
@AllowNetworkAccess(endpoints= {"127.0.0.1:0"})
public class TestServerLifecycle
{
    private StandaloneServer server = null;

    @Before
    public void setUp()
    {
        Assert.assertNull(server);
        server = new StandaloneServer() {
            @Override
            public Config getConfig() {
                return Config.getEmptyConfig();
            }

            @Override
            protected Module getMainModule(final Config config) {
                return new Module() {
                    @Override
                    public void configure(final Binder binder) {
                    }
                };
            }

            @Override
            protected String getServerType()
            {
                return "test";
            }
        };
    }

    @After
    public void tearDown()
    {
        Assert.assertNotNull(server);

        if (!server.isStarted()) {
            server.startServer();
        }

        if (!server.isStopped()) {
            server.stopServer();
        }

        server = null;
    }


    @Test
    public void testLifecycle() throws Exception
    {
        Assert.assertFalse(server.isStarted());
        Assert.assertFalse(server.isStopped());

        server.startServer();

        Assert.assertTrue(server.isStarted());
        Assert.assertFalse(server.isStopped());

        server.stopServer();

        Assert.assertTrue(server.isStarted());
        Assert.assertTrue(server.isStopped());
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStart() throws Exception
    {
        Assert.assertFalse(server.isStarted());
        Assert.assertFalse(server.isStopped());

        server.startServer();

        Assert.assertTrue(server.isStarted());
        Assert.assertFalse(server.isStopped());

        server.startServer();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStop() throws Exception
    {
        Assert.assertFalse(server.isStarted());
        Assert.assertFalse(server.isStopped());

        server.startServer();

        Assert.assertTrue(server.isStarted());
        Assert.assertFalse(server.isStopped());

        server.stopServer();

        Assert.assertTrue(server.isStarted());
        Assert.assertTrue(server.isStopped());

        server.stopServer();
    }

}
