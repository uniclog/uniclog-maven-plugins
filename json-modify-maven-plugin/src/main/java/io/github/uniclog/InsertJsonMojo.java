package io.github.uniclog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.execution.ExecutionType;
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
import static io.github.uniclog.utils.DataUtils.getElement;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Mojo(name = "insert", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class InsertJsonMojo extends AbstractMojo implements UtilsInterface {
    @Parameter(alias = "json.in")
    private String jsonInputPath;
    @Parameter(alias = "json.out")
    private String jsonOutputPath;
    @Parameter(alias = "executions", required = true)
    private List<ExecutionMojo> executions;

    public InsertJsonMojo() {
    }

    public InsertJsonMojo(String jsonInputPath, String jsonOutputPath, List<ExecutionMojo> executions) {
        this.jsonInputPath = jsonInputPath;
        this.jsonOutputPath = jsonOutputPath;
        this.executions = executions;
    }

    @Override
    public void execute() throws MojoExecutionException {
        ExecuteConsumer<Object, ExecutionMojo, Integer> executeConsumer = (object, ex, exIndex) -> {
            DocumentContext json = (DocumentContext) object;

            Object value = getElement(ex.getType(), ex.getValue(), ex.getValueFile());

            JsonPath pathToArray = JsonPath.compile(ex.getToken());
            debug("pathToArray=" + pathToArray.getPath());

            if (nonNull(ex.getKey())) {
                json.put(ex.getToken(), ex.getKey(), value);
            } else {
                ArrayNode array = json.read(ex.getToken());
                ArrayNode outArrayNode = new ArrayNode(new JsonNodeFactory(true));
                for (int index = 0; index < array.size(); index++) {
                    if (nonNull(ex.getArrayIndex()) && ex.getArrayIndex() == index) {
                        addElement(ex, outArrayNode, value);
                    }
                    outArrayNode.add(array.get(index));
                }
                if (isNull(ex.getArrayIndex())) {
                    addElement(ex, outArrayNode, value);
                }
                var insertToken = ex.getToken().replaceAll("\\.\\*$", "");
                json.set(insertToken, outArrayNode);
            }
            info(String.format("(%d) ad: %s | %s | %s", exIndex, ex.getToken(), ex.getKey(), ex.getValue()));
        };

        executeAction(executeConsumer, JSON);
    }

    private void addElement(ExecutionMojo ex, ArrayNode outArrayNode, Object value) {
        if (ex.getType().equals(ExecutionType.JSON)) {
            outArrayNode.add((JsonNode) value);
        } else if (ex.getType().equals(ExecutionType.STRING)) {
            outArrayNode.add((String) value);
        } else if (ex.getType().equals(ExecutionType.INTEGER)) {
            outArrayNode.add((Integer) value);
        } else if (ex.getType().equals(ExecutionType.DOUBLE)) {
            outArrayNode.add((Double) value);
        } else if (ex.getType().equals(ExecutionType.BOOLEAN)) {
            outArrayNode.add((Boolean) value);
        } else if (ex.getType().equals(ExecutionType.NULL)) {
            outArrayNode.add((JsonNode) null);
        }
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
