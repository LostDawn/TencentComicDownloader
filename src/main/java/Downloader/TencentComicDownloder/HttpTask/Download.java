package Downloader.TencentComicDownloder.HttpTask;

import java.io.RandomAccessFile;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import Downloader.TencentComicDownloder.core.NetworkController;
import Downloader.TencentComicDownloder.entity.OutStream;
import Downloader.TencentComicDownloder.utils.Log;

public class Download extends HttpTask
{
	public static final int DefauktPriority = 128;
	
	public Download(String url,int start,String file,NetworkController controller)
	{
		super(null,DefauktPriority);
		setParam("url",url);
		setParam("start",start);
		setParam("file",file);
		if (controller!=null)
			setParam("networkController",controller);
	}
	public Download(String url,String file,NetworkController controller)
	{
		this(url,0,file,controller);
	}
	public Download(String url,String file)
	{
		this(url,0,file,null);
	}
	
	@Override
	public Object executeHandler() throws Exception
	{
		HttpGet request = new HttpGet((String)getParam("url"));
		request = (HttpGet)browserEmulation(request);
		int start = (Integer)(getParam("start"));
		if (params.containsKey("completedSize"))
			start += (Integer)getParam("completedSize");
		if (params.containsKey("end"))
			request.setHeader("Range","bytes=" + start + "-" + (Integer)getParam("end"));
		HttpResponse response = HttpClientBuilder.create().build().execute(request);
        RandomAccessFile raf = new RandomAccessFile((String)getParam("file"), "rws");
        raf.seek(start);
        fetchContent(response,new OutStream(raf));
        raf.close();
        return null;
	}
	@Override
	public void errorHandler(Exception e)
	{
		NetworkController controller = (NetworkController)getParam("networkController");
		if (e instanceof InterruptedException)
		{
			setParam("completedSize",Integer.valueOf(e.getMessage()));
			Log.err("Error : HttpTask Download " + getParam("url") + " interrupted. try again later ...");
			if (controller!=null)
				controller.addTask(this);
		}
		else
		{
			Log.err("Error : HttpTask Download " + getParam("url") + " failed, try again later ...");
			e.printStackTrace();
			if (controller!=null)
			{
				controller.addTask(this);
				controller.needChangeIP();
			}
		}
	}
}
