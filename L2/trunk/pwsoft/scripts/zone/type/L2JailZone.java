package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.zone.L2ZoneType;

public class L2JailZone extends L2ZoneType
{
  public L2JailZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      character.setInsideZone(256, true);
      character.setInsideZone(1, true);
      ((L2PcInstance)character).sendPacket(SystemMessage.id(SystemMessageId.ENTERED_COMBAT_ZONE));
    }
  }

  protected void onExit(L2Character character)
  {
    if (character.isPlayer())
    {
      character.setInsideZone(256, false);
      character.setInsideZone(1, false);
      ((L2PcInstance)character).sendPacket(SystemMessage.id(SystemMessageId.LEFT_COMBAT_ZONE));
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}