package scripts.zone.type;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import scripts.zone.L2ZoneType;

public class L2DismountZone extends L2ZoneType
{
  public L2DismountZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
    {
      L2PcInstance activeChar = (L2PcInstance)character;
      activeChar.setInDismountZone(true);
      if (activeChar.isMounted())
      {
        if (activeChar.setMountType(0))
        {
          if (activeChar.isFlying()) {
            activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
          }
          Ride dismount = new Ride(activeChar.getObjectId(), 0, 0);
          activeChar.broadcastPacket(dismount);
          activeChar.setMountObjectID(0);
        }
        return;
      }
    }
  }

  protected void onExit(L2Character character)
  {
    if (character.isPlayer())
    {
      L2PcInstance activeChar = (L2PcInstance)character;
      activeChar.setInDismountZone(false);
    }
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}