package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaceManagerInstance;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;

public class RaceManagerKnownList extends NpcKnownList
{
  public RaceManagerKnownList(L2RaceManagerInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper) {
    return super.addKnownObject(object, dropper);
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) return false;

    if (object.isPlayer())
    {
      DeleteObject obj = null;
      for (int i = 0; i < 8; i++)
      {
        obj = new DeleteObject(net.sf.l2j.gameserver.MonsterRace.getInstance().getMonsters()[i]);
        object.getPlayer().sendPacket(obj);
      }
    }

    return true;
  }

  public L2RaceManagerInstance getActiveChar()
  {
    return (L2RaceManagerInstance)super.getActiveChar();
  }
}