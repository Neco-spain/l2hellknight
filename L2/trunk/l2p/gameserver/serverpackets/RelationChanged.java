package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;

public class RelationChanged extends L2GameServerPacket
{
  public static final int RELATION_PARTY1 = 1;
  public static final int RELATION_PARTY2 = 2;
  public static final int RELATION_PARTY3 = 4;
  public static final int RELATION_PARTY4 = 8;
  public static final int RELATION_PARTYLEADER = 16;
  public static final int RELATION_HAS_PARTY = 32;
  public static final int RELATION_CLAN_MEMBER = 64;
  public static final int RELATION_LEADER = 128;
  public static final int RELATION_CLAN_MATE = 256;
  public static final int RELATION_INSIEGE = 512;
  public static final int RELATION_ATTACKER = 1024;
  public static final int RELATION_ALLY = 2048;
  public static final int RELATION_ENEMY = 4096;
  public static final int RELATION_MUTUAL_WAR = 16384;
  public static final int RELATION_1SIDED_WAR = 32768;
  public static final int RELATION_ALLY_MEMBER = 65536;
  public static final int RELATION_ISINTERRITORYWARS = 524288;
  protected final List<RelationChangedData> _data;

  protected RelationChanged(int s)
  {
    _data = new ArrayList(s);
  }

  protected void add(RelationChangedData data)
  {
    _data.add(data);
  }

  protected void writeImpl()
  {
    writeC(206);
    writeD(_data.size());
    for (RelationChangedData d : _data)
    {
      writeD(d.charObjId);
      writeD(d.relation);
      writeD(d.isAutoAttackable ? 1 : 0);
      writeD(d.karma);
      writeD(d.pvpFlag);
    }
  }

  public static L2GameServerPacket update(Player sendTo, Playable targetPlayable, Player activeChar)
  {
    if ((sendTo == null) || (targetPlayable == null) || (activeChar == null)) {
      return null;
    }
    Player targetPlayer = targetPlayable.getPlayer();

    int relation = targetPlayer == null ? 0 : targetPlayer.getRelation(activeChar);

    RelationChanged pkt = new RelationChanged(1);

    pkt.add(new RelationChangedData(targetPlayable, targetPlayable.isAutoAttackable(activeChar), relation));

    return pkt;
  }

  static class RelationChangedData
  {
    public final int charObjId;
    public final boolean isAutoAttackable;
    public final int relation;
    public final int karma;
    public final int pvpFlag;

    public RelationChangedData(Playable cha, boolean _isAutoAttackable, int _relation)
    {
      isAutoAttackable = _isAutoAttackable;
      relation = _relation;
      charObjId = cha.getObjectId();
      karma = cha.getKarma();
      pvpFlag = cha.getPvpFlag();
    }
  }
}