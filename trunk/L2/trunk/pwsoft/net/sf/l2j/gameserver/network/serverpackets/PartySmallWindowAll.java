package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.network.L2GameClient;

public class PartySmallWindowAll extends L2GameServerPacket
{
  private int leader_id;
  private int loot;
  private FastList<MemberInfo> _members = new FastList();
  private FastTable<L2PcInstance> _partyMembers;

  public PartySmallWindowAll(FastTable<L2PcInstance> party)
  {
    if (((L2PcInstance)party.get(0)).getParty() == null) {
      return;
    }
    leader_id = ((L2PcInstance)party.get(0)).getObjectId();
    loot = ((L2PcInstance)party.get(0)).getParty().getLootDistribution();

    _partyMembers = party;
  }

  public final void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    for (L2PcInstance member : _partyMembers) {
      if ((member == null) || (member.equals(player))) {
        continue;
      }
      _members.add(new MemberInfo(member.getName(), member.getObjectId(), (int)member.getCurrentCp(), member.getMaxCp(), (int)member.getCurrentHp(), member.getMaxHp(), (int)member.getCurrentMp(), member.getMaxMp(), member.getLevel(), member.getClassId().getId(), member.getRace().ordinal()));
    }
  }

  protected final void writeImpl()
  {
    writeC(78);
    writeD(leader_id);
    writeD(loot);
    writeD(_members.size());

    FastList.Node n = _members.head(); for (FastList.Node end = _members.tail(); (n = n.getNext()) != end; ) {
      MemberInfo member = (MemberInfo)n.getValue();
      if (member == null)
      {
        continue;
      }
      writeD(member._id);
      writeS(member._name);
      writeD(member.curCp);
      writeD(member.maxCp);
      writeD(member.curHp);
      writeD(member.maxHp);
      writeD(member.curMp);
      writeD(member.maxMp);
      writeD(member.level);
      writeD(member.class_id);
      writeD(0);
      writeD(member.race);
    }
  }

  public void gcb()
  {
    _members.clear(); } 
  static class MemberInfo { public String _name;
    public int _id;
    public int curCp;
    public int maxCp;
    public int curHp;
    public int maxHp;
    public int curMp;
    public int maxMp;
    public int level;
    public int class_id;
    public int race;

    public MemberInfo(String __name, int __id, int _curCp, int _maxCp, int _curHp, int _maxHp, int _curMp, int _maxMp, int _level, int _class_id, int race) { _name = __name;
      _id = __id;
      curCp = _curCp;
      maxCp = _maxCp;
      curHp = _curHp;
      maxHp = _maxHp;
      curMp = _curMp;
      maxMp = _maxMp;
      level = _level;
      class_id = _class_id;
      this.race = race;
    }
  }
}