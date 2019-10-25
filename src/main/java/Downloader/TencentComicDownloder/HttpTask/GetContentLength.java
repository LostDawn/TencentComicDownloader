package Downloader.TencentComicDownloder.HttpTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.CharArrayBuffer;

import Downloader.TencentComicDownloder.core.NetworkController;
import Downloader.TencentComicDownloder.entity.OutStream;
import Downloader.TencentComicDownloder.utils.Log;

public class GetContentLength extends HttpTask
{
	public static final int DefauktPriority = 32;
	
	public GetContentLength(String url,NetworkController controller)
	{
		super(null,DefauktPriority);
		setParam("url",url);
		if (controller!=null)
			setParam("networkController",controller);
	}
	public GetContentLength(String url)
	{
		this(url,null);
	}
	
	@Override
	public Object executeHandler() throws Exception
	{
		HttpGet request = new HttpGet((String)getParam("url"));
		request = (HttpGet)browserEmulation(request);
		HttpResponse response = HttpClientBuilder.create().build().execute(request);
		int result = (int)(response.getEntity().getContentLength());
		return result;
	}
	@Override
	public void errorHandler(Exception e)
	{
		NetworkController controller = (NetworkController)getParam("networkController");
		if (e instanceof InterruptedException)
		{
			Log.err("Error : HttpTask GetContentLength " + getParam("url") + " interrupted. try again later ...");
			if (controller!=null)
				controller.addTask(this);
		}
		else
		{
			Log.err("Error : HttpTask GetContentLength " + getParam("url") + " failed, try again later ...");
			e.printStackTrace();
			if (controller!=null)
			{
				controller.addTask(this);
				controller.needChangeIP();
			}
		}
	}
}
