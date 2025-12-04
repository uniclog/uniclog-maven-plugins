package io.github.uniclog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.uniclog.execution.ExecutionConfig;
import io.github.uniclog.execution.PluginConfig;
import io.github.uniclog.utils.JmAbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;

@Mojo(name = "configuration", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class PluginConfigMojo extends JmAbstractMojo {
    @Parameter(alias = "config")
    private String pluginConfigPath;
    @Component
    private MavenProject project;

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path configPath = project.getBasedir().toPath().resolve(pluginConfigPath);
        PluginConfig pluginConfig = loadAndValidate(configPath);

        Map<String, Function<ExecutionConfig, JmAbstractMojo>> fab = Map.of(
                "modify", action -> new ModifyJsonMojo(action.getJsonIn(), action.getJsonOut(), action.getExecutions()),
                "insert", action -> new InsertJsonMojo(action.getJsonIn(), action.getJsonOut(), action.getExecutions()),
                "remove", action -> new RemoveJsonMojo(action.getJsonIn(), action.getJsonOut(), action.getExecutions()),
                "regex", action -> new RegExModifyMojo(action.getJsonIn(), action.getJsonOut(), action.getExecutions())
        );

        for (ExecutionConfig action : pluginConfig.getConfiguration()) {
            var mojo = fab.get(action.getGoal()).apply(action);
            if (mojo == null) {
                throw new MojoExecutionException("Unexpected value: " + action.getGoal());
            }
            mojo.execute();
        }
    }

    private PluginConfig loadAndValidate(Path yamlPath) throws MojoExecutionException {
        Object yamlObj;
        try (InputStream in = Files.newInputStream(yamlPath)) {
            yamlObj = yamlMapper.readValue(in, Object.class);
        } catch (IOException ex) {
            throw new MojoExecutionException("Cannot read YAML: " + yamlPath, ex);
        }

        validateObjectAgainstSchema(yamlObj);

        try {
            String json = yamlMapper.writeValueAsString(yamlObj);
            return yamlMapper.readValue(json, PluginConfig.class);
        } catch (Exception ex) {
            throw new MojoExecutionException("Cannot convert YAML to PluginConfig", ex);
        }
    }

    private void validateObjectAgainstSchema(Object obj) throws MojoExecutionException {
        try {
            String jsonStr = mapper.writeValueAsString(obj);
            JSONObject jsonToValidate = new JSONObject(jsonStr);
            try (InputStream schemaStream = getClass().getResourceAsStream("/schema/config-schema.json")) {
                if (schemaStream == null) {
                    throw new MojoExecutionException("Resource not found: /schema/config-schema.json");
                }
                JSONObject rawSchema = new JSONObject(new JSONTokener(schemaStream));
                Schema schema = SchemaLoader.load(rawSchema);
                schema.validate(jsonToValidate);
            }
        } catch (FileNotFoundException ex) {
            String err = format("FileNotFoundException : %s", ex.getMessage());
            getLogger().error(err, ex);
            throw new MojoExecutionException(err, ex);
        } catch (ValidationException | IOException ex) {
            String err = format("ValidationException : %s", ex.getMessage());
            getLogger().error(err, ex);
            throw new MojoExecutionException(err, ex);
        }
    }
}
