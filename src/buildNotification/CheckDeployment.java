/**
 * 
 */
package buildNotification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tools.ant.DefaultLogger;


/**
 * Simple utility to verify that a web application host is
 * running.  This will show a pop up window and beep.
 * <br>
 * Supply hostname, delay before starting to check, interval between each check, timeout
 * <br>
 * <br>
 * Example:
 * <br>
 * <pre>http://localhost:8080 1000 2000 5000</pre>
 * Start checking localhost after 1 second and check every 2 seconds, stop checking after 5 seconds
 * @author slarson@twia.org
 * @since May 19, 2015
 * 
 */
public class CheckDeployment extends DefaultLogger
{

   private String userToNotify;
   private String url;
   private double delay;
   private double interval;
   private HttpClient client;
   private GetMethod method; // We should only need to do GETs
   private double stop;
   private DateFormat formatter;


   /**
    * @param userToNotify
    * @param url
    * @param delay
    * @param interval
    */
   public CheckDeployment(String userToNotify, String url, String delay, String interval, String stop)
   {
      super();
      this.userToNotify = userToNotify;
      this.url = url;
      this.delay = Double.parseDouble(delay) * 1000.0d;
      this.interval = Double.parseDouble(interval)* 1000.0d;
      this.stop = Double.parseDouble(stop)* 1000.0d;
      this.client = new HttpClient();
      this.method = new GetMethod(url);
      this.formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
      System.out.println(this + "\n");
   }

   public CheckDeployment(String url, String delay, String interval, String stop)
   {
      this(getCurrentUserName(), url, delay, interval, stop);
   }


   /**
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception
   {

      CheckDeployment check = null;
      if (args.length == 5)
      {
         check = new CheckDeployment(args[0], args[1], args[2], args[3], args[4]);
      }
      else if (args.length == 4)
      {
         check = new CheckDeployment(args[0], args[1], args[2], args[3]);
      }
      else
      {
         throw new IllegalStateException("must supply 4 or 5 parameters!");
      }

      check.run();
   }

   private void run() throws Exception
   {
      int statusCode = 0;
      boolean keepTrying = true;
      long startTime = System.currentTimeMillis();

      // wait until delay then keep calling executeMethod until we timeout??
      Thread.sleep((long)delay);

      long endTime = startTime + (long)this.stop;
      long currentTime = System.currentTimeMillis();
      Date startDateTime = new Date(startTime);
      Date currentDateTime = new Date(currentTime);
      Date endDateTime = new Date(endTime);

      while (keepTrying && currentTime < endTime)
      {
         currentTime = System.currentTimeMillis();
         try
         {
            statusCode = client.executeMethod(method);
         }
         catch (SocketException e)
         {
            System.err.println("Error:" + e.getMessage() + ". Keep trying...");
            Thread.sleep((long)interval);
            continue;
         }
         if (statusCode == HttpStatus.SC_OK)
         {
            keepTrying = false;
            System.out.println("\n\nApplication Ready!:\n\n" + method.getResponseBodyAsString());
            showMessage(" " + url + " is ready!");
            break;
         }
         else
         {
            System.out.println("Error:" + statusCode + " Error body:" + method.getResponseBodyAsString());
         }

         method.releaseConnection();
      }

      //System.out.println("startTime:" + startTime + " endTime:" + endTime + " currentTime:" + currentTime);
      System.out.println("startTime:" + formatter.format(startDateTime) + " endTime:" + formatter.format(endDateTime));

      if (keepTrying == true && currentTime >= endTime)
      {
         String message = " ERROR: " + url + " exceeded " + ((currentTime - startTime) / 1000.0f) + " secs";
         System.err.println(message);
         showMessage(message);
      }

      System.exit(0);
   }

   private static String getCurrentUserName()
   {
      Runtime run = Runtime.getRuntime();
      Process pr = null;
      String userName = null;
      try
      {
         // pr = run.exec("echo ");
         pr = run.exec("whoami ");
         BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
         String output = input.readLine();
         userName = output.substring(output.indexOf("\\") + 1);
         System.out.println("Current User:"+userName);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      try
      {
         pr.waitFor();
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }

      return userName;
   }

   private void showMessage(String message)

   {
      if (userToNotify != null && userToNotify.length() > 0)
      {
         Runtime run = Runtime.getRuntime();
         Process pr = null;
         try
         {
            pr = run.exec("msg /V " + userToNotify + " "/* add space to make dos command recognize username */+ message);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
         try
         {
            pr.waitFor();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         System.out.println("NO user supplied.  Just printing output to console!");
      }

      System.out.println("buildFinished() - complete.");
   }


   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("CheckDeployment [userToNotify=");
      builder.append(userToNotify);
      builder.append(", url=");
      builder.append(url);
      builder.append(", delay=");
      builder.append(delay);
      builder.append(", interval=");
      builder.append(interval);
      builder.append(", client=");
      builder.append(client);
      builder.append(", method=");
      builder.append(method);
      builder.append(", stop=");
      builder.append(stop);
      builder.append("]");
      return builder.toString();
   }
}
