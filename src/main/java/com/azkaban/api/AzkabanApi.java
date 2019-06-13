package com.azkaban.api;

import com.azkaban.response.*;


/**
 * Created by shirukai on 2019-06-01 20:12
 * azkaban api 接口
 */
public interface AzkabanApi {

    /**
     * 创建项目 API
     *
     * @param name 项目名称
     * @param desc 项目描述
     * @return BaseResponse
     */
    BaseResponse createProject(String name, String desc);

    /**
     * 删除项目 API
     *
     * @param name 项目名称
     * @return BaseResponse
     */
    BaseResponse deleteProject(String name);

    /**
     * 上传Zip API
     *
     * @param filePath    zip文件路径
     * @param projectName 项目名称
     * @return ProjectZipResponse
     */
    ProjectZipResponse uploadProjectZip(String filePath, String projectName);

    /**
     * 查询项目Flows
     *
     * @param projectName 项目名称
     * @return FetchFlowsResponse
     */
    FetchFlowsResponse fetchProjectFlows(String projectName);

    /**
     * 执行flow
     *
     * @param projectName 项目名称
     * @param flowName    flow名称
     * @return ExecuteFlowResponse
     */
    ExecuteFlowResponse executeFlow(String projectName, String flowName);

    /**
     * 取消执行flow
     *
     * @param execId 执行ID
     * @return BaseResponse
     */
    BaseResponse cancelFlow(String execId);

    /**
     * 查询执行Flow信息
     *
     * @param execId 执行ID
     * @return FetchExecFlowResponse
     */
    FetchExecFlowResponse fetchExecFlow(String execId);


    /**
     * 查询执行Job的日志
     *
     * @param execId 执行ID
     * @param jobId  JobID
     * @param offset 起始位置
     * @param length 长度
     * @return FetchExecJobLogs
     */
    FetchExecJobLogs fetchExecJobLogs(String execId, String jobId, int offset, int length);

    /**
     * 查询Flow的执行记录
     *
     * @param projectName 项目名称
     * @param flowName    flow名称
     * @param start       开始位置
     * @param length      查询条数
     * @return FetchFlowExecutionsResponse
     */
    FetchFlowExecutionsResponse fetchFlowExecutions(String projectName, String flowName, int start, int length);

    /**
     * 查询所有项目
     *
     * @return FetchAllProjectsResponse
     */
    FetchAllProjectsResponse fetchAllProjects();


    /**
     * 设置定时任务
     *
     * @param projectName    项目名称
     * @param flowName       Flow名称
     * @param cronExpression cron表达式
     * @return ScheduleCronFlowResponse
     */
    ScheduleCronFlowResponse scheduleCronFlow(String projectName, String flowName, String cronExpression);

    /**
     * 查询定时任务
     *
     * @param projectId 项目ID
     * @param flowId    Flow ID
     * @return FetchScheduleResponse
     */
    FetchScheduleResponse fetchSchedule(String projectId, String flowId);

    /**
     * 移除定时任务
     *
     * @param scheduleId schedule ID
     * @return BaseResponse
     */
    BaseResponse removeSchedule(String scheduleId);
}
