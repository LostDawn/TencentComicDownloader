package Downloader.TencentComicDownloder;

import java.util.Vector;

import Downloader.TencentComicDownloder.core.MultiTaskDownloader;
import Downloader.TencentComicDownloder.core.NetworkController;
import Downloader.TencentComicDownloder.core.PageParser;

public class App 
{
	public static void main(String[] args) throws Exception
	{	
		String address = "https://ac.qq.com/ComicView/index/id/532071/cid/1";
		String folder = "data\\";
		
		NetworkController networkController = new NetworkController();
		
		Vector<String> s = PageParser.fetchAllImageUrl(address,networkController);
		System.out.println("Finish fetching image urls ...");
		
		String[] urls = s.toArray(new String[s.size()]);
		String[] filePaths = new String[s.size()];
		for (int i=0;i<filePaths.length;++i)
			filePaths[i] = folder + i + ".jpg";
		
		new MultiTaskDownloader(networkController,urls,filePaths,1).download();
		System.out.println("Finish all tasks!");
	}
}
