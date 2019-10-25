package Downloader.TencentComicDownloder.HttpTask;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import Downloader.TencentComicDownloder.core.NetworkController;
import Downloader.TencentComicDownloder.entity.OutStream;
import Downloader.TencentComicDownloder.utils.Log;

public abstract class HttpTask implements Comparable<HttpTask>,Runnable
{
	protected TreeMap<String,Object> params;
	protected Object result;
	protected int priority;
	protected long createTime;
	
	private ReentrantLock lock;
	private Condition isCompleted;
	private boolean completed;
	
	public HttpTask(TreeMap<String,Object> param,int priority)
	{
		this.createTime = System.currentTimeMillis();
		if (param!=null)
			this.params = new TreeMap<String,Object>(param);
		else
			this.params = new TreeMap<String,Object>();
		this.priority = priority;
		
		lock = new ReentrantLock();
		isCompleted = lock.newCondition();
		completed = false;
	}
	public HttpTask(int priority)
	{
		this(null,priority);
	}
	public HttpTask()
	{
		this(null,128);
	}
	
	public void setCreateTime(long createTime)
	{
		this.createTime = createTime;
	}
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
	public void setParam(String key,Object value)
	{
		params.put(key, value);
	}
	public Object getParam(String key)
	{
		return params.get(key);
	}
	public void deleteParam(String key)
	{
		params.remove(key);
	}
	final public boolean isCompleted()
	{
		return completed;
	}
	final public void waitForCompleted() throws InterruptedException
	{
		lock.lock();
		while (!completed)
			isCompleted.await();
		lock.unlock();
	}
	public Object getResult()
	{
		return result;
	}
	
	public int compareTo(HttpTask o) 
	{
		if (priority<o.priority)
			return -1;
		else if (priority>o.priority)
			return 1;
		else if (createTime<o.createTime)
			return -1;
		else if (createTime>o.createTime)
			return 1;
		else
			return 0;
	}
	
	public abstract Object executeHandler() throws Exception;

	public abstract void errorHandler(Exception e);
	
	final public void run()
	{
		try {
			lock.lockInterruptibly();
			result = executeHandler();
			if (params.containsKey("onComplete"))
			{
				OnComplete onComplete = (OnComplete)getParam("onComplete");
				onComplete.onComplete(this.params);
			}
			completed = true;
			isCompleted.signalAll();
		}
		catch (Exception e) {
			errorHandler(e);
		}
		finally {
			lock.unlock();
		}
	}
	
	protected static HttpUriRequest browserEmulation(HttpUriRequest request)
	{
		request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
		request.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
		request.setHeader("Accept-Encoding","gzip, deflate, br");
		request.setHeader("Accept-Language","en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7");
		return request;
	}
	protected static void fetchContent(HttpResponse response,OutStream out) throws InterruptedException,IOException,UnsupportedOperationException
	{
		InputStream in = response.getEntity().getContent();
		byte[] temp = new byte[1024];
		int len,completedSize = 0;
		while ((len=in.read(temp))!=-1)
		{
			out.write(temp,0,len);
			completedSize += len;
			if (Thread.currentThread().isInterrupted())
			{
				out.close();
				throw new InterruptedException(String.valueOf(completedSize));
			}
		}
		out.close();
	}
}
