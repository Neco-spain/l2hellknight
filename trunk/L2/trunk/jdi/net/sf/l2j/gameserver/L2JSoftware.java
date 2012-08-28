package net.sf.l2j.gameserver;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.util.Util;

public class L2JSoftware
{
  private static final Logger _log = Logger.getLogger(L2JSoftware.class.getName());

  public static void info()
  {
    Util.printCpuInfo();
    _log.info("-------------------------------------------------------------------------------");
    Util.printOSInfo();
    _log.info("-------------------------------------------------------------------------------");
    _log.info(" #     #####   ##   ####   ####  #### ##### #          #  ###   ####   #####  ");
    _log.info(" #         #    #  #      #    # #      #    #   ##   #  #   #  #   #  #      ");
    _log.info(" #       #      #   ###   #    # ###    #     #  ##  #   #####  ####   ###    ");
    _log.info(" #     #     #  #      #  #    # #      #     ##    ##   #   #  #   #  #      ");
    _log.info(" ##### ##### ####  ####    ####  #      #     #      #   #   #  #    # #####  ");
    _log.info("-------------------------------------------------------------------------------");
    _log.info("                                                                  \t\t  ");
    _log.info("                               www.L2jSoftware.ru    -   Closed                ");
    _log.info("                                 Version = " + Config.VERSION_SERV);
    _log.info("-------------------------------------------------------------------------------");
  }
}