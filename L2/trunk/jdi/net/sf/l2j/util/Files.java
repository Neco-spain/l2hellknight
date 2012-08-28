package net.sf.l2j.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.logging.Logger;

public class Files
{
  private static Logger _log = Logger.getLogger(Strings.class.getName());

  private static HashMap<String, String> cache = new HashMap();

  public static String read(String name)
  {
    if (name == null) {
      return null;
    }
    File file = new File("./" + name);

    if (!file.exists()) {
      return null;
    }
    String content = null;

    BufferedReader br = null;
    try
    {
      br = new BufferedReader(new UnicodeReader(new FileInputStream(file), "UTF-8"));
      StringBuffer sb = new StringBuffer();
      String s = "";
      while ((s = br.readLine()) != null)
        sb.append(s).append("\n");
      content = sb.toString();
      sb = null;
    }
    catch (Exception e1)
    {
    }
    finally
    {
      try {
        if (br != null)
          br.close();
      }
      catch (Exception e1)
      {
      }
    }
    return content;
  }

  public static String read(File file)
  {
    if (!file.exists()) {
      return null;
    }
    String content = null;

    BufferedReader br = null;
    try
    {
      br = new BufferedReader(new UnicodeReader(new FileInputStream(file), "UTF-8"));
      StringBuffer sb = new StringBuffer();
      String s = "";
      while ((s = br.readLine()) != null)
        sb.append(s).append("\n");
      content = sb.toString();
      sb = null;
    }
    catch (Exception e1)
    {
    }
    finally
    {
      try {
        if (br != null)
          br.close();
      }
      catch (Exception e1)
      {
      }
    }
    return FilesCrypt.getInstance().decrypt(content);
  }

  public static void cacheClean()
  {
    cache = new HashMap();
  }

  public static long lastModified(String name)
  {
    if (name == null) {
      return 0L;
    }
    return new File(name).lastModified();
  }
}