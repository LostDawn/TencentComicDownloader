package Downloader.TencentComicDownloder.HttpTask;

import java.io.RandomAccessFile;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.CharArrayBuffer;

import Downloader.TencentComicDownloder.core.NetworkController;
import Downloader.TencentComicDownloder.entity.OutStream;
import Downloader.TencentComicDownloder.utils.Log;

public class GetContent extends HttpTask
{
	public static final int DefauktPriority = 64;
	
	public GetContent(String url,NetworkController controller)
	{
		super(null,DefauktPriority);
		setParam("url",url);
		if (controller!=null)
			setParam("networkController",controller);
	}
	public GetContent(String url)
	{
		this(url,null);
	}
	
	@Override
	public Object executeHandler() throws Exception
	{
		HttpGet request = new HttpGet((String)getParam("url"));
		request = (HttpGet)browserEmulation(request);
		HttpResponse response = HttpClientBuilder.create().build().execute(request);
		int totalLen = (int)response.getEntity().getContentLength();
		if (totalLen<0)
			totalLen = 4096;
		CharArrayBuffer buffer = new CharArrayBuffer(totalLen);
		fetchContent(response,new OutStream(buffer));
		return buffer.toString();
	}
	@Override
	public void errorHandler(Exception e)
	{
		NetworkController controller = (NetworkController)getParam("networkController");
		if (e instanceof InterruptedException)
		{
			Log.err("Error : HttpTask GetContent " + getParam("url") + " interrupted. try again later ...");
			if (controller!=null)
				controller.addTask(this);
		}
		else
		{
			Log.err("Error : HttpTask GetContent " + getParam("url") + " failed, try again later ...");
			e.printStackTrace();
			if (controller!=null)
			{
				controller.addTask(this);
				controller.needChangeIP();
			}
		}
	}
}