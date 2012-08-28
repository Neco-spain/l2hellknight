package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;

public class Attack extends L2GameServerPacket
{
  protected final int _attackerObjId;
  public final boolean soulshot;
  protected int _grade;
  private int _x;
  private int _y;
  private int _z;
  private Hit[] _hits;

  public Attack(L2Character attacker, boolean ss, int grade)
  {
    _attackerObjId = attacker.getObjectId();
    soulshot = ss;
    _grade = grade;
    _x = attacker.getX();
    _y = attacker.getY();
    _z = attacker.getZ();
    _hits = new Hit[0];
  }

  public void addHit(L2Object target, int damage, boolean miss, boolean crit, boolean shld)
  {
    int pos = _hits.length;

    Hit[] tmp = new Hit[pos + 1];

    for (int i = 0; i < _hits.length; i++) {
      tmp[i] = _hits[i];
    }
    tmp[pos] = new Hit(target, damage, miss, crit, shld);
    _hits = tmp;
  }

  public boolean hasHits()
  {
    return _hits.length > 0;
  }

  protected final void writeImpl()
  {
    writeC(5);

    writeD(_attackerObjId);
    writeD(_hits[0]._targetId);
    writeD(_hits[0]._damage);
    writeC(_hits[0]._flags);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeH(_hits.length - 1);
    for (int i = 1; i < _hits.length; i++) {
      writeD(_hits[i]._targetId);
      writeD(_hits[i]._damage);
      writeC(_hits[i]._flags);
    }
  }

  private class Hit
  {
    protected int _targetId;
    protected int _damage;
    protected int _flags;

    Hit(L2Object target, int damage, boolean miss, boolean crit, boolean shld)
    {
      _targetId = target.getObjectId();
      _damage = damage;
      if ((soulshot) && (Config.SOULSHOT_ANIM)) {
        _flags |= 0x10 | _grade;
      }
      if (crit) {
        _flags |= 32;
      }
      if (shld) {
        _flags |= 64;
      }
      if (miss)
        _flags |= 128;
    }
  }
}