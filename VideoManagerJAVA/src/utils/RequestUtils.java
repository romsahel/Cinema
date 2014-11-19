package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import videomanagerjava.VLCController;

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

	public String sendGetRequest(String requestParameters)
	{
		String result;
		String endpoint = "http://localhost:8080/requests/status.json";
		if (endpoint.startsWith("http://"))
		{
			// Send data
			String urlStr = endpoint;
			if (requestParameters != null && requestParameters.length() > 0)
				urlStr += "?" + requestParameters;

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
				System.err.println(ex.getClass() + ": There was an error during connection to VLC Interface");
				return null;
			}

			closeReader(rd);

			System.out.println(result);
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
