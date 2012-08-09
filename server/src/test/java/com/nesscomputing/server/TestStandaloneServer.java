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

import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.config.Config;
import com.nesscomputing.server.AnnouncingStandaloneServer;
import com.nesscomputing.server.StandaloneServer;
import com.nesscomputing.testing.lessio.AllowNetworkAccess;
import com.nesscomputing.testing.lessio.AllowNetworkListen;


@AllowNetworkListen(ports= {0})
@AllowNetworkAccess(endpoints= {"127.0.0.1:0"})
public class TestStandaloneServer
{
    @Test
    public void testBasic()
    {
        final StandaloneServer server = DemoServer.getServer();
        Assert.assertNull(server);
    }

    @Test
    public void testSpinup() throws Exception
    {
        final StandaloneServer server = new DemoServer(Config.getEmptyConfig());
        server.startServer();

        try {
            Assert.assertNotNull(server);
            Assert.assertTrue(server.isStarted());
            Assert.assertFalse(server.isStopped());
        }
        finally {
            server.stopServer();
            Assert.assertTrue(server.isStarted());
            Assert.assertTrue(server.isStopped());
        }
    }

    @Test
    public void testAnnouncingBasic()
    {
        final AnnouncingStandaloneServer server = AnnouncingDemoServer.getServer();
        Assert.assertNull(server);
    }

    @Test
    public void testAnnouncingSpinup() throws Exception
    {
        final AnnouncingStandaloneServer server = new AnnouncingDemoServer(Config.getEmptyConfig());
        server.startServer();
        try {
            Assert.assertNotNull(server);
            Assert.assertTrue(server.isStarted());
            Assert.assertFalse(server.isStopped());
        }
        finally {
            server.stopServer();
            Assert.assertTrue(server.isStarted());
            Assert.assertTrue(server.isStopped());
        }
    }
}
