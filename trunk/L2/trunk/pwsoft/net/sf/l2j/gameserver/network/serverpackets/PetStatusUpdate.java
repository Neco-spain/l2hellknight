package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;

public class PetStatusUpdate extends L2GameServerPacket
{
  private String title;
  private long exp;
  private long cur;
  private long next;
  private int _maxHp;
  private int _maxMp;
  private int _summonType;
  private int _summonObj;
  private int _maxFed;
  private int _curFed;
  private int x;
  private int y;
  private int z;
  private int hp;
  private int mp;
  private int level;

  public PetStatusUpdate(L2Summon summon)
  {
    _maxHp = summon.getMaxHp();
    _maxMp = summon.getMaxMp();
    if (summon.isPet()) {
      L2PetInstance pet = (L2PetInstance)summon;
      _curFed = pet.getCurrentFed();
      _maxFed = pet.getMaxFed();
    }
    _summonType = summon.getSummonType();
    _summonObj = summon.getObjectId();
    x = summon.getX();
    y = summon.getY();
    z = summon.getZ();
    title = summon.getTitle();
    hp = (int)summon.getCurrentHp();
    mp = (int)summon.getCurrentMp();
    level = summon.getLevel();
    exp = summon.getStat().getExp();
    cur = summon.getExpForThisLevel();
    next = summon.getExpForNextLevel();
  }

  public PetStatusUpdate(L2PcInstance partner) {
    _maxHp = partner.getMaxHp();
    _maxMp = partner.getMaxMp();

    _curFed = 100;
    _maxFed = 100;

    _summonType = 1;
    _summonObj = partner.getObjectId();
    x = partner.getX();
    y = partner.getY();
    z = partner.getZ();
    title = partner.getTitle();
    hp = (int)partner.getCurrentHp();
    mp = (int)partner.getCurrentMp();
    level = partner.getLevel();
    exp = partner.getStat().getExp();
    cur = partner.getStat().getExp();
    next = partner.getStat().getExp();
  }

  protected final void writeImpl()
  {
    writeC(181);
    writeD(_summonType);
    writeD(_summonObj);
    writeD(x);
    writeD(y);
    writeD(z);
    writeS(title);
    writeD(_curFed);
    writeD(_maxFed);
    writeD(hp);
    writeD(_maxHp);
    writeD(mp);
    writeD(_maxMp);
    writeD(level);
    writeQ(exp);
    writeQ(cur);
    writeQ(next);
  }
}