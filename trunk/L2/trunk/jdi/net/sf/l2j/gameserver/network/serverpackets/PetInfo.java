package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class PetInfo extends L2GameServerPacket
{
  private static final String _S__CA_PETINFO = "[S] b1 PetInfo";
  private L2Summon _summon;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private boolean _isSummoned;
  private int _mAtkSpd;
  private int _pAtkSpd;
  private int _runSpd;
  private int _walkSpd;
  private int _swimRunSpd;
  private int _swimWalkSpd;
  private int _flRunSpd;
  private int _flWalkSpd;
  private int _flyRunSpd;
  private int _flyWalkSpd;
  private int _maxHp;
  private int _maxMp;
  private int _maxFed;
  private int _curFed;

  public PetInfo(L2Summon summon)
  {
    _summon = summon;
    _isSummoned = _summon.isShowSummonAnimation();
    _x = _summon.getX();
    _y = _summon.getY();
    _z = _summon.getZ();
    _heading = _summon.getHeading();
    _mAtkSpd = _summon.getMAtkSpd();
    _pAtkSpd = _summon.getPAtkSpd();
    _runSpd = _summon.getRunSpeed();
    _walkSpd = _summon.getWalkSpeed();
    _swimRunSpd = (this._flRunSpd = this._flyRunSpd = _runSpd);
    _swimWalkSpd = (this._flWalkSpd = this._flyWalkSpd = _walkSpd);
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
    writeC(177);
    writeD(_summon.getSummonType());
    writeD(_summon.getObjectId());
    writeD(_summon.getTemplate().idTemplate + 1000000);
    writeD(0);

    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_heading);
    writeD(0);
    writeD(_mAtkSpd);
    writeD(_pAtkSpd);
    writeD(_runSpd);
    writeD(_walkSpd);
    writeD(_swimRunSpd);
    writeD(_swimWalkSpd);
    writeD(_flRunSpd);
    writeD(_flWalkSpd);
    writeD(_flyRunSpd);
    writeD(_flyWalkSpd);

    writeF(1.0D);
    writeF(1.0D);
    writeF(_summon.getTemplate().collisionRadius);
    writeF(_summon.getTemplate().collisionHeight);
    writeD(0);
    writeD(0);
    writeD(0);
    writeC(1);
    writeC(_summon.isRunning() ? 1 : 0);
    writeC(_summon.isInCombat() ? 1 : 0);
    writeC(_summon.isAlikeDead() ? 1 : 0);
    writeC(_isSummoned ? 2 : 0);
    writeS(_summon.getName());
    writeS(_summon.getTitle());
    writeD(1);
    writeD(_summon.getPvpFlag());
    writeD(_summon.getKarma());
    writeD(_curFed);
    writeD(_maxFed);
    writeD((int)_summon.getCurrentHp());
    writeD(_maxHp);
    writeD((int)_summon.getCurrentMp());
    writeD(_maxMp);
    writeD(_summon.getStat().getSp());
    writeD(_summon.getLevel());
    writeQ(_summon.getStat().getExp());
    writeQ(_summon.getExpForThisLevel());
    writeQ(_summon.getExpForNextLevel());
    writeD((_summon instanceof L2PetInstance) ? _summon.getInventory().getTotalWeight() : 0);
    writeD(_summon.getMaxLoad());
    writeD(_summon.getPAtk(null));
    writeD(_summon.getPDef(null));
    writeD(_summon.getMAtk(null, null));
    writeD(_summon.getMDef(null, null));
    writeD(_summon.getAccuracy());
    writeD(_summon.getEvasionRate(null));
    writeD(_summon.getCriticalHit(null, null));
    writeD(_runSpd);
    writeD(_summon.getPAtkSpd());
    writeD(_summon.getMAtkSpd());

    writeD(0);
    int npcId = _summon.getTemplate().npcId;

    if ((npcId >= 12526) && (npcId <= 12528))
      writeH(1);
    else {
      writeH(0);
    }
    writeC(0);

    writeH(0);
    writeC(0);
    writeD(_summon.getSoulShotsPerHit());
    writeD(_summon.getSpiritShotsPerHit());
  }

  public String getType()
  {
    return "[S] b1 PetInfo";
  }
}