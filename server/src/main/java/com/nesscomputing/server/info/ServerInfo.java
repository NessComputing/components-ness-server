package com.nesscomputing.server.info;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.apache.log4j.MDC;

import com.nesscomputing.logging.Log;


/**
 * ServerInfo contains information about the running server such as the type of binary,
 * the binary version, what service type the service itself exposes etc.
 *
 * This implementation shadow-copies the data into the MDC to expose it to third party places
 * in the service such as logging.
 */
public final class ServerInfo
{
    private static final Log LOG = Log.findLog();

    private static final Object NULL_VALUE = new Object();

    private static final ServerInfo SERVER_INFO = new ServerInfo();

    public static final String SERVER_BINARY = "server-binary";
    public static final String SERVER_VERSION = "server-version";
    public static final String SERVER_TYPE = "server-type";
    public static final String SERVER_SERVICE = "server-service";

    // Keys in the jar manifest
    private static final String NESS_BINARY_KEY = "X-Ness-Binary";
    private static final String NESS_VERSION_KEY = "X-Ness-Version";
    private static final String NESS_TYPE_KEY = "X-Ness-Type";

    private final Map<String, Object> info = new ConcurrentHashMap<String, Object>();

    public static void add(@Nonnull final String key, @Nullable final Object value)
    {
        SERVER_INFO.register(key, value);
    }

    public static void remove(@Nonnull final String key)
    {
        SERVER_INFO.unregister(key);
    }

    public static Object get(@Nonnull final String key)
    {
        return SERVER_INFO.retrieve(key);
    }

    public static void clear()
    {
        SERVER_INFO.clean();
    }

    public static void registerServerInfo(@Nonnull final Class<?> serverClazz)
    {
        final String className = serverClazz.getName();

        try {
            final Enumeration<URL> manifests = serverClazz.getClassLoader().getResources("META-INF/MANIFEST.MF");

            while (manifests.hasMoreElements()) {
                try {
                    final URL url = manifests.nextElement();
                    final Manifest manifest = new Manifest(url.openStream());
                    final Attributes classAttributes = manifest.getAttributes(className);
                    if (classAttributes != null) {
                        add(ServerInfo.SERVER_BINARY, classAttributes.getValue(NESS_BINARY_KEY));
                        add(ServerInfo.SERVER_VERSION, classAttributes.getValue(NESS_VERSION_KEY));
                        add(ServerInfo.SERVER_TYPE, classAttributes.getValue(NESS_TYPE_KEY));
                        LOG.debug("Found manifest, set values to binary=%s, version=%s, type=%s", get(SERVER_BINARY), get(SERVER_VERSION), get(SERVER_TYPE));
                        return;
                    }
                }
                catch (IOException ioe) {
                    LOG.warnDebug(ioe, "While loading manifest");
                }
            }
            LOG.info("Could not locate manifest information for server, looked for %s.", className);
        }
        catch (IOException ioe) {
            LOG.warnDebug(ioe, "Manifest information is unavailable!");
        }
    }

    private ServerInfo()
    {
    }

    private void register(final String key, final Object value)
    {
        Preconditions.checkNotNull(key != null, "The key must not be null!");

        // If the value put in, remove any fetcher that might exist. This keeps the
        // semantics that a null object will return null when calling MDC.get(key).
        if (value != null) {
            info.put(key, value);
            MDC.put(key, new Object() {
                @Override
                public String toString() {
                    return String.valueOf(get(key));
                }
            });
        }
        else {
            info.put(key, NULL_VALUE);
            MDC.remove(key);
        }
    }

    private void unregister(final String key)
    {
        Preconditions.checkNotNull(key != null, "The key must not be null!");

        MDC.remove(key);
        info.remove(key);
    }

    private Object retrieve(final String key)
    {
        Preconditions.checkNotNull(key != null, "The key must not be null!");

        final Object o = info.get(key);
        return (o == NULL_VALUE) ? null : o;
    }

    private void clean()
    {
        for (Iterator<String> it = info.keySet().iterator(); it.hasNext(); ) {
            final String key = it.next();
            MDC.remove(key);
            it.remove();
        }
    }

    @Override
    public String toString()
    {
        return info.toString();
    }
}
