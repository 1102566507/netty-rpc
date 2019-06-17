package com.wing.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wing
 * @date 2019-06-16 20:41
 */
    @Data
public class InvokeProtocol implements Serializable {

    private String className;
    private String methodName;
    private Class<?>[] parames;
    private Object[] values;



}
