package com.azkaban.response;

import java.util.List;

public class Node {
    private String id;
    private String nestedId;
    private String type;
    private Integer attempt;
    private String status;
    private Long updateTime;
    private Long startTime;
    private Long endTime;
    private List<String> in;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNestedId() {
        return nestedId;
    }

    public void setNestedId(String nestedId) {
        this.nestedId = nestedId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<String> getIn() {
        return in;
    }

    public void setIn(List<String> in) {
        this.in = in;
    }
}
