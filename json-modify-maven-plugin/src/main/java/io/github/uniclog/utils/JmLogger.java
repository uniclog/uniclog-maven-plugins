package io.github.uniclog.utils;

import org.apache.maven.plugin.logging.Log;

public interface JmLogger {
    Log getLogger();

    default void info(String log) {
        getLogger().info(log);
    }

    default void debug(String log) {
        getLogger().debug(log);
    }

    default void error(String log, Throwable ex) {
        getLogger().error(log, ex);
    }
}
