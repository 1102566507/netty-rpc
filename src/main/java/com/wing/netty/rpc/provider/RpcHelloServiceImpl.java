package com.wing.netty.rpc.provider;

import com.wing.netty.rpc.api.IRpcHelloService;

/**
 * @author wing
 * @date 2019-06-16 20:26
 */
public class RpcHelloServiceImpl implements IRpcHelloService {


    public String hello(String name) {
        return "Hello " + name + "!";
    }
}
