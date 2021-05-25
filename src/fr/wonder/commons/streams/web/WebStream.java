package fr.wonder.commons.streams.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WebStream {
	
	private final Map<String, String> properties = new HashMap<>();
	
	public WebStream setProperty(String key, String value) {
		properties.put(key, value);
		return this;
	}
	
	public WebStream setUserProperties() {
		setProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		setProperty("Accept-language", "en-US,en;");
		return this;
	}
	
	public String open(String url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		for(Entry<String, String> property : properties.entrySet())
			con.setRequestProperty(property.getKey(), property.getValue());
		int response = con.getResponseCode();
		if(response != 200)
			throw new IOException("Could not fetch from url, got response code " + response);
		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("UTF-8")));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null)
			sb.append(line);
		reader.close();
		return sb.toString();
	}
	
}
