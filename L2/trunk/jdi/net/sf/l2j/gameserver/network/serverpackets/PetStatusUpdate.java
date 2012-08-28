package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;

public class PetStatusUpdate extends L2GameServerPacket
{
  private static final String _S__CE_PETSTATUSSHOW = "[S] B5 PetStatusUpdate";
  private L2Summon _summon;
  private int _maxHp;
  private int _maxMp;
  private int _maxFed;
  private int _curFed;

  public PetStatusUpdate(L2Summon summon)
  {
    _summon = summon;
    _maxHp = _summon.getMaxHp();
    _maxMp = _summon.getMaxMp();
    if ((_summon instanceof L2PetInstance))
    {
      L2PetInstance pet = (L2PetInstance)_summon;
      _curFed = pet.getCurrentFed();
      _maxFed = pet.getMaxFed();
    }
  }

  protected final void writeImpl()
  {
    writeC(181);
    writeD(_summon.getSummonType());
    writeD(_summon.getObjectId());
    writeD(_summon.getX());
    writeD(_summon.getY());
    writeD(_summon.getZ());
    writeS(_summon.getTitle());
    writeD(_curFed);
    writeD(_maxFed);
    writeD((int)_summon.getCurrentHp());
    writeD(_maxHp);
    writeD((int)_summon.getCurrentMp());
    writeD(_maxMp);
    writeD(_summon.getLevel());
    writeQ(_summon.getStat().getExp());
    writeQ(_summon.getExpForThisLevel());
    writeQ(_summon.getExpForNextLevel());
  }

  public String getType()
  {
    return "[S] B5 PetStatusUpdate";
  }
}