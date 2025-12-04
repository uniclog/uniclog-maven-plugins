package io.github.uniclog;

import com.jayway.jsonpath.DocumentContext;
import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.utils.ExecuteConsumer;
import io.github.uniclog.utils.UtilsInterface;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

import static io.github.uniclog.execution.DocumentType.JSON;
import static java.lang.String.format;

@Mojo(name = "remove", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class RemoveJsonMojo extends AbstractMojo implements UtilsInterface {
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    public RemoveJsonMojo() {
    }

    public RemoveJsonMojo(String jsonInputPath, String jsonOutputPath, List<ExecutionMojo> executions) {
        this.jsonInputPath = jsonInputPath;
        this.jsonOutputPath = jsonOutputPath;
        this.executions = executions;
    }

    @Override
    public void execute() throws MojoExecutionException {
        ExecuteConsumer<Object, ExecutionMojo, Integer> executeConsumer = (object, ex, exIndex) -> {
            DocumentContext json = (DocumentContext) object;
            json.delete(ex.getToken());
            info(format("(%d) rm: %s", exIndex, ex.getToken()));
        };

        executeAction(executeConsumer, JSON);
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
    public Log getLogger() {
        return getLog();
    }
}
