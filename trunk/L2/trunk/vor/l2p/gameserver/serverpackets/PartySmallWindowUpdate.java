package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
  private int obj_id;
  private int class_id;
  private int level;
  private int curCp;
  private int maxCp;
  private int curHp;
  private int maxHp;
  private int curMp;
  private int maxMp;
  private String obj_name;

  public PartySmallWindowUpdate(Player member)
  {
    obj_id = member.getObjectId();
    obj_name = member.getName();
    curCp = (int)member.getCurrentCp();
    maxCp = member.getMaxCp();
    curHp = (int)member.getCurrentHp();
    maxHp = member.getMaxHp();
    curMp = (int)member.getCurrentMp();
    maxMp = member.getMaxMp();
    level = member.getLevel();
    class_id = member.getClassId().getId();
  }

  protected final void writeImpl()
  {
    writeC(82);

    writeD(obj_id);
    writeS(obj_name);
    writeD(curCp);
    writeD(maxCp);
    writeD(curHp);
    writeD(maxHp);
    writeD(curMp);
    writeD(maxMp);
    writeD(level);
    writeD(class_id);
  }
}