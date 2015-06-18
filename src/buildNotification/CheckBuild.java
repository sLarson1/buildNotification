package buildNotification;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.StringUtils;



/**
 * Utility class to simply alert the user with a system
 * generated popup box with the status of the build.
 * Usage:<br>
 * <pre>
 * ant [some build target] -DuserName=[some Active directory username] -listener buildNotification.CheckBuild
 * </pre>
 * @author slarson@twia.org
 * @since Mar 14, 2015
 *
 */
public class CheckBuild extends DefaultLogger
{

   private String userToNotify;
   /** Time of the start of the build */
   private long startTime = System.currentTimeMillis();   
   
   /**
    * 
    */
   public CheckBuild()
   {
      this.setOutputPrintStream(System.out);
      this.setErrorPrintStream(System.err);
   }  
      
   protected String getTimestamps() {
      StringBuilder sb = new StringBuilder();
      Date date = new Date(startTime);
      DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
      return "Start Time: "+formatter.format(date) +"\nEnd Time:   "+getTimestamp();
  }      
   
   private String getCurrentUserName()
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
   
   /**
    * Responds to a build being started by just remembering the current time.
    *
    * @param event Ignored.
    */
   public void buildStarted(BuildEvent event) {
       startTime = System.currentTimeMillis();
   }   
   
   /* (non-Javadoc)
    * @see org.apache.tools.ant.DefaultLogger#buildFinished(org.apache.tools.ant.BuildEvent)
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public void buildFinished(BuildEvent event)
   {
      super.buildFinished(event);
      out.println(getTimestamps());
      
      Project project = event.getProject();
      Hashtable properties = project.getProperties();   
      Map map = new TreeMap(properties);      
      
      if(Project.MSG_INFO <= msgOutputLevel) {      
         for(Object key : map.keySet()) {
            out.println(key +":"+properties.get(key));
         }            
      }
      
      boolean success = (event.getException() == null);
      String prefix = success ? "success" : "       !!!! FAILURE !!!!";
      
      userToNotify=(String)map.get("userName");
      if(userToNotify==null) {
         userToNotify = getCurrentUserName();
      }
      
      // play beep too
      Toolkit.getDefaultToolkit().beep();

      if(userToNotify!=null && userToNotify.length() > 0) {
         Runtime run = Runtime.getRuntime();
         Process pr = null;
         try
         {
            pr = run.exec( "msg /V " +userToNotify +" build "+prefix );
         }
         catch (IOException e)
         {
            err.println(StringUtils.getStackTrace(e));
         }
         try
         {
            pr.waitFor();
         }
         catch (InterruptedException e)
         {
            err.println(StringUtils.getStackTrace(e));
         }      
      }else {
         out.println("NO user supplied.  Just printing output to console!");
      }      
   }

   /* (non-Javadoc)
    * @see org.apache.tools.ant.DefaultLogger#targetFinished(org.apache.tools.ant.BuildEvent)
    */
   @Override
   public void targetFinished(BuildEvent event)
   {
      // TODO Auto-generated method stub
      super.targetFinished(event);
   }

   /* (non-Javadoc)
    * @see org.apache.tools.ant.DefaultLogger#taskFinished(org.apache.tools.ant.BuildEvent)
    */
   @Override
   public void taskFinished(BuildEvent event)
   {
      // TODO Auto-generated method stub
      super.taskFinished(event);
   }   
}
