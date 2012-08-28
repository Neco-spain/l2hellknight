package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Summon;
import l2m.gameserver.utils.Location;

public class PetStatusUpdate extends L2GameServerPacket
{
  private int type;
  private int obj_id;
  private int level;
  private int maxFed;
  private int curFed;
  private int maxHp;
  private int curHp;
  private int maxMp;
  private int curMp;
  private long exp;
  private long exp_this_lvl;
  private long exp_next_lvl;
  private Location _loc;
  private String title;

  public PetStatusUpdate(Summon summon)
  {
    type = summon.getSummonType();
    obj_id = summon.getObjectId();
    _loc = summon.getLoc();
    title = summon.getTitle();
    curHp = (int)summon.getCurrentHp();
    maxHp = summon.getMaxHp();
    curMp = (int)summon.getCurrentMp();
    maxMp = summon.getMaxMp();
    curFed = summon.getCurrentFed();
    maxFed = summon.getMaxFed();
    level = summon.getLevel();
    exp = summon.getExp();
    exp_this_lvl = summon.getExpForThisLevel();
    exp_next_lvl = summon.getExpForNextLevel();
  }

  protected final void writeImpl()
  {
    writeC(182);
    writeD(type);
    writeD(obj_id);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeS(title);
    writeD(curFed);
    writeD(maxFed);
    writeD(curHp);
    writeD(maxHp);
    writeD(curMp);
    writeD(maxMp);
    writeD(level);
    writeQ(exp);
    writeQ(exp_this_lvl);
    writeQ(exp_next_lvl);
  }
}