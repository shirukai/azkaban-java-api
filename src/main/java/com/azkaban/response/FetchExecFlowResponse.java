package com.azkaban.response;

import java.util.List;

/**
 * Created by shirukai on 2019-06-04 09:24
 * 查询执行Flow信息
 */
public class FetchExecFlowResponse extends BaseResponse {
    private String id;
    private String project;
    private String projectId;
    private String flow;
    private String flowId;
    private String execid;
    private String nestedId;
    private String type;
    private Integer attempt;
    private String submitUser;
    private String status;
    private Long submitTime;
    private Long updateTime;
    private Long startTime;
    private Long endTime;
    private List<Node> nodes;

    public String getId() {
        return id;
    }

    public FetchExecFlowResponse setId(String id) {
        this.id = id;
        return this;
    }

    public String getProject() {
        return project;
    }

    public FetchExecFlowResponse setProject(String project) {
        this.project = project;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public FetchExecFlowResponse setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getFlow() {
        return flow;
    }

    public FetchExecFlowResponse setFlow(String flow) {
        this.flow = flow;
        return this;
    }

    public String getFlowId() {
        return flowId;
    }

    public FetchExecFlowResponse setFlowId(String flowId) {
        this.flowId = flowId;
        return this;
    }

    public String getExecid() {
        return execid;
    }

    public FetchExecFlowResponse setExecid(String execid) {
        this.execid = execid;
        return this;
    }

    public String getNestedId() {
        return nestedId;
    }

    public FetchExecFlowResponse setNestedId(String nestedId) {
        this.nestedId = nestedId;
        return this;
    }

    public String getType() {
        return type;
    }

    public FetchExecFlowResponse setType(String type) {
        this.type = type;
        return this;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public FetchExecFlowResponse setAttempt(Integer attempt) {
        this.attempt = attempt;
        return this;
    }

    public String getSubmitUser() {
        return submitUser;
    }

    public FetchExecFlowResponse setSubmitUser(String submitUser) {
        this.submitUser = submitUser;
        return this;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Long submitTime) {
        this.submitTime = submitTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

}

