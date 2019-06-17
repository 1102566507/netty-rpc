package com.wing.netty.rpc.registry;

import com.wing.netty.rpc.protocol.InvokeProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wing
 * @date 2019-06-16 20:51
 */
public class RegistryHandler extends ChannelInboundHandlerAdapter {
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> registryMap = new ConcurrentHashMap<String, Object>();
    //1:根据一个包名将所有复合要求的class 都扫面出来

    //2:给每一个class 起一个名称 注册到一个容器中
    //3:当客户端有一个连接过来的时候,就会获取协议内容的对象
    //4:去注册好的容器中找到复合条件的服务,
    //5:通过远程调用provider 得到返回结果 并返回给客户端

    public RegistryHandler() {
        //递归扫描
        scannerClass("com.wing.netty.rpc.provider");
        //完成注册
        doRegistry();
    }

    /**
     * 完成注册
     */
    private void doRegistry() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(), clazz.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 完成递归扫描
     *
     * @param packageName
     */
    private void scannerClass(String packageName) {
        //将对应包下面的class文件扫描出来 保存在一个数组当中
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scannerClass(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replaceAll(".class", ""));
            }
        }

    }

    /**
     * y有客戶端連上就會回調
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokeProtocol request = (InvokeProtocol) msg;
        if (registryMap.containsKey(request.getClassName())) {
            Object clazz = registryMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParames());
            result = method.invoke(clazz, request.getValues());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    /**
     * 異常回調
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

}
