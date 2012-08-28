package l2p.gameserver.utils;

import java.io.File;
import javolution.text.TextBuilder;
import l2p.gameserver.Config;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BBSUtil
{
  private static final Logger _log = LoggerFactory.getLogger(BBSUtil.class);

  public static String BuildMenu()
  {
    TextBuilder menu = new TextBuilder();
    try
    {
      String[] lines = FileUtils.readFileToString(new File("config/community_menu.txt"), "UTF-8").split("\n");
      for (String line : lines)
      {
        if (((line.contains("_bbspage:30-2")) && (!Config.COMMUNITYBOARD_SHOP_ENABLED)) || ((!Config.COMMUNITYBOARD_BUFFER_ENABLED) && (line.contains("_bbsbuff"))) || ((!Config.COMMUNITYBOARD_TELEPORT_ENABLED) && (line.contains("_bbsteleport"))) || ((!Config.COMMUNITYBOARD_CLASSMASTER_ENABLED) && (line.contains("_bbsclassmaster"))))
        {
          continue;
        }

        menu.append(line);
      }
    }
    catch (Exception e)
    {
      _log.error("Error while loading config/announcements.txt!");
    }

    return menu.toString();
  }
}