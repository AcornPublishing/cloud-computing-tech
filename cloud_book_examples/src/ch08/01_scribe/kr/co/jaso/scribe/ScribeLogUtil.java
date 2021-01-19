package kr.co.jaso.scribe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScribeLogUtil {
  static Log LOG = LogFactory.getLog("kr.co.jaso.scribe"); 
  
  public static void info(String message) {
    LOG.info(message);
  }
}