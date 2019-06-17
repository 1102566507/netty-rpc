package com.wing.netty.rpc.api;

/**
 * @author wing
 * @date 2019-06-17 9:45
 */
public interface IRpcService {

    /** 加 */
    public int add(int a,int b);

    /** 减 */
    public int sub(int a,int b);

    /** 乘 */
    public int mult(int a,int b);

    /** 除 */
    public int div(int a,int b);


}
