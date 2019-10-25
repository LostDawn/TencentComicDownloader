package Downloader.TencentComicDownloder.core;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import Downloader.TencentComicDownloder.HttpTask.HttpTask;
import Downloader.TencentComicDownloder.utils.IPControlUtil;
import Downloader.TencentComicDownloder.utils.Log;

public class NetworkController
{
	private int state;
	//0:normal
	//1:shutdown
	
	private ThreadPoolExecutor threadPoolExecutor;
	private PriorityBlockingQueue<HttpTask> taskQueue;
	private int waitingThreadNum,maxThreadNum;
	private long timeout;
	private ReentrantLock lock;
	
	private class CustomSynchronousQueue extends SynchronousQueue<Runnable>
	{
		private static final long serialVersionUID = 1L;
		@Override
		public Runnable poll()
		{
			Runnable result;
			if (state!=0 || (result = (Runnable)(taskQueue.poll()))==null)
				try {
					lock.lock();
					++waitingThreadNum;
					lock.unlock();
					return super.poll();
				}
				finally {
					lock.lock();
					--waitingThreadNum;
					lock.unlock();
				}
			else
				return result;
		}
		@Override
		public Runnable poll(long timeout,TimeUnit unit) throws InterruptedException
		{
			if (threadPoolExecutor.getPoolSize()>maxThreadNum)
				throw new InterruptedException();
			Runnable result;
			if (state!=0 || (result = (Runnable)(taskQueue.poll()))==null)
				try {
					lock.lock();
					++waitingThreadNum;
					lock.unlock();
					return super.poll(timeout,unit);
				}
				finally {
					lock.lock();
					--waitingThreadNum;
					lock.unlock();
				}
			else
				return result;
		}
	}
	
	public NetworkController()
	{
		this(10,60L,new PriorityBlockingQueue<HttpTask>());
	}
	public NetworkController(int maxThreadNum,long timeout,PriorityBlockingQueue<HttpTask> queue)
	{
		threadPoolExecutor = new ThreadPoolExecutor(0, maxThreadNum,timeout,TimeUnit.SECONDS,
				new CustomSynchronousQueue(),Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy());
		threadPoolExecutor.allowCoreThreadTimeOut(true);
		
		state = 0;
		taskQueue = queue;
		waitingThreadNum = 0;
		this.timeout = timeout;
		this.maxThreadNum = maxThreadNum;
		lock = new ReentrantLock();
	}
	
	public void addTask(HttpTask newTask)
	{
		newTask.setCreateTime(System.currentTimeMillis());
		lock.lock();
		try {
			//if there exists an available thread
			if (state==0 && (waitingThreadNum>0 || threadPoolExecutor.getPoolSize()<maxThreadNum))
				threadPoolExecutor.execute(newTask);
			//else put the task into queue, waiting for threadPoolExecutor to poll task
			else
				taskQueue.add(newTask);
		}
		finally {
			lock.unlock();
		}
	}
	
	public void needChangeIP()
	{
		//how to change your IP is a custom problem.
		//my solution is to change my MAC and then get a new IP from DHCP server.
		//you can realize it by yourself.
		
		/*if (state==1)
			return;
		state = 1;
		new Thread()
		{
			@Override
			public void run()
			{
				List<Runnable> x = threadPoolExecutor.shutdownNow();
				try {
					threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS);
				}
				catch (InterruptedException e) {
					;
				}
				try {
					IPControlUtil.changeIP();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				state = 0;
				restart();
			}
		}.start();*/
	}
	
	private void restart()
	{
		threadPoolExecutor = new ThreadPoolExecutor(0, maxThreadNum,timeout,TimeUnit.SECONDS,
				new CustomSynchronousQueue(),Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy());
		threadPoolExecutor.allowCoreThreadTimeOut(true);
		for (int i=0;i<maxThreadNum;++i)
			if (taskQueue.isEmpty())
				break;
			else
				threadPoolExecutor.execute(taskQueue.poll());
	}

}
