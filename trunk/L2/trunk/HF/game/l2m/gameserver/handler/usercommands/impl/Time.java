package l2m.gameserver.handler.usercommands.impl;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import l2m.gameserver.Config;
import l2m.gameserver.GameTimeController;
import l2m.gameserver.handler.usercommands.IUserCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;

public class Time
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 77 };

  private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
  private static final SimpleDateFormat sf = new SimpleDateFormat("H:mm");

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (COMMAND_IDS[0] != id) {
      return false;
    }
    int h = GameTimeController.getInstance().getGameHour();
    int m = GameTimeController.getInstance().getGameMin();
    SystemMessage sm;
    SystemMessage sm;
    if (GameTimeController.getInstance().isNowNight())
      sm = new SystemMessage(928);
    else
      sm = new SystemMessage(927);
    sm.addString(df.format(h)).addString(df.format(m));

    activeChar.sendPacket(sm);

    if (Config.ALT_SHOW_SERVER_TIME) {
      activeChar.sendMessage(new CustomMessage("usercommandhandlers.Time.ServerTime", activeChar, new Object[] { sf.format(new Date(System.currentTimeMillis())) }));
    }
    return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }

  static
  {
    df.setMinimumIntegerDigits(2);
  }
}