package com.azkaban.api;

import com.azkaban.response.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Created by shirukai on 2019-05-31 16:46
 * Azkaban 操作相关API实现类
 */
public class AzkabanApiImpl implements AzkabanApi {
    private String username;
    private String password;
    private String uri;
    private String sessionId = "b1d4f665-f4b9-4e7d-b83a-b928b41cc323";
    private static final String DELETE_PROJECT = "{0}/manager?delete=true&project={1}&session.id={2}";
    private static final String FETCH_PROJECT_FLOWS = "{0}/manager?ajax=fetchprojectflows&session.id={1}&project={2}";
    private static final String EXECUTE_FLOW = "{0}/executor?ajax=executeFlow&session.id={1}&project={2}&flow={3}";
    private static final String CANCEL_FLOW = "{0}/executor?ajax=cancelFlow&session.id={1}&execid={2}";
    private static final String FETCH_EXEC_FLOW = "{0}/executor?ajax=fetchexecflow&session.id={1}&execid={2}";
    private static final String FETCH_EXEC_JOB_LOGS = "{0}/executor?ajax=fetchExecJobLogs&session.id={1}&execid={2}" +
            "&jobId={3}&offset={4}&length={5}";
    private static final String FETCH_FLOW_EXECUTIONS = "{0}/manager?ajax=fetchFlowExecutions&session.id={1}" +
            "&project={2}&flow={3}&start={4}&length={5}";
    private static final String FETCH_ALL_PROJECTS = "{0}/index?ajax=fetchuserprojects&session.id={1}";
    private static final String SCHEDULE_CRON_FLOW = "{0}/schedule?ajax=scheduleCronFlow&session.id={1}&" +
            "projectName={2}&flow={3}&cronExpression={4}";
    private static final String FETCH_SCHEDULE = "{0}/schedule?ajax=fetchSchedule&session.id={1}&projectId={2}&flowId={3}";

    public AzkabanApiImpl(String uri, String username, String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    /**
     * 登录 API
     *
     * @return LoginResponse
     */
    public LoginResponse login() throws IOException {
        Response res = Request.Post(uri)
                .bodyForm(Form.form()
                        .add("action", "login")
                        .add("username", username)
                        .add("password", password).build())
                .execute();
        HttpEntity entity = res.returnResponse().getEntity();
        String content = EntityUtils.toString(entity).replace("session.id", "sessionId");
        LoginResponse response = ResponseHandler.handle(content, LoginResponse.class);
        if (StringUtils.isNotEmpty(response.getSessionId())) {
            this.sessionId = response.getSessionId();
        }
        return response;
    }

    @Override
    public BaseResponse createProject(String name, String desc) {
        Request res = Request.Post(uri + "/manager")
                .bodyForm(Form.form()
                        .add("session.id", sessionId)
                        .add("action", "create")
                        .add("name", name)
                        .add("description", desc).build());
        return ResponseHandler.handle(res);
    }

    @Override
    public BaseResponse deleteProject(String name) {
        Request res = Request.Get(MessageFormat.format(DELETE_PROJECT, uri, name, sessionId));
        return ResponseHandler.handle(res);
    }

    @Override
    public ProjectZipResponse uploadProjectZip(String filePath, String projectName) {
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addBinaryBody("file", new File(filePath))
                .addTextBody("session.id", sessionId)
                .addTextBody("ajax", "upload")
                .addTextBody("project", projectName)
                .build();
        Request res = Request.Post(uri + "/manager")
                .body(entity);
        return ResponseHandler.handle(res, ProjectZipResponse.class);
    }

    @Override
    public FetchFlowsResponse fetchProjectFlows(String projectName) {
        Request res = Request.Get(MessageFormat.format(FETCH_PROJECT_FLOWS, uri, sessionId, projectName));
        return ResponseHandler.handle(res, FetchFlowsResponse.class);
    }

    @Override
    public ExecuteFlowResponse executeFlow(String projectName, String flowName) {
        Request res = Request.Post(MessageFormat.format(EXECUTE_FLOW, uri, sessionId, projectName, flowName));
        return ResponseHandler.handle(res, ExecuteFlowResponse.class);
    }

    @Override
    public BaseResponse cancelFlow(String execId) {
        Request res = Request.Post(MessageFormat.format(CANCEL_FLOW, uri, sessionId, execId));
        return ResponseHandler.handle(res);
    }

    @Override
    public FetchExecFlowResponse fetchExecFlow(String execId) {
        Request res = Request.Get(MessageFormat.format(FETCH_EXEC_FLOW, uri, sessionId, execId));
        return ResponseHandler.handle(res, FetchExecFlowResponse.class);
    }

    @Override
    public FetchExecJobLogs fetchExecJobLogs(String execId, String jobId, int offset, int length) {
        Request res = Request.Get(
                MessageFormat.format(FETCH_EXEC_JOB_LOGS, uri, sessionId, execId, jobId, String.valueOf(offset),
                        String.valueOf(length))
        );
        return ResponseHandler.handle(res, FetchExecJobLogs.class);
    }

    @Override
    public FetchFlowExecutionsResponse fetchFlowExecutions(String projectName,
                                                           String flowName,
                                                           int start,
                                                           int length) {
        Request res = Request.Get(
                MessageFormat.format(FETCH_FLOW_EXECUTIONS, uri, sessionId, projectName, flowName,
                        String.valueOf(start), String.valueOf(length))
        );
        return ResponseHandler.handle(res, FetchFlowExecutionsResponse.class);
    }


    @Override
    public FetchAllProjectsResponse fetchAllProjects() {
        Request res = Request.Get(MessageFormat.format(FETCH_ALL_PROJECTS, sessionId));
        return ResponseHandler.handle(res, FetchAllProjectsResponse.class);
    }

    @Override
    public ScheduleCronFlowResponse scheduleCronFlow(String projectName, String flowName, String cronExpression) {
        Request res = Request.Post(
                MessageFormat.format(SCHEDULE_CRON_FLOW, uri, sessionId, projectName, flowName, cronExpression)
        );
        return ResponseHandler.handle(res, ScheduleCronFlowResponse.class);
    }

    @Override
    public FetchScheduleResponse fetchSchedule(String projectId, String flowId) {
        Request res = Request.Get(MessageFormat.format(FETCH_SCHEDULE, uri, sessionId, projectId, flowId));
        return ResponseHandler.handle(res, FetchScheduleResponse.class);
    }

    @Override
    public BaseResponse removeSchedule(String scheduleId) {
        Request res = Request.Post(uri + "/schedule")
                .bodyForm(Form.form()
                        .add("session.id", sessionId)
                        .add("action", "removeSched")
                        .add("scheduleId", scheduleId).build());
        return ResponseHandler.handle(res);
    }
}
