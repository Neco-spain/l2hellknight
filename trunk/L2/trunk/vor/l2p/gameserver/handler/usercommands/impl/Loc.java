package l2p.gameserver.handler.usercommands.impl;

import java.util.Map;
import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.instancemanager.MapRegionManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.mapregion.RestartArea;
import l2p.gameserver.templates.mapregion.RestartPoint;

public class Loc
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 0 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (COMMAND_IDS[0] != id) {
      return false;
    }
    RestartArea ra = (RestartArea)MapRegionManager.getInstance().getRegionData(RestartArea.class, activeChar);
    int msgId = ra != null ? ((RestartPoint)ra.getRestartPoint().get(activeChar.getRace())).getMsgId() : 0;
    if (msgId > 0)
      activeChar.sendPacket(new SystemMessage(msgId).addNumber(activeChar.getX()).addNumber(activeChar.getY()).addNumber(activeChar.getZ()));
    else {
      activeChar.sendPacket(new SystemMessage(1983).addString("Current location : " + activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ()));
    }
    return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}