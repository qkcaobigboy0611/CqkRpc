/**
 * @author qkcao
 * @date 2023/7/30 08:16
 */
package com.cqk.client;

import com.cqk.bean.RpcRequest;
import com.cqk.bean.RpcResponse;
import com.cqk.codec.RpcDecoder;
import com.cqk.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * add qkcao 0514
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private final static Log LOG = LogFactory.getLog(RpcClient.class);
    private RpcResponse response;

    private String host;
    private int port;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
    }

    public RpcResponse send(RpcRequest rpcRequest) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline channelPipeline = socketChannel.pipeline();
                    channelPipeline.addLast(new RpcEncoder(RpcRequest.class));//编码RPC请求
                    channelPipeline.addLast(new RpcDecoder(RpcResponse.class));//解码RPC响应
                    channelPipeline.addLast(RpcClient.this);
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();
            channel.writeAndFlush(rpcRequest).sync();
            channel.closeFuture().sync();
        } catch (Exception e) {
            LOG.error("");
        } finally {
            group.shutdownGracefully();
        }
        return response;
    }
}
