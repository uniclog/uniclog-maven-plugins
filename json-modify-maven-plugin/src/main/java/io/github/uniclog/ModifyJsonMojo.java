package io.github.uniclog;

import com.jayway.jsonpath.DocumentContext;
import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.utils.ExecuteConsumer;
import io.github.uniclog.utils.JmAbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static io.github.uniclog.execution.DocumentType.JSON;
import static io.github.uniclog.utils.DataUtils.getElement;
import static java.lang.String.format;

@Mojo(name = "modify", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ModifyJsonMojo extends JmAbstractMojo {
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    public ModifyJsonMojo() {
    }

    public ModifyJsonMojo(String jsonInputPath, String jsonOutputPath, List<ExecutionMojo> executions) {
        this.jsonInputPath = jsonInputPath;
        this.jsonOutputPath = jsonOutputPath;
        this.executions = executions;
    }

    @Override
    public String getJsonInputPath() {
        return jsonInputPath;
    }

    @Override
    public String getJsonOutputPath() {
        return jsonOutputPath;
    }

    @Override
    public List<ExecutionMojo> getExecutions() {
        return executions;
    }

    @Override
    public void execute() throws MojoExecutionException {
        ExecuteConsumer<Object, ExecutionMojo, Integer> executeConsumer = (object, ex, exIndex) -> {
            DocumentContext json = (DocumentContext) object;
            Object value = getElement(ex.getType(), ex.getValue(), ex.getValueFile());
            var old = json.read(ex.getToken());
            json.set(ex.getToken(), value);
            info(format("(%d) md: %s: %s -> %s", exIndex, ex.getToken(), old, value));
        };

        executeAction(executeConsumer, JSON);
    }
}
