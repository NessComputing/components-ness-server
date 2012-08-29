package com.nesscomputing.server.info;

import org.apache.log4j.MDC;
import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.server.info.ServerInfo;

public class TestServerInfo
{
    @Test
    public void testEmpty()
    {
        Assert.assertNull(ServerInfo.get("foo"));
        Assert.assertNull(MDC.get("foo"));
    }

    @Test
    public void testValue()
    {
        ServerInfo.add("hello", "world");
        Assert.assertEquals("world", ServerInfo.get("hello"));
        Assert.assertEquals("world", MDC.get("hello").toString());
    }

    @Test
    public void testValueChange()
    {
        ServerInfo.add("hello", "world");
        Assert.assertEquals("world", ServerInfo.get("hello"));
        Assert.assertEquals("world", MDC.get("hello").toString());

        ServerInfo.add("hello", "moon");
        Assert.assertEquals("moon", ServerInfo.get("hello"));
        Assert.assertEquals("moon", MDC.get("hello").toString());
    }

    @Test
    public void testNullValue()
    {
        ServerInfo.add("hello", null);
        Assert.assertEquals(null, ServerInfo.get("hello"));
        Assert.assertEquals(null, MDC.get("hello"));
    }

    @Test
    public void testProviderValue()
    {
        final DataProvider p = new DataProvider();
        ServerInfo.add("hello", p);

        p.setValue("xxx");

        Assert.assertEquals(p, ServerInfo.get("hello"));
        Assert.assertEquals("xxx", MDC.get("hello").toString());

        p.setValue("yyy");

        Assert.assertEquals(p, ServerInfo.get("hello"));
        Assert.assertEquals("yyy", MDC.get("hello").toString());
    }

    @Test
    public void testClear()
    {
        ServerInfo.add("hello", "world");
        Assert.assertEquals("world", ServerInfo.get("hello"));
        Assert.assertEquals("world", MDC.get("hello").toString());

        ServerInfo.clear();

        Assert.assertNull(ServerInfo.get("hello"));
        Assert.assertNull(MDC.get("hello"));
    }

    @Test
    public void testRemove()
    {
        ServerInfo.add("hello", "world");
        Assert.assertEquals("world", ServerInfo.get("hello"));
        Assert.assertEquals("world", MDC.get("hello").toString());

        ServerInfo.remove("hello");

        Assert.assertNull(ServerInfo.get("hello"));
        Assert.assertNull(MDC.get("hello"));
    }

    @Test(expected=NullPointerException.class)
    public void testNullAdd()
    {
        ServerInfo.add(null, "foo");
    }

    @Test(expected=NullPointerException.class)
    public void testNullRemove()
    {
        ServerInfo.remove(null);
    }

    @Test(expected=NullPointerException.class)
    public void testNullGet()
    {
        ServerInfo.get(null);
    }

    public static class DataProvider
    {
        private String value = "";

        public DataProvider()
        {
        }

        public void setValue(final String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return value;
        }
    }
}

