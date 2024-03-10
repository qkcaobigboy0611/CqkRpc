/**
 * @author qkcao
 * @date 2023/7/28 17:45
 */
package com.cqk.server;

import com.alibaba.fastjson.JSON;
import com.cqk.bean.RpcRequest;
import com.cqk.bean.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;

/**
 * 用于接收入站的消息：将接收的消息转换为需要的类型，并对消息进行处理
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOG = Logger.getLogger(SimpleChannelInboundHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }


    /**
     * rpcRequest用于接收客户端传过来的接口名字，参数等数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        LOG.info("channelRead0.RpcRequest=" + JSON.toJSONString(rpcRequest));
        RpcResponse response = new RpcResponse();

        try {
            String interfaceName = rpcRequest.getInterfaceName();
            String serviceVersion = rpcRequest.getServiceVersion();
            if (StringUtils.isNotBlank(serviceVersion)) {
                interfaceName += "-" + serviceVersion;
            }
            Object serviceBean = handlerMap.get(interfaceName);
            if (null == serviceBean) {
                throw new RuntimeException("该接口不存在=" + interfaceName);
            }
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = rpcRequest.getMethodName();
            Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
            Object[] parameters = rpcRequest.getParameters();
//            // 反射
//            // 是一个反射方法，用于获取指定类中的的指定方法。参数为：方法名和参数类型
//            Method method = serviceClass.getMethod(methodName, parameterTypes);
//            // 设置为true时，可以提高反射速度，原因是：可以跳过访问检查，即使private修饰的也可以访问
//            method.setAccessible(true);
            //cglib反射
            // 这个FastClassd对象相当于A的方法索引，根据A的方法名生成并关联一个index、每个index对应A的一个方法
            FastClass serviceFastClass = FastClass.create(serviceClass);

            //主要用于高性能地调用和执行被代理对象的方法
            // 由于CGLIB动态生成的子类继承自被代理对象的父类，因此在执行被代理对象的方法时，
            // 会经过继承层级的查找和方法调用等操作，导致额外的性能开销。为了提高执行效率，CGLIB使用FastMethod类来优化这个过程
            // FastMethod通过使用ClassVisitor和MethodVisitor技术，将需要代理的方法转换并存储为￥￥的快速执行路径，从而避免每次调用都进行查找和检查。
            // 当需要调用被代理对象的方法时，FastMethod会直接定位并执行相应的代码路径，以提高代理调用的性能。
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

            // 快速调用该方法，实现了Java反射的“运行时动态调用指定类的方法”的功能
            Object result = serviceFastMethod.invoke(serviceBean, parameters);
            response.setRequestId(rpcRequest.getRequestId());
            response.setResult(result);

            // 在 Netty 中，通过 ChannelHandler 来处理各种事件，
            // 而 ChannelHandlerContext 则表示 ChannelHandler 与 ChannelPipeline 的上下文关系。
            // 在数据传输过程中，可以使用 ChannelHandlerContext 的方法来进行零拷贝操作。
            ctx.writeAndFlush(response).addListeners(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            response.setException(e);
            LOG.error("channelRead0 is error:{}" + e.getMessage());
        }
    }
}
