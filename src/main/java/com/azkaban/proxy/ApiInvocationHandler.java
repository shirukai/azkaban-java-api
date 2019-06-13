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
