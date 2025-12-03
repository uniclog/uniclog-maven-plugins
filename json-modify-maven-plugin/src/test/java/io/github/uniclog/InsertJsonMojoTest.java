package io.github.uniclog;

import io.github.uniclog.execution.ExecutionMojo;
import io.github.uniclog.utils.UtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.github.uniclog.execution.ExecutionType.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InsertJsonMojoTest implements TestUtils {
    private InsertJsonMojo service;

    @Override
    public UtilsInterface getService() {
        return service;
    }

    @BeforeEach
    public void setUp() {
        service = spy(new InsertJsonMojo());
        doReturn(outputPath).when(service).getJsonOutputPath();
        doReturn(inputPath).when(service).getJsonInputPath();
    }

/*
    /// @todo вставка если json такой [ "" ]
    @Test
    void test() throws Exception {
        doReturn("src/test/resources/testInsert_in_2.json").when(service).getJsonInputPath();
        ExecutionMojo execution = getSpyExecutionMojo("$", null, "value1",
                STRING, null, false, null);
        doReturn(List.of(execution)).when(service).getExecutions();
        assertDoesNotThrow(() -> service.execute());
    }*/

    @MethodSource({
            "insertValueKeyField", "insertNewArrayItem",
            "insertNewArrayItemToIndex", "notInsertNewArrayItem_notValid_or_skipIfNotFound"})
    @ParameterizedTest
    void testInsertJsonMojo(ExecutionMojo execution, String validation, String token) {
        doReturn(List.of(execution)).when(service).getExecutions();
        assertDoesNotThrow(() -> service.execute());
        assertDoesNotThrow(() -> validation(execution, validation, token));
    }

    @Test
    void testInsertFindPath() {
        ExecutionMojo execution = getSpyExecutionMojo(
                "$.resource[?(@.title == \"text\")].content.*",
                null, "{\"a1\": \"a2\"}", JSON, null, false, null);
        doReturn(List.of(execution)).when(service).getExecutions();
        assertDoesNotThrow(() -> service.execute());
        assertDoesNotThrow(() -> validation(execution,
                "[\"a2\"]",
                "$.resource[?(@.title == \"text\")].content[?(@.a1 == \"a2\")].a1"));
    }

    private Stream<Arguments> insertValueKeyField() {
        return Stream.of(
                of(getSpyExecutionMojo("@", "key1", "value1",
                                STRING, null, false, null),
                        "\"value1\"", "@.key1"),
                of(getSpyExecutionMojo("$.jo", "key2", "value2",
                                STRING, null, false, null),
                        "\"value2\"", "$.jo.key2"),
                of(getSpyExecutionMojo("$.l1[2]", "key3", "value3",
                                STRING, null, false, null),
                        "\"value3\"", "$.l1[2].key3"),
                of(getSpyExecutionMojo("$.none", null, "value4",
                                STRING, "none", true, null),
                        null, null)
        );
    }

    private Stream<Arguments> insertNewArrayItem() {
        return Stream.of(
                of(getSpyExecutionMojo("$.l1", null, "{\"a1\": \"a2\"}",
                                JSON, null, false, null),
                        "{\"a1\":\"a2\"}", "$.l1.[3]"),
                of(getSpyExecutionMojo("$.l1", null, "value1",
                                STRING, null, false, null),
                        "\"value1\"", "$.l1.[3]"),
                of(getSpyExecutionMojo("$.l1", null, null,
                                NULL, null, false, null),
                        null, "$.l1.[5]"),
                of(getSpyExecutionMojo("$.l1[1]", "i1", "test4",
                                STRING, "{\"i1\":\"test2\"}", false, null),
                        "{\"i1\":\"test4\"}", "$.l1.[1]")
        );
    }

    private Stream<Arguments> notInsertNewArrayItem_notValid_or_skipIfNotFound() {
        return Stream.of(
                of(getSpyExecutionMojo("$.l1[0]", "i1", "test4",
                                STRING, "{\"i1\":\"none\"}", true, null)
                        , "{\"i1\":\"test1\"}", "$.l1.[0]"),
                of(getSpyExecutionMojo("$.jsonArrNon", null, "value1",
                                STRING, null, true, null)
                        , "{\"i1\":\"test1\"}", "$.l1.[0]")
        );
    }

    private Stream<Arguments> insertNewArrayItemToIndex() {
        return Stream.of(
                of(getSpyExecutionMojo("$.l1", null, "{\"l2\": \"test5\"}",
                                JSON, null, false, 1),
                        "{\"l2\":\"test5\"}", "$.l1.[1]"),
                of(getSpyExecutionMojo("$.jsonArr", null, "newValue",
                                STRING, null, false, 1),
                        "\"text\"", "$.jsonArr.[2]"));
    }
}