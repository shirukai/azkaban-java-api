package com.azkaban.response;

import com.alibaba.fastjson.JSONObject;

public class Schedule {
    private String scheduleId;
    private String cronExpression;
    private String submitUser;
    private String firstSchedTime;
    private String nextExecTime;
    private String period;
    private JSONObject executionOptions;

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getSubmitUser() {
        return submitUser;
    }

    public void setSubmitUser(String submitUser) {
        this.submitUser = submitUser;
    }

    public String getFirstSchedTime() {
        return firstSchedTime;
    }

    public void setFirstSchedTime(String firstSchedTime) {
        this.firstSchedTime = firstSchedTime;
    }

    public String getNextExecTime() {
        return nextExecTime;
    }

    public void setNextExecTime(String nextExecTime) {
        this.nextExecTime = nextExecTime;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public JSONObject getExecutionOptions() {
        return executionOptions;
    }

    public void setExecutionOptions(JSONObject executionOptions) {
        this.executionOptions = executionOptions;
    }
}
