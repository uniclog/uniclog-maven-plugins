package io.github.uniclog.utils;

import io.github.uniclog.execution.ExecutionMojo;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

public abstract class JmAbstractMojo extends AbstractMojo implements UtilsInterface {
    @Override
    public String getJsonInputPath() {
        return "";
    }

    @Override
    public String getJsonOutputPath() {
        return "";
    }

    @Override
    public List<ExecutionMojo> getExecutions() {
        return List.of();
    }

    @Override
    public Log getLogger() {
        return getLog();
    }
}
