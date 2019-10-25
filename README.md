# TencentComicDownloader
 Tencent Comic Downloader
> ### Target
This project try to download comic images from tencent website
> ### Feature
1. ##### A custom http downloader.
+ You can manage your http request easily. For example, when your IP is banned by tencent, you can change your IP easily in only one function and then continue interrtuped requests   automatically.
+ You can do multiple tasks and each by multiple threads at the same time. It's quite easy to change parameters to fit your net environment.
2. ##### Automatically tencent comic page parse
+ Parse the tencent comic page automatically. Otherwise you can't get image urls through the source html.
> ### How to use
We havn't supply a UI for this downloader. You can assemble code in your project and use the interfaces. Here's an example.
```java
public static void main(String[] args) throws Exception
{	
	//the first comic page
	String address = "https://ac.qq.com/ComicView/index/id/532071/cid/1";
	//where to store result
	String folder = "data\\";
		
	NetworkController networkController = new NetworkController();
		
	//fetch image urls.
	//this function will come to next page automatically after one page complete parsing.
	Vector<String> s = PageParser.fetchAllImageUrl(address,networkController);
	System.out.println("Finish fetching image urls ...");
		
	String[] urls = s.toArray(new String[s.size()]);
	String[] filePaths = new String[s.size()];
	for (int i=0;i<filePaths.length;++i)
		filePaths[i] = folder + i + ".jpg";
	
	//parallelly download all the images
	new MultiTaskDownloader(networkController,urls,filePaths,1).download();
	System.out.println("Finish all tasks!");
}
```