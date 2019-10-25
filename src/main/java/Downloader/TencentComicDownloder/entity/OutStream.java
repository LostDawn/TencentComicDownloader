package Downloader.TencentComicDownloder.entity;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.http.util.CharArrayBuffer;

import Downloader.TencentComicDownloder.utils.Log;

public class OutStream
{
	private Object out;
	private int outType;
	private int totalOffset;
	
	public OutStream(Object out)
	{
		this.out = out;
		totalOffset = 0;
		String objectType = out.getClass().getName();
		if (objectType.equals("org.apache.http.util.CharArrayBuffer"))
			outType = 1;
		else if (objectType.equals("java.io.RandomAccessFile"))
			outType = 2;
		else
			outType = 0;
	}
	public void write(byte[] buffer,int offset,int len) throws IOException
	{
		switch (outType)
		{
		case 1:
			((CharArrayBuffer)out).append(buffer,offset,len);
			break;
		case 2:
			((RandomAccessFile)out).write(buffer,offset,len);
			break;
		}
	}
	public void close() throws IOException
	{
		switch (outType)
		{
		case 2:
			((RandomAccessFile)out).close();
			break;
		}
	}
}
