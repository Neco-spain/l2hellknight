package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.L2GameClient;

public class PartySmallWindowAll extends L2GameServerPacket
{
  private static final String _S__63_PARTYSMALLWINDOWALL = "[S] 4e PartySmallWindowAll";
  private List<L2PcInstance> _partyMembers = new FastList();

  public void setPartyList(List<L2PcInstance> party)
  {
    _partyMembers = party;
  }

  protected final void writeImpl()
  {
    writeC(78);
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    writeD(((L2PcInstance)_partyMembers.get(0)).getObjectId());
    writeD(((L2PcInstance)_partyMembers.get(0)).getParty().getLootDistribution());
    writeD(_partyMembers.size() - 1);

    for (int i = 0; i < _partyMembers.size(); i++)
    {
      L2PcInstance member = (L2PcInstance)_partyMembers.get(i);
      if (member.equals(player))
        continue;
      writeD(member.getObjectId());
      writeS(member.getName());

      writeD((int)member.getCurrentCp());
      writeD(member.getMaxCp());

      writeD((int)member.getCurrentHp());
      writeD(member.getMaxHp());
      writeD((int)member.getCurrentMp());
      writeD(member.getMaxMp());
      writeD(member.getLevel());
      writeD(member.getClassId().getId());
      writeD(0);
      writeD(0);
    }
  }

  public String getType()
  {
    return "[S] 4e PartySmallWindowAll";
  }
}