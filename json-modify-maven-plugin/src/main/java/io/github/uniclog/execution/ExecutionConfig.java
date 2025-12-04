package io.github.uniclog.execution;

import java.util.List;

public class ExecutionConfig {
    private String id;
    private String jsonIn;
    private String jsonOut;
    private String phase;
    private String goal;
    private List<ExecutionMojo> executions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonIn() {
        return jsonIn;
    }

    public void setJsonIn(String jsonIn) {
        this.jsonIn = jsonIn;
    }

    public String getJsonOut() {
        return jsonOut;
    }

    public void setJsonOut(String jsonOut) {
        this.jsonOut = jsonOut;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public List<ExecutionMojo> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecutionMojo> executions) {
        this.executions = executions;
    }
}
