package com.yzw.stock.controller;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;


public class FileUploadCilent {
	private final static Logger log = Logger.getLogger(FileUploadCilent.class
			.getName());

	private InetSocketAddress inetSocketAddress;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	private SocketChannel client;
	public FileUploadCilent(String fileName,String fileType) {
		try {
			inetSocketAddress = new InetSocketAddress("58.96.176.204", 12345);
			init(fileName,fileType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init(String fileName,String fileType) {
		try {
			client = SocketChannel.open();
			client.connect(inetSocketAddress);  
			client.configureBlocking(false);
			sendFile(fileName,fileType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendFile(String fileName,String fileType) {

		FileInputStream fis = null;

		FileChannel channel = null;

		try {
			File file = new File(fileName);
			fis = new FileInputStream(file);
			channel = fis.getChannel();
			int i = 1;
			int sum = 0;
			int len = 0;
			Long file_length = file.length();

			//写头部数据
			String header = "10000_" + file_length + "_"+fileType+"_0";
			ByteBuffer buf = ByteBuffer.allocate(header.getBytes().length + 1);
			buf.put(header.getBytes());
			buf.put((byte) 0);
			buf.flip();
			client.write(buf);
			buf.clear();

			long total = 0;
			//写文件内容
			while ((len = channel.read(sendBuffer)) != -1) {
				sendBuffer.flip();
				int send = client.write(sendBuffer);
				//服务器缓冲区满的情况
				while (send == 0) {
					Thread.sleep(10);
					send = client.write(sendBuffer);
				}
				
				total += send;
				float progress = (float)total/(float)file_length;
				System.out.print("" + progress);
				sendBuffer.clear();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
				channel.close();
				fis.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private byte[] i2b(int i) {
		// 4个字节
		return new byte[] {
		(byte) ((i >> 24) & 0xFF),

		(byte) ((i >> 16) & 0xFF),

		(byte) ((i >> 8) & 0xFF),

		(byte) (i & 0xFF),

		};

	}

	
	public static void main(String[] args) {
		new FileUploadCilent("d:\\03.jpg","jpg");
	}

}
