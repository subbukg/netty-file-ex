package netty.tcp.filetransfer;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class FilClientHandler extends SimpleChannelInboundHandler<Object> {

	 private String destfilePath = "/Users/subramanya/netty/testcopy.pdf";
	//private String destfilePath = "/Users/subramanya/netty/copy_image.jpg";
	private FileOutputStream fileOutStream = null;
	private FileChannel channel = null;
	
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if(channel!= null) {
			channel.close();
	
		}
	if(fileOutStream!= null) {
		fileOutStream.close();

		}}
	
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("Data has arrived....on the Client Side!"+msg);
		
		ByteBuf buffer = (ByteBuf) msg; 
		if(fileOutStream == null) {
			fileOutStream = new FileOutputStream(destfilePath);
			channel = fileOutStream.getChannel();
		}
		ByteBuffer nioBuffer = buffer.nioBuffer();
		
		
		while (nioBuffer.hasRemaining()) {
		    channel.write(nioBuffer);
		}
				
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// super.channelReadComplete(ctx);
		System.out.println("Inside Client read complete");
		// ctx.writeAndFlush(srcfilePath);
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println("=========> Client Exc");

		cause.printStackTrace();
		// ctx.close();
	}


	public String toString(ByteBuf byteBuf) {
		String byteBufStr = "";
		byteBuf.toString(CharsetUtil.UTF_8);
		return byteBufStr;
	}

	public ByteBuf ToByteBuff(String str) {
		ByteBuf byBuff = null;
		byte[] byteArray = str.getBytes(CharsetUtil.UTF_8);
		byBuff = Unpooled.wrappedBuffer(byteArray);
		return byBuff;
	}



}