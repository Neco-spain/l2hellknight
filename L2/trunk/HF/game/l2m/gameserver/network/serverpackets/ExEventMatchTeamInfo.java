package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.Race;

public class ExEventMatchTeamInfo extends L2GameServerPacket
{
  private int leader_id;
  private int loot;
  private List<EventMatchTeamInfo> members = new ArrayList();

  public ExEventMatchTeamInfo(List<Player> party, Player exclude)
  {
    leader_id = ((Player)party.get(0)).getObjectId();
    loot = ((Player)party.get(0)).getParty().getLootDistribution();

    for (Player member : party)
      if (!member.equals(exclude))
        members.add(new EventMatchTeamInfo(member));
  }

  protected void writeImpl()
  {
    writeEx(28); } 
  public static class EventMatchTeamInfo { public String _name;
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

    public EventMatchTeamInfo(Player member) { _name = member.getName();
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