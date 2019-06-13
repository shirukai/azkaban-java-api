# Java封装Azkaban相关API

> 版本说明：
>
> azkaban：3.43.0
>
> jdk：1.8
>
> 项目地址：https://github.com/shirukai/azkaban-java-api.git

# 1 前言

之前在项目开发记录中，写到过两篇文章[《利用AOP对Azkaban进行登录控制》](https://blog.csdn.net/shirukai/article/details/80812088)和 《[Java调用Azkaban相关服务》](https://blog.csdn.net/shirukai/article/details/80841875)，记录了在开发过程中使用spring的aop对azkaban进行了登录控制，以及使用Java请求azkaban相关的服务。总的来说，能够完成基本的需求，但是还是存在一些问题，比如：

* 深度依赖Spring，利用AOP切面进行的登录控制以及RestTemplate的HTTP请求
* Azkaban本身REST API缺陷，响应风格不统一，导致在之前的Java调用Azakban相关服务时候，请求响应统一被当做字符串处理，后期根据需要单独处理，不友好。
* 登录控制不精确

为解决如上问题，进行了代码重构。对于Azkaban API的请求脱离Spring的RestTemplate使用http-client的fluent，统一了请求响应，使用动态代理替换之前Spring的AOP，使用构建器使封装的Azkaban API支持可插拔，可以脱离Spring使用也可以整合Spring0。

# 2 统一Azkaban响应

这里为什么要统一Azkaban响应呢？我们先看一下[官网关于API的说明文档](https://azkaban.readthedocs.io/en/latest/ajaxApi.html)。

![](https://raw.githubusercontent.com/shirukai/images/master/74b9991335b6cac6133b82a3a352c4d7.jpg)

上图截了一个登录响应描述的图，看这个描述，参数是error，描述如果登录失败会返回错误信息，参数session.id 如果登录成功返回session.id。响应内容不定，还有条件语句，有错误也不报个错误码，也没有个状态描述。如果单纯是这样的逻辑就算了，看如下的官网给的简单实例

```json
{
  "status" : "success",
  "session.id" : "c001aba5-a90f-4daf-8f11-62330d034c0a"
}
```

what?怎么还有个status？意义何在，响应成功，我就给你返回一个"status":"success"，失败了，直接返回"error":"info"，难道不应该返回一个"status":"error"吗？对于其它的接口，响应也有不同的呈现，难道对响应的处理逻辑还要一个API一个嘛。所以这里为了解决响应不统一的问题，对Azkaban的响应结果进行了一层封装。

## 2.1 创建响应实体类

如下所示为响应实体类，其中BaseResponse为响应基类。

![](https://raw.githubusercontent.com/shirukai/images/master/74913d751bc5c9c2552dce052a318ec7.jpg)

BaseResponse内容如下，其中为了映射Azkaban的响应，既包含了"status"又包含了"error"，最后通过correction会更正信息到"status"，所以我们可以统一对"status"进行判断是否执行成功。内容如下：

```java
package com.azkaban.response;

import java.util.Objects;

/**
 * Created by shirukai on 2019-06-01 15:03
 */
public class BaseResponse {
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    /**
     * 响应状态
     */
    private String status;
    /**
     * 错误类型(单纯为了映射Azkaban)
     */
    private String error;
    /**
     * 详细信息
     */
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 更正信息
     */
    public void correction() {
        if (!ERROR.equals(this.status) && Objects.isNull(this.error)) {
            this.status = SUCCESS;
        } else {
            this.status = ERROR;
            if (Objects.isNull(this.error)) {
                this.error = this.message;
            } else if (Objects.isNull(this.message)) {
                this.message = this.error;
            }
        }
    }
}

```

## 2.2 响应处理器

为了统一响应，这里使用响应处理器，对Azkaban响应进行统一处理，内容如下：

```java
package com.azkaban.response;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by shirukai on 2019-06-01 14:58
 * 响应处理器
 */
public class ResponseHandler {
    private static Logger log = LoggerFactory.getLogger(ResponseHandler.class);

    public static <T extends BaseResponse> T handle(Request request, Class<T> tClass) {
        T response = null;
        try {
            Response res = request.execute();
            HttpEntity entity  = res.returnResponse().getEntity();
            response = handle(entity, tClass);
        } catch (Exception e) {
            try {
                response = tClass.newInstance();
                response.setStatus(T.ERROR);
                response.setError(e.getMessage());
                response.correction();
            } catch (Exception ea) {
                log.warn(ea.getMessage());
            }
        }
        return response;
    }

    public static BaseResponse handle(Request request) {
        return handle(request, BaseResponse.class);
    }


    public static BaseResponse handle(String content) {
        return handle(content, BaseResponse.class);
    }

    public static <T extends BaseResponse> T handle(HttpEntity entity, Class<T> tClass) {
        T response = null;
        try {
            String content = EntityUtils.toString(entity);
            response = handle(content, tClass);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return response;
    }

    public static <T extends BaseResponse> T handle(String content, Class<T> tClass) {
        T response = null;
        try {
            response = JSONObject.parseObject(content, tClass);
            if (Objects.nonNull(response.getError())) {
                response.setStatus(T.ERROR);
            }
            response.correction();
        } catch (Exception e) {
            try {
                response = tClass.newInstance();
                response.setStatus(T.ERROR);
                response.setError(content);
                response.correction();
            } catch (Exception ea) {
                log.warn(ea.getMessage());
            }
        }
        return response;
    }
}

```



# 3 API 接口及实现

这里整理了常用的14种常见的Azkaban的API，在使用Java实现之前，使用Postman测试过，并生成了一份postman的接口文档，可以访问https://documenter.getpostman.com/view/2759292/S1TbUaeU查看。

![](https://raw.githubusercontent.com/shirukai/images/master/6e4eb4f5377833b57f2c636461f9bdbd.jpg)

## 3.1 创建API接口类

在com.azkaban.api包下创建AzkabanApi接口类，提供Azkaban相对应API的接口，内容如下所示：

```java
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
```

## 3.2 创建接口实现类

接口有了，接下来就是实现接口，创建AzkabanApiImpl实现类，其中请求主要使用了http-client的fluent，内容如下：

```java
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
```

# 4 利用动态代理实现登录控制

思想与之前的AOP一样，在代理方法执行完成后，判断执行结果是否异常，如果异常，调用login方法，进行Azkaban登录，然后重新执行代理方法。

## 4.1 创建ApiInvocationHandler

ApiInvocationHander继承java.lang.reflect.InvocationHandler，重写invoke()方法，是动态代理的调用处理器。其中主要实现了三个功能，一个是代理AzkabanApiImpl提供的方法，二是判断执行结果是否异常，如果异常进行登录，三是进行统一的异常处理。代码如下：

```java
package com.azkaban.proxy;


import com.azkaban.api.AzkabanApiImpl;
import com.azkaban.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by shirukai on 2019-06-01 20:17
 * API 动态代理处理类
 */
public class ApiInvocationHandler implements InvocationHandler {
    /**
     * api 接口实现类实例
     */
    private AzkabanApiImpl azkabanApi;
    /**
     * 重试次数
     */
    private static final Integer RETRY = 2;
    /**
     * object默认方法
     */
    private List<String> defaultMethods;
    private static Logger log = LoggerFactory.getLogger(ApiInvocationHandler.class);

    ApiInvocationHandler(AzkabanApiImpl azkabanApi) {
        this.azkabanApi = azkabanApi;
        this.defaultMethods = new ArrayList<>(16);
        for (Method method : Object.class.getMethods()) {
            this.defaultMethods.add(method.getName());
        }
    }

    /**
     * 判断是否为默认方法
     *
     * @param methodName 方面名称
     * @return boolean
     */
    private boolean isDefault(String methodName) {
        return this.defaultMethods.contains(methodName);
    }

    /**
     * 重写动态代理invoke方法
     *
     * @param proxy  代理实例
     * @param method 方法
     * @param args   参数
     * @return 执行结果
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Object result = null;
        int tryTime = 1;
        try {
            while (tryTime <= RETRY) {
                // 指定代理方法
                result = method.invoke(azkabanApi, args);
                // 判断是否执行成功
                if (Objects.nonNull(result) && !this.isDefault(method.getName())) {
                    Class superClass = result.getClass().getSuperclass();
                    if (Object.class.equals(superClass)) {
                        superClass = result.getClass();
                    }
                    Field field = superClass.getDeclaredField("status");
                    field.setAccessible(true);
                    if (BaseResponse.SUCCESS.equals(field.get(result))) {
                        log.info("Execute the azkaban's API {} successfully.", method.getName());
                        return result;
                    }
                    azkabanApi.login();
                }

                tryTime++;
            }
        } catch (Exception e) {
            // 如果返回结果为null,捕获异常并实重新生成结果实例，设置异常信息
            if (Objects.isNull(result)) {
                Class returnType = method.getReturnType();
                try {
                    Object response = returnType.newInstance();
                    if (response instanceof BaseResponse) {
                        BaseResponse baseResponse = (BaseResponse) response;
                        baseResponse.setStatus(BaseResponse.ERROR);
                        if (e instanceof InvocationTargetException) {
                            baseResponse.setMessage(((InvocationTargetException) e).getTargetException().getMessage());
                        } else {
                            baseResponse.setMessage(e.getMessage());
                        }
                        baseResponse.correction();
                        result = response;
                    }

                } catch (InstantiationException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }

}

```

## 4.2 创建AzkabanApiProxyBuilder

AzkabanApiProxyBuilder是动态代理的构建器，通过构建器能够构建出代理的AzkabanApi实例。构建器需要传入azkaban的服务地址、用户名、密码。代码如下：

```java
package com.azkaban.proxy;


import com.azkaban.api.AzkabanApi;
import com.azkaban.api.AzkabanApiImpl;

import java.lang.reflect.Proxy;

/**
 * Created by shirukai on 2019-06-01 22:26
 * azkaban api Builder
 */
public class AzkabanApiProxyBuilder {
    private String uri;
    private String username;
    private String password;

    private AzkabanApiProxyBuilder() {
    }


    public static AzkabanApiProxyBuilder create() {
        return new AzkabanApiProxyBuilder();
    }

    public AzkabanApiProxyBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public AzkabanApiProxyBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public AzkabanApiProxyBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public AzkabanApi build() {
        AzkabanApiImpl impl = new AzkabanApiImpl(this.uri, this.username, this.password);
        ApiInvocationHandler handler = new ApiInvocationHandler(impl);
        return (AzkabanApi) Proxy.newProxyInstance(
                impl.getClass().getClassLoader(),
                impl.getClass().getInterfaces(),
                handler);
    }

}

```

# 5 两种方式调用API

## 5.1 普通方式调用

无论在什么时候使用，只要使用代理构建器构建出AzkabanApi实例即可。如下代码所示：

```python
    @Test
    public void builder() {
        AzkabanApi apis = AzkabanApiProxyBuilder.create()
                .setUri("http://localhost:8666")
                .setUsername("azkaban")
                .setPassword("azkaban")
                .build();
    }
```

## 5.2 整合Spring

通过AzkabanApiConfig类，创建Bean注册到Spring里，代码如下所示：

```java
package com.azkaban.config;


import com.azkaban.api.AzkabanApi;
import com.azkaban.proxy.AzkabanApiProxyBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by shirukai on 2019-06-03 11:05
 * 配置API，创建Bean，并注入Spring
 */
@Configuration
public class AzkabanApiConfig {
    @Value("${azkaban.url}")
    private String uri;

    @Value("${azkaban.username}")
    private String username;

    @Value("${azkaban.password}")
    private String password;

    @Bean
    public AzkabanApi azkabanApi() {
        return AzkabanApiProxyBuilder.create()
                .setUri(uri)
                .setUsername(username)
                .setPassword(password)
                .build();
    }
}

```

调用

```java
    @Autowired
    private AzkabanApi azkabanApi;
```

# 6 总结

之前写过两篇关于Azkaban的文章，都是关于使用Java调用Azkaban的API。其中有同学问具体代码，这次把重构后的代码提交到了github上了，项目地址：https://github.com/shirukai/azkaban-java-api.git。
