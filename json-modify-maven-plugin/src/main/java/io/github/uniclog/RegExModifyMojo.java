package io.github.uniclog;

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

import static io.github.uniclog.execution.DocumentType.DOCUMENT;
import static java.lang.String.format;

@Mojo(name = "regex", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class RegExModifyMojo extends AbstractMojo implements UtilsInterface {
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    // outValidationType - валидация измененного файла по типу ? json, xml, html мбб

    public RegExModifyMojo() {
    }

    public RegExModifyMojo(String jsonInputPath, String jsonOutputPath, List<ExecutionMojo> executions) {
        this.jsonInputPath = jsonInputPath;
        this.jsonOutputPath = jsonOutputPath;
        this.executions = executions;
    }

    @Override
    public void execute() throws MojoExecutionException {
        ExecuteConsumer<Object, ExecutionMojo, Integer> executeConsumer = (object, ex, exIndex) -> {
            StringBuilder document = (StringBuilder) object;
            String replaced = document.toString().replaceAll(ex.getToken(), ex.getValue());
            document.replace(0, document.length(), replaced);
            info(format("(%d) mr: %s", exIndex, ex.getToken()));
        };

        executeAction(executeConsumer, DOCUMENT);
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
