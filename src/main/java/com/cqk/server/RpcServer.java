/**
 * @author qkcao
 * @date 2023/7/28 14:07
 */
package com.cqk.server;

import com.alibaba.fastjson.JSON;
import com.cqk.bean.RpcRequest;
import com.cqk.codec.RpcDecoder;
import com.cqk.codec.RpcEncoder;
import com.cqk.registry.IServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册
 * 监听客户端
 */
public class RpcServer implements InitializingBean, ApplicationContextAware {
    private static final Logger LOG = Logger.getLogger(RpcServer.class);
    private final static Map<String, Object> handlerMap = new ConcurrentHashMap<String, Object>();
    private String serviceAddress;
    private IServiceRegistry serviceRegistry;


    public RpcServer(String serviceAddress, IServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 对当前bean传入对应的Spring上下文
     * Spring容器会在创建bean之后，自动调用该Bean的setApplicationContextAware()方法，
     */

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            // 首先从容器获取要注册的Bean
            LOG.info("获取的上下文=" + JSON.toJSONString(applicationContext));
            Map<String, Object> beansMap = applicationContext.getBeansWithAnnotation(RpcService.class);
            LOG.info("从容器中获取的Bean=" + JSON.toJSONString(beansMap));
            if (beansMap != null) {
                for (Map.Entry<String, Object> map : beansMap.entrySet()) {
                    // @RpcService(value = IHelloRPC.class, version = "sample.hello.person")
                    RpcService annotation = map.getValue().getClass().getAnnotation(RpcService.class);
                    String serviceName = annotation.value().getName();
                    LOG.info("setApplicationContext.serviceName" + JSON.toJSONString(serviceName));

                    String serviceVersion = annotation.version();
                    if (StringUtils.isNotBlank(serviceVersion)) {
                        serviceName += "-" + serviceVersion;
                    }
                    handlerMap.put(serviceName, map.getValue());
                }
            }
        } catch (Exception e) {
            LOG.error("RpcServer.setApplicationContext=" + e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup worker = new NioEventLoopGroup();
        EventLoopGroup master = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(master, worker);
            serverBootstrap.channel(NioServerSocketChannel.class);

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    // 解码RPC请求
                    pipeline.addLast(new RpcDecoder(RpcRequest.class));
                    // 编码RPC请求
                    pipeline.addLast(new RpcEncoder(RpcRequest.class));
                    pipeline.addLast(new RpcServerHandler(handlerMap));
                }
            });
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            // 获取RPC服务器IP地址与端口
            String[] addressAry = StringUtils.splitByWholeSeparator(serviceAddress, ":");
            String ip = addressAry[0];
            int port = Integer.parseInt(addressAry[1]);
            ChannelFuture future = serverBootstrap.bind(ip, port);
            if (serviceRegistry != null) {
                for (String interfaceName : handlerMap.keySet()) {
                    serviceRegistry.register(interfaceName, serviceAddress);
                    LOG.info("注册的服务为:[" + interfaceName + "-" + serviceAddress + "]");
                }
            }
            // 关闭RPC服务器 sync:是一个阻塞操作，它会一直等待直到异步操作完成
            // 当调用该方法时，当前线程会阻塞，直到通道关闭操作完成，确保通道关闭操作完成之前，当前线程不会继续执行后面的代码
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("RpcServer.afterPropertiesSet=" + e.getMessage());
        } finally {
            // 优雅关闭一个netty应用程序的方法，它以非阻塞方法关闭所有的网络资源，并且等待所有正在处理的任务完成
            worker.shutdownGracefully();
            master.shutdownGracefully();
        }

    }

}
