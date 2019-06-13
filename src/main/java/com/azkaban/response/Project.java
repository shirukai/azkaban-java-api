package com.azkaban.response;

import com.alibaba.fastjson.JSONArray;

public class Project {
    private String projectId;
    private String projectName;
    private String createdBy;
    private Long createdTime;
    private JSONArray userPermissions;
    private JSONArray groupPermissions;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public JSONArray getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(JSONArray userPermissions) {
        this.userPermissions = userPermissions;
    }

    public JSONArray getGroupPermissions() {
        return groupPermissions;
    }

    public void setGroupPermissions(JSONArray groupPermissions) {
        this.groupPermissions = groupPermissions;
    }
}
