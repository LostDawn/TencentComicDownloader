package Downloader.TencentComicDownloder.HttpTask;

import java.util.Map;

public interface OnComplete 
{
	abstract void onComplete(Map<String,Object> params);
}
