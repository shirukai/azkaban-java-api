package com.azkaban.response;

import java.util.List;

/**
 * Created by shirukai on 2019-06-03 12:08
 * 查询FLow执行记录的响应
 */
public class FetchFlowExecutionsResponse extends BaseResponse {
    private String project;
    private String projectId;
    private String flow;
    private Integer from;
    private Integer length;
    private Integer total;
    private List<Execution> executions;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<Execution> executions) {
        this.executions = executions;
    }

}

