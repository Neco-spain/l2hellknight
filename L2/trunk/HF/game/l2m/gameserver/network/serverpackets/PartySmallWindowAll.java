package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.Race;

public class PartySmallWindowAll extends L2GameServerPacket
{
  private int leaderId;
  private int loot;
  private List<PartySmallWindowMemberInfo> members = new ArrayList();

  public PartySmallWindowAll(Party party, Player exclude)
  {
    leaderId = party.getPartyLeader().getObjectId();
    loot = party.getLootDistribution();

    for (Player member : party.getPartyMembers())
      if (member != exclude)
        members.add(new PartySmallWindowMemberInfo(member));
  }

  protected final void writeImpl()
  {
    writeC(78);
    writeD(leaderId);
    writeD(loot);
    writeD(members.size());
    for (PartySmallWindowMemberInfo member : members)
    {
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
      writeD(member.race_id);
      writeD(0);
      writeD(0);

      if (member.pet_id != 0)
      {
        writeD(member.pet_id);
        writeD(member.pet_NpcId);
        writeS(member.pet_Name);
        writeD(member.pet_curHp);
        writeD(member.pet_maxHp);
        writeD(member.pet_curMp);
        writeD(member.pet_maxMp);
        writeD(member.pet_level);
      }
      else {
        writeD(0); }  }  } 
  public static class PartySmallWindowMemberInfo { public String _name;
    public String pet_Name;
    public int _id;
    public int curCp;
    public int maxCp;
    public int curHp;
    public int maxHp;
    public int curMp;
    public int maxMp;
    public int level;
    public int class_id;
    public int race_id;
    public int pet_id;
    public int pet_NpcId;
    public int pet_curHp;
    public int pet_maxHp;
    public int pet_curMp;
    public int pet_maxMp;
    public int pet_level;

    public PartySmallWindowMemberInfo(Player member) { _name = member.getName();
      _id = member.getObjectId();
      curCp = (int)member.getCurrentCp();
      maxCp = member.getMaxCp();
      curHp = (int)member.getCurrentHp();
      maxHp = member.getMaxHp();
      curMp = (int)member.getCurrentMp();
      maxMp = member.getMaxMp();
      level = member.getLevel();
      class_id = member.getClassId().getId();
      race_id = member.getRace().ordinal();

      Summon pet = member.getPet();
      if (pet != null)
      {
        pet_id = pet.getObjectId();
        pet_NpcId = (pet.getNpcId() + 1000000);
        pet_Name = pet.getName();
        pet_curHp = (int)pet.getCurrentHp();
        pet_maxHp = pet.getMaxHp();
        pet_curMp = (int)pet.getCurrentMp();
        pet_maxMp = pet.getMaxMp();
        pet_level = pet.getLevel();
      }
      else {
        pet_id = 0;
      }
    }
  }
}