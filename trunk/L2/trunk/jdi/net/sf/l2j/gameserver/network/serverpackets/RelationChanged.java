package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;

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
  private static final String _S__CE_RELATIONCHANGED = "[S] CE RelationChanged";
  private int _objId;
  private int _relation;
  private int _autoAttackable;
  private int _karma;
  private int _pvpFlag;

  public RelationChanged(L2PlayableInstance cha, int relation, boolean autoattackable)
  {
    _objId = cha.getObjectId();
    _relation = relation;
    _autoAttackable = (autoattackable ? 1 : 0);
    if ((cha instanceof L2PcInstance))
    {
      _karma = ((L2PcInstance)cha).getKarma();
      _pvpFlag = ((L2PcInstance)cha).getPvpFlag();
    }
    else if ((cha instanceof L2SummonInstance))
    {
      _karma = 0;
      _pvpFlag = ((L2SummonInstance)cha).getOwner().getPvpFlag();
    }
  }

  public RelationChanged(L2Summon pet, int relation, boolean autoAttackable)
  {
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

  public String getType()
  {
    return "[S] CE RelationChanged";
  }
}