package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.zone.L2ZoneType;

public class L2ElfTreeZone extends L2ZoneType
{
  public L2ElfTreeZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)character;

      if (player.getRace() != Race.elf) {
        return;
      }
      player.setInsideZone(8, true);
      player.setInElfTree(true);
      player.sendPacket(SystemMessage.id(SystemMessageId.ENTER_SHADOW_MOTHER_TREE));
    }
  }

  protected void onExit(L2Character character)
  {
    if ((character.isPlayer()) && (character.isInsideZone(8)))
    {
      L2PcInstance player = (L2PcInstance)character;

      character.setInsideZone(8, false);
      player.setInElfTree(false);
      player.sendPacket(SystemMessage.id(SystemMessageId.EXIT_SHADOW_MOTHER_TREE));
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}