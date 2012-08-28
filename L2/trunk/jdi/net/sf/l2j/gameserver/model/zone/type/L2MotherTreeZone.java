package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2MotherTreeZone extends L2ZoneType
{
  public L2MotherTreeZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)character;

      if (player.isInParty())
      {
        for (L2PcInstance member : player.getParty().getPartyMembers()) {
          if (member.getRace() != Race.elf) return;
        }
      }
      player.setInsideZone(8, true);
      player.sendPacket(new SystemMessage(SystemMessageId.ENTER_SHADOW_MOTHER_TREE));
    }
  }

  protected void onExit(L2Character character)
  {
    if (((character instanceof L2PcInstance)) && (character.isInsideZone(8)))
    {
      character.setInsideZone(8, false);
      ((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.EXIT_SHADOW_MOTHER_TREE));
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}