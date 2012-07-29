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

import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.ServiceDiscoveryLifecycle;
import com.nesscomputing.lifecycle.guice.LifecycleModule;

import com.google.inject.Module;

/**
 * Standalone server that uses the ServiceDiscovery Lifecycle. No additional
 * announcements etc. are done.
 */
public abstract class AnnouncingStandaloneServer extends StandaloneServer
{
    @Override
    protected LifecycleStage getStartStage()
    {
        return LifecycleStage.ANNOUNCE_STAGE;
    }

    @Override
    protected Module getLifecycleModule()
    {
        return new LifecycleModule(ServiceDiscoveryLifecycle.class);
    }
}
