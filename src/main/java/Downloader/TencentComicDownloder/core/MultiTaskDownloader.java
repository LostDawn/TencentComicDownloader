package Downloader.TencentComicDownloder.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

import Downloader.TencentComicDownloder.HttpTask.GetContentLength;
import Downloader.TencentComicDownloder.HttpTask.HttpTask;

public class MultiTaskDownloader
{
	private String[] urls;
	private String[] files;
	private int[] lengths;
	private int singleTaskThreadNum;
	private NetworkController networkController;
	
	public MultiTaskDownloader(NetworkController networkController,String[] urls,String[] files,int singleTaskThreadNum)
	{
		this.urls = urls;
		this.files = files;
		this.singleTaskThreadNum = singleTaskThreadNum;
		this.networkController = networkController;
		lengths = new int[urls.length];
		Arrays.fill(lengths, -1);
	}
	public MultiTaskDownloader(NetworkController networkController,String[] urls,String[] files)
	{
		this(networkController,urls,files,1);
	}
	public void download() throws InterruptedException, IOException
	{
		if (singleTaskThreadNum>1)
		{
			HttpTask[] taskList = new HttpTask[urls.length];
			for (int i=0;i<urls.length;++i)
			{
				taskList[i] = new GetContentLength(urls[i],networkController);
				networkController.addTask(taskList[i]);
			}
			for (int i=0;i<urls.length;++i)
				taskList[i].waitForCompleted();
			for (int i=0;i<urls.length;++i)
				lengths[i] = (Integer)(taskList[i].getResult());
		}
		for (int i=0;i<urls.length;++i)
			new MultiThreadDownloader(networkController,urls[i],files[i],lengths[i],singleTaskThreadNum).download();
	}
	
}
