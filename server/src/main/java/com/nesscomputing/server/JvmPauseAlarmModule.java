package com.nesscomputing.server;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import com.nesscomputing.config.ConfigProvider;

class JvmPauseAlarmModule extends AbstractModule
{
    @Override
    public void configure()
    {
        bind (JvmPauseAlarm.class).asEagerSingleton();
        bind (JvmPauseAlarmConfig.class).toProvider(ConfigProvider.of(JvmPauseAlarmConfig.class)).in(Scopes.SINGLETON);
    }

    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj.getClass() == getClass();
    }
}
