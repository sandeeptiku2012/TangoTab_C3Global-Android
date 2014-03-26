package com.tangotab.core.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.tangotab.core.ex.TangoTabException;

/**
 * Util class will be used for Rest service call.
 * 
 * @author Dillip.Lenka
 *
 */
public class RestServiceUtil 
{
	/**
	 * A generic method to execute all of HTTP Get request 
	 * 
	 * @param {@link HttpGet}
	 * 
	 * @return {@link HttpResponse}
	 */
	public String executeHTTPGet(HttpGet get) throws TangoTabException
	{
		//HTTP Client Library
		HttpClient httpClient = new DefaultHttpClient();
				
		try	{
			ResponseHandler<String> responseHandler = new BasicResponseHandler(); 
			String responseBody = httpClient.execute(get, responseHandler);	
			return responseBody;
		} 
		catch (Exception e) 
		{
		    Log.e(getClass().getSimpleName(), "HTTP protocol error", e);
		    throw new TangoTabException(this.getClass().getName(),"executeHTTPGet",e); 
		} 
	}
}
