package com.azkaban.response;

/**
 * Created by shirukai on 2019-06-03 11:27
 * 执行flow响应
 */
public class ExecuteFlowResponse extends BaseResponse {
    private String project;
    private String flow;
    private String execid;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getExecid() {
        return execid;
    }

    public void setExecid(String execid) {
        this.execid = execid;
    }

}
