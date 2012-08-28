package l2p.gameserver.utils;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Files
{
  private static final Logger _log = LoggerFactory.getLogger(Files.class);

  public static void writeFile(String path, String string)
  {
    try
    {
      FileUtils.writeStringToFile(new File(path), string, "UTF-8");
    }
    catch (IOException e)
    {
      _log.error("Error while saving file : " + path, e);
    }
  }

  public static boolean copyFile(String srcFile, String destFile)
  {
    try
    {
      FileUtils.copyFile(new File(srcFile), new File(destFile), false);
      return true;
    }
    catch (IOException e)
    {
      _log.error("Error while copying file : " + srcFile + " to " + destFile, e);
    }

    return false;
  }
}