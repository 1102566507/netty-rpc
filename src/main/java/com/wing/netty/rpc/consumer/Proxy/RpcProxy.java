package com.wing.netty.rpc.consumer.Proxy;

import com.wing.netty.rpc.protocol.InvokeProtocol;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author wing
 * @date 2019-06-17 10:27
 */
public class RpcProxy {


    public static <T>  T create(Class<?> clazz) {
        Class[] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();
        T service =(T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, new MethodProxy(clazz));
        return  service;
    }

  private static  class MethodProxy implements  InvocationHandler{

      private final Class<?> clazz;

      public MethodProxy(Class<?> clazz) {
          this.clazz=clazz;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          //传输协议封装
          InvokeProtocol msg = new InvokeProtocol();
          msg.setClassName(this.clazz.getName());
          msg.setMethodName(method.getName());
          msg.setValues(args);
          msg.setParames(method.getParameterTypes());

          final RpcProxyHandler consumerHandler = new RpcProxyHandler();
          EventLoopGroup group = new NioEventLoopGroup();
          Bootstrap b = new Bootstrap();
          b.group(group)
                  .channel(NioSocketChannel.class)
                  .option(ChannelOption.TCP_NODELAY,true)
                  .handler(new ChannelInitializer<SocketChannel>() {
                      @Override
                      protected void initChannel(SocketChannel ch) throws Exception {
                          ChannelPipeline pipeline = ch.pipeline();
                          //自定义协议解码器
                          /** 入参有5个，分别解释如下
                           maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                           lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                           lengthFieldLength：长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
                           lengthAdjustment：要添加到长度字段值的补偿值
                           initialBytesToStrip：从解码帧中去除的第一个字节数
                           */
                          pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                          //自定义协议编码器
                          pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                          //对象参数类型编码器
                          pipeline.addLast("encoder", new ObjectEncoder());
                          //对象参数类型解码器
                          pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                          pipeline.addLast("handler", consumerHandler);
                      }
                  });

          ChannelFuture future = b.connect("localhost", 8080).sync();
          future.channel().writeAndFlush(msg).sync();
          future.channel().closeFuture().sync();

          group.shutdownGracefully();

          return consumerHandler.getResponse();
      }
  }

}
