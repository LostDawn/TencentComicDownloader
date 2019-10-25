package Downloader.TencentComicDownloder.core;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import Downloader.TencentComicDownloder.HttpTask.GetContent;
import Downloader.TencentComicDownloder.HttpTask.HttpTask;

public class PageParser
{
	final static private String baseUrl = "https://ac.qq.com";
	final static private String noncePattern = "<script>\\s*window\\[.*\\]\\s*=\\s*([^;]*);\\s*</script>";
	final static private HashMap<String,String> elementMapping;
	final static private String dataPattern = "<script>\\s*var\\s*DATA\\s*=\\s*'([^']*)'";
	final static private String urlPattern = "\"pid\":[^,]*,\"width\":[^,]*,\"height\":[^,]*,\"url\":\"([^\"]*)\"";
	final static private String nextPagePattern = "id=\"mainControlNext\".*href=\"([^\"]*)\"";
	static
	{
		elementMapping = new HashMap<String,String>();
		elementMapping.put("document.children","true");
		elementMapping.put("document.getElementsByTagName('html')","true");
		elementMapping.put("window.Array","true");
	}
	
	//中间那段解码代码是js，从腾讯的js文件中解析的
	final static private String decodeCode = 
			"var W = {DATA:data,nonce:nonce};"+
		    "function Base(){_keyStr=\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\";this.decode=function(c){var a=\"\",b,d,h,f,g,e=0;for(c=c.replace(/[^A-Za-z0-9\\+\\/\\=]/g,\"\");e<c.length;)b=_keyStr.indexOf(c.charAt(e++)),d=_keyStr.indexOf(c.charAt(e++)),f=_keyStr.indexOf(c.charAt(e++)),g=_keyStr.indexOf(c.charAt(e++)),b=b<<2|d>>4,d=(d&15)<<4|f>>2,h=(f&3)<<6|g,a+=String.fromCharCode(b),64!=f&&(a+=String.fromCharCode(d)),64!=g&&(a+=String.fromCharCode(h));return a=_utf8_decode(a)};_utf8_decode=function(c){for(var a=\"\",b=0,d=c1=c2=0;b<c.length;)d=c.charCodeAt(b),128>d?(a+=String.fromCharCode(d),b++):191<d&&224>d?(c2=c.charCodeAt(b+1),a+=String.fromCharCode((d&31)<<6|c2&63),b+=2):(c2=c.charCodeAt(b+1),c3=c.charCodeAt(b+2),a+=String.fromCharCode((d&15)<<12|(c2&63)<<6|c3&63),b+=3);return a}}var B=new Base(),T=W['DA'+'TA'].split(''),N=W['n'+'onc'+'e'],len,locate,str;N=N.match(/\\d+[a-zA-Z]+/g);len=N.length;while(len--){locate=parseInt(N[len])&255;str=N[len].replace(/\\d+/g,'');T.splice(locate,str.length)}T=T.join('');_v=B.decode(T);"+
		    "result = _v;";
	
	public static Vector<String> parseImageUrl(String pageContent)
	{
		Pattern jsPattern = Pattern.compile(noncePattern+"[\\s\\S]*"+dataPattern);
		Matcher matcher = jsPattern.matcher(pageContent);
		if (matcher.find())
		{
			String nonce = matcher.group(1);
			String data = matcher.group(2);
			for (Entry<String, String> entry : elementMapping.entrySet())
				nonce = nonce.replace(entry.getKey(), entry.getValue());
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
	        try {
				nonce = (String)engine.eval(nonce);
			}
			catch (Exception e) {
				System.out.println("Error : javascript analyse --- " + nonce);
				return null;
			}
	        engine.put("nonce",nonce);
			engine.put("data",data);
			try {
				engine.eval(decodeCode);
			}
			catch (Exception e) {
				System.out.println("Not valid tencent comic page.");
				return null;
			}
			String s = (String)engine.get("result");
			jsPattern = Pattern.compile(urlPattern);
			matcher = jsPattern.matcher(s);
			Vector<String> result = new Vector<String>();
			while (matcher.find())
				result.addElement(matcher.group(1));
			for (int i=0;i<result.size();++i)
				result.set(i, result.get(i).replace("\\/","/"));
			return result;
		}
		else
		{
			System.out.println("Not valid tencent comic page.");
			return null;
		}
	}
	
	public static String getNextPage(String pageContent)
	{
		String nextPage;
		Pattern jsPattern = Pattern.compile(nextPagePattern);
		Matcher matcher = jsPattern.matcher(pageContent);
		if (matcher.find())
		{
			nextPage = matcher.group(1);
			if (nextPage.equals("javascript:void(0)"))
				nextPage = null;
			else
				nextPage = baseUrl + nextPage;
		}
		else
			nextPage = null;
		return nextPage;
	}
	
	public static Vector<String> fetchAllImageUrl(String address,NetworkController networkController) throws InterruptedException
	{
		Vector<String> s = new Vector<String>();
		while (address!=null)
		{
			HttpTask getPageContent = new GetContent(address,networkController);
			networkController.addTask(getPageContent);
			getPageContent.waitForCompleted();
			String pageContent = (String)getPageContent.getResult();
			s.addAll(parseImageUrl(pageContent));
			
			address = getNextPage(pageContent);
		}
		return s;
	}
	
}
