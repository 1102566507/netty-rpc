package com.wing.netty.rpc.consumer;

import com.wing.netty.rpc.api.IRpcHelloService;
import com.wing.netty.rpc.api.IRpcService;
import com.wing.netty.rpc.consumer.Proxy.RpcProxy;


public class RpcConsumer {
	
    public static void main(String [] args){  
        IRpcHelloService rpcHello = RpcProxy.create(IRpcHelloService.class);
        
        System.out.println(rpcHello.hello("Tom老师"));

        IRpcService service = RpcProxy.create(IRpcService.class);
        
        System.out.println("8 + 2 = " + service.add(8, 2));
        System.out.println("8 - 2 = " + service.sub(8, 2));
        System.out.println("8 * 2 = " + service.mult(8, 2));
        System.out.println("8 / 2 = " + service.div(8, 2));
    }
    
}
