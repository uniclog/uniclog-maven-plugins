package io.github.uniclog.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

public class FileUtils {
    private final static Configuration configuration = Configuration.builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static DocumentContext readJsonObject(String jsonInputPath) throws MojoExecutionException {
        if (isNull(jsonInputPath)) {
            throw new MojoExecutionException("Parameter 'json.in' can't be null.");
        }

        try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(jsonInputPath))) {
            return JsonPath.using(configuration).parse(in, UTF_8.name());
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read file " + jsonInputPath, e);
        }
    }

    public static String readObject(String inputPath) throws MojoExecutionException {
        if (isNull(inputPath)) {
            throw new MojoExecutionException("Parameter 'json.in' can't be null.");
        }
        try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(inputPath))) {
            byte[] buffer = new byte[in.available()];
            int bytesRead = in.read(buffer);
            return new String(buffer, 0, bytesRead, UTF_8);
        } catch (NoSuchFileException e) {
            String baseDir = Paths.get("").toAbsolutePath().toString();
            try (InputStream in = Files.newInputStream(FileSystems.getDefault().getPath(baseDir + inputPath))) {
                byte[] buffer = new byte[in.available()];
                int bytesRead = in.read(buffer);
                return new String(buffer, 0, bytesRead, UTF_8);
            } catch (IOException ex) {
                throw new MojoExecutionException("Unable to read file " + inputPath, e);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read file " + inputPath, e);
        }
    }

    public static DocumentContext writeJsonObject(DocumentContext json, String jsonOutputPath) throws MojoExecutionException {
        try {
            ObjectWriter writer = new ObjectMapper().writer(new CustomPrettyPrinter());
            writer.writeValue(Paths.get(jsonOutputPath).toFile(), json.json());
            return json;
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write file " + jsonOutputPath, e);
        }
    }

    public static <T> T writeObject(T object, String jsonOutputPath) throws MojoExecutionException {
        try (var writer = new FileWriter(jsonOutputPath)) {
            writer.write(object.toString());
            return object;
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to write file " + jsonOutputPath, e);
        }
    }
}
