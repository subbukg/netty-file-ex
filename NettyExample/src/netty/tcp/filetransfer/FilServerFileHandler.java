package netty.tcp.filetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;

public class FilServerFileHandler extends SimpleChannelInboundHandler<Object> {

	private boolean SSL = false;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		//ByteBuf in = (ByteBuf) msg;
		
		//String filePathStr = 	new String(ByteBufUtil.getBytes(in), Charset.forName("UTF-8"));

		String filePathStr = msg.toString();	
		
		System.out.println("Control Arrived.....Server.."+filePathStr);


		
		File file = new File(filePathStr);
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException fnfe) {
			return;
		}
		long fileLength = raf.length();
		// Write the initial line .

		// Write the content.
		ChannelFuture sendFileFuture;
		if (SSL) {
			System.out.println("Sending file in ssl mode...");
			sendFileFuture = ctx.writeAndFlush(new ChunkedFile(raf, 0, fileLength, 8192), ctx.newProgressivePromise());

		} else {
			System.out.println("Non SSL File Sending...");
			DefaultFileRegion def = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
			sendFileFuture = ctx.writeAndFlush(def,ctx.newProgressivePromise());
		}

		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) { // total unknown
					System.err.println("Transfer progress: " + progress);
				} else {
					System.err.println("Transfer progress: " + progress + " / " + total);
				}
			}

			@Override
			public void operationComplete(ChannelProgressiveFuture future) throws Exception {
				System.err.println("Transfer complete.");
			}
		});

		// Wait until all messages are flushed before closing the channel.
		if (sendFileFuture != null) {
			// sendFileFuture.sync();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}



}
