package l2p.gameserver.handler.usercommands.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import l2p.gameserver.data.xml.holder.InstantZoneHolder;
import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class InstanceZone
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 114 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (COMMAND_IDS[0] != id) {
      return false;
    }
    if (activeChar.getActiveReflection() != null) {
      activeChar.sendPacket(new SystemMessage2(SystemMsg.INSTANT_ZONE_CURRENTLY_IN_USE_S1).addInstanceName(activeChar.getActiveReflection().getInstancedZoneId()));
    }

    boolean noLimit = true;
    boolean showMsg = false;
    for (Iterator i$ = activeChar.getInstanceReuses().keySet().iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      int limit = InstantZoneHolder.getInstance().getMinutesToNextEntrance(i, activeChar);
      if (limit > 0)
      {
        noLimit = false;
        if (!showMsg)
        {
          activeChar.sendPacket(SystemMsg.INSTANCE_ZONE_TIME_LIMIT);
          showMsg = true;
        }
        activeChar.sendPacket(((SystemMessage2)((SystemMessage2)new SystemMessage2(SystemMsg.S1_WILL_BE_AVAILABLE_FOR_REUSE_AFTER_S2_HOURS_S3_MINUTES).addInstanceName(i)).addInteger(limit / 60)).addInteger(limit % 60));
      }
    }
    if (noLimit) {
      activeChar.sendPacket(SystemMsg.THERE_IS_NO_INSTANCE_ZONE_UNDER_A_TIME_LIMIT);
    }
    return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}