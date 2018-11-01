package netty.tcp.filetransfer;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class FilClient {

	private static final int PORT = 8080;

	private Channel channel;
	private EventLoopGroup workerGroup;
	//private String srcfilePath = "/Users/subramanya/netty/image.jpg";
	 private String srcfilePath = "/Users/subramanya/netty/micro.pdf";
	int maxLength = Integer.MAX_VALUE;

	public FilClient() {

	}

	public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel socketChannel) throws Exception {
			ChannelPipeline pipeline = socketChannel.pipeline();
			
			 pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));
             pipeline.addLast(new LengthFieldPrepender(4));
             pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
             pipeline.addLast(new ObjectEncoder());

             pipeline.addLast("clientHandler", new FilClientHandler());
		}

	}

	private void startUp() throws Exception {
		workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap clientBootstrap = new Bootstrap();
			clientBootstrap.group(workerGroup);
			clientBootstrap.channel(NioSocketChannel.class);
			clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
			clientBootstrap.handler(new LoggingHandler(LogLevel.INFO));
			clientBootstrap.handler(new ClientChannelInitializer());
			channel = clientBootstrap.connect(new InetSocketAddress("localhost", PORT)).sync().channel();

			
			//ByteBuf srcfilePathByteBuff = Unpooled.wrappedBuffer(srcfilePath.getBytes());
			ChannelFuture channelFuture = channel.writeAndFlush(srcfilePath);
			channelFuture.isDone();

			// Thread.sleep(1000);
			channel.closeFuture().sync();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new FilClient().startUp();
	}
}