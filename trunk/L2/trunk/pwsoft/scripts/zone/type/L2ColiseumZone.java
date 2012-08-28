package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.zone.L2ZoneType;

public class L2ColiseumZone extends L2ZoneType
{
  public L2ColiseumZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(1, true);

    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      player.setInColiseum(true);
      player.sendPacket(SystemMessage.id(SystemMessageId.ENTERED_COMBAT_ZONE));
    }
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(1, false);

    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      player.setInColiseum(false);
      player.sendPacket(SystemMessage.id(SystemMessageId.LEFT_COMBAT_ZONE));
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}