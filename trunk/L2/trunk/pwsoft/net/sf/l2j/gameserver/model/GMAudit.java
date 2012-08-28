package net.sf.l2j.gameserver.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;

public class GMAudit
{
  private static final Logger _log = Logger.getLogger("gmaudit");

  public static void auditGMAction(String gmName, String action, String target, String params) {
    if (Config.GMAUDIT)
    {
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
      String today = formatter.format(new Date());

      _log.log(Level.INFO, today + ">" + gmName + ">" + action + ">" + target + ">" + params);
    }
  }
}