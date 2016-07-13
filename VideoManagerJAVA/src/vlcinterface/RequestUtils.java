package vlcinterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Romsahel
 */
public class RequestUtils
{

	private RequestUtils()
	{
		Authenticator.setDefault(new Authenticator()
		{
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication("", "coucou".toCharArray());
			}
		});
	}

	public String sendGet(String requestParameters)
	{
		try {
			return coucou(requestParameters);
		} catch (Exception e) {
			System.err.println(e.getClass() + ": There was an error during connection to VLC Interface");
			e.printStackTrace();
		}
		return "";
	}

	private String coucou(String requestParameters) throws IOException {
		// Sets the authenticator that will be used by the networking code
		// when a proxy or an HTTP server asks for authentication.
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("", "coucou".toCharArray());
			}
		});

		String str = "http://localhost:8080/requests/status.json";
		if (requestParameters != null && requestParameters.length() > 0)
			str += "?" + requestParameters;

		URL url = new URL(str);
		// read text returned by server
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	private String doSendGet(String requestParameters) throws Exception
	{
		String url = "http://localhost:8080/requests/status.json";
		if (requestParameters != null && requestParameters.length() > 0)
			url += "?" + requestParameters;

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

		return response.toString();
	}

	public String sendGetRequest(String requestParameters)
	{
		String result;
//		System.out.println("Requesting: " + requestParameters);
		String endpoint = "http://localhost:8080/requests/status.json";
		if (endpoint.startsWith("http://"))
		{
			// Send data
			String urlStr = endpoint;
			if (requestParameters != null && requestParameters.length() > 0)
				urlStr += "?" + requestParameters;

//			System.out.println(urlStr);
			URL url;
			URLConnection conn;
			BufferedReader rd;
			try
			{
				url = new URL(urlStr);
				conn = url.openConnection();
				// Get the response
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				result = readBuffer(rd);

			} catch (IOException ex)
			{
				return null;
			}

			closeReader(rd);
//			System.out.println("Successful request");
			return result;
		}
		return null;
	}

	private String readBuffer(BufferedReader rd) throws IOException
	{
		String result;
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null)
			sb.append(line);
		result = sb.toString();
		return result;
	}

	private void closeReader(BufferedReader rd)
	{
		try
		{
			rd.close();
		} catch (IOException ex)
		{
			Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public String pathToUrl(final String path)
	{
		String parameter = null;
		try
		{
			String file = URLEncoder.encode(path.replace(" ", "\\ "), "UTF-8");
			final URI f = new File(path).toURI();
			parameter = String.format("command=in_play&input=%s", file.replace("%5C+", "%20"));
		} catch (UnsupportedEncodingException ex)
		{
			Logger.getLogger(VLCController.class.getName()).log(Level.SEVERE, null, ex);
		}
		return parameter;
	}

	// <editor-fold defaultstate="collapsed" desc="Singleton">
	public static RequestUtils getInstance()
	{
		return HttpUtilsHolder.INSTANCE;
	}

	private static class HttpUtilsHolder
	{

		private static final RequestUtils INSTANCE = new RequestUtils();
	}
	// </editor-fold>
}
