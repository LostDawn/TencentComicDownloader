package Downloader.TencentComicDownloder.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;

import Downloader.TencentComicDownloder.HttpTask.Download;
import Downloader.TencentComicDownloder.HttpTask.HttpTask;
import Downloader.TencentComicDownloder.HttpTask.OnComplete;

public class MultiThreadDownloader implements OnComplete
{
	private String url;
	private String filePath;
	private File file;
	private int threadNum,totalLen;
	private NetworkController networkController;
	private boolean[] completed;
	
	public MultiThreadDownloader(NetworkController networkController,
			String url,String filePath,int totalLen,int threadNum) throws IOException
	{
		this.networkController = networkController;
		this.url = url;
		this.filePath = filePath;
		file = null;
		this.threadNum = threadNum;
		this.totalLen = totalLen;
		completed = null;
	}
	
	public void download() throws IOException, InterruptedException 
	{
		String tempFilePath = filePath+".temp";
		file = new File(tempFilePath);
		
        int threadLen;
        if (totalLen>0)
        {
        	threadLen = (totalLen + threadNum - 1) / threadNum;
        	RandomAccessFile raf = new RandomAccessFile(file, "rws");
        	raf.setLength(totalLen);
            raf.close();
        }
        else
        {
        	threadLen = -1;
        	threadNum = 1;
        }
        completed = new boolean[threadNum];
        
        for (int i = 0; i < threadNum; i++)
        {
        	HttpTask sliceTask = new Download(url,i*threadLen,tempFilePath,networkController);
        	if (threadLen>0)
        		sliceTask.setParam("end", i*threadLen+threadLen-1);
        	sliceTask.setParam("order", i);
        	sliceTask.setParam("onComplete", this);
        	networkController.addTask(sliceTask);
        }
    }
	public void onComplete(Map<String,Object> params)
	{
		completed[(Integer)(params.get("order"))] = true;
		for (boolean sliceCompleted : completed)
			if (!sliceCompleted)
				return;
		File resultFile = new File(filePath);
        if (resultFile.exists())
        	resultFile.delete();
        file.renameTo(resultFile);
	}
	
	public static void main(String[] args) throws IOException
	{
		String address = "https://ac.qq.com/ComicView/index/id/544425/cid/1";
        String folder = "D:\\workspace\\video downloader\\data\\";
        HttpGet s = new HttpGet(address);
        System.out.println(s.getURI().getHost()+s.getURI().getPath());
	}
}
