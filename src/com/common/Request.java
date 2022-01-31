package com.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class Request {
	
   public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
          result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
          result.append("=");
          result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
          result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
          ? resultString.substring(0, resultString.length() - 1)
          : resultString;
    }
	   
	public static String get(String targetURL, Map<String, String> params) throws Exception {
	     String url = targetURL + "?" + getParamsString(params);
	     URL obj = new URL(url);
	     HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	     // optional default is GET
	     con.setRequestMethod("GET");
	     //add request header
	     con.setRequestProperty("User-Agent", "Mozilla/5.0");
	     int responseCode = con.getResponseCode();
	     System.out.println("\nSending 'GET' request to URL : " + url);
	     System.out.println("Response Code : " + responseCode);
	     BufferedReader in = new BufferedReader(
	             new InputStreamReader(con.getInputStream()));
	     String inputLine;
	     StringBuffer response = new StringBuffer();
	     while ((inputLine = in.readLine()) != null) {
	     	response.append(inputLine);
	     }
	     in.close();
	     //print in String
	     System.out.println(response.toString());
	     //Read JSON response and print
	     return response.toString();
	}
}
