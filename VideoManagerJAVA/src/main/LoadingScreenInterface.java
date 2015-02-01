/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author Romsahel
 */
public class LoadingScreenInterface
{

	private Thread thread = null;
	public static final String HTML_CANCEL = "document.getElementsByClassName(\"button-block\")[0]";

	public LoadingScreenInterface()
	{
	}

	public void cancelThread()
	{
		if (thread != null)
		{
			System.out.println("Cancelling thread");
			thread.interrupt();
			thread = null;
			MainController.stopLoading();
		}
	}

	/**
	 * @param thread the thread to set
	 */
	public void setThread(Thread thread)
	{
		this.thread = thread;
	}
}
