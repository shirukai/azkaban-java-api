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
