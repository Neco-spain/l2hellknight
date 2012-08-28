package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PartyMemberPosition extends L2GameServerPacket
{
  private L2Party _party;

  public PartyMemberPosition(L2PcInstance actor)
  {
    _party = actor.getParty();
  }

  protected void writeImpl()
  {
    writeC(167);
    writeD(_party.getMemberCount());

    for (L2PcInstance pm : _party.getPartyMembers())
    {
      if (pm == null)
        continue;
      writeD(pm.getObjectId());
      writeD(pm.getX());
      writeD(pm.getY());
      writeD(pm.getZ());
    }
  }

  public String getType()
  {
    return null;
  }
}