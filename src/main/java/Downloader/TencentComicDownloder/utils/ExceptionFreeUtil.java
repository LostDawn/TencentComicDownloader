package Downloader.TencentComicDownloder.utils;

public class ExceptionFreeUtil 
{
	public static void sleepRandomTime(int maxSleepTime)
	{
		sleep((int)(Math.random()*maxSleepTime));
	}
	
	public static void sleep(long time)
	{
		try {
			Thread.sleep(time);
		}
		catch (Exception e) {
			;
		}
	}

}
