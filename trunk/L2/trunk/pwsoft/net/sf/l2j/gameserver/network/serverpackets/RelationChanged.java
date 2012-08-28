package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RelationChanged extends L2GameServerPacket
{
  public static final int RELATION_PVP_FLAG = 2;
  public static final int RELATION_HAS_KARMA = 4;
  public static final int RELATION_LEADER = 128;
  public static final int RELATION_INSIEGE = 512;
  public static final int RELATION_ATTACKER = 1024;
  public static final int RELATION_ALLY = 2048;
  public static final int RELATION_ENEMY = 4096;
  public static final int RELATION_MUTUAL_WAR = 32768;
  public static final int RELATION_1SIDED_WAR = 65536;
  private int _objId;
  private int _relation;
  private int _autoAttackable;
  private int _karma;
  private int _pvpFlag;

  public RelationChanged(L2Character cha, int relation, boolean autoattackable)
  {
    _objId = cha.getObjectId();
    _relation = relation;
    _autoAttackable = (autoattackable ? 1 : 0);

    if (cha.isPlayer()) {
      _karma = cha.getKarma();
      _pvpFlag = cha.getPvpFlag();
    } else if (cha.isL2Summon()) {
      _karma = cha.getOwner().getKarma();
      _pvpFlag = cha.getOwner().getPvpFlag();
    }

    if (Config.FREE_PVP)
      _pvpFlag = 0;
  }

  protected final void writeImpl()
  {
    writeC(206);
    writeD(_objId);
    writeD(_relation);
    writeD(_autoAttackable);
    writeD(_karma);
    writeD(_pvpFlag);
  }
}