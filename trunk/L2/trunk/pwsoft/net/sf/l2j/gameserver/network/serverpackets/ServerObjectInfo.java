package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class ServerObjectInfo extends L2GameServerPacket
{
  private L2NpcInstance _activeChar;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _idTemplate;
  private boolean _isAttackable;
  private int _collisionHeight;
  private int _collisionRadius;

  public ServerObjectInfo(L2NpcInstance activeChar, L2Character actor)
  {
    _activeChar = activeChar;
    _idTemplate = _activeChar.getTemplate().idTemplate;
    _isAttackable = _activeChar.isAutoAttackable(actor);
    _collisionHeight = _activeChar.getCollisionHeight();
    _collisionRadius = _activeChar.getCollisionRadius();
    _x = _activeChar.getX();
    _y = _activeChar.getY();
    _z = _activeChar.getZ();
    _heading = _activeChar.getHeading();
  }

  protected final void writeImpl()
  {
    writeC(140);
    writeD(_activeChar.getObjectId());
    writeD(_idTemplate + 1000000);
    writeS("");
    writeD(_isAttackable ? 1 : 0);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_heading);
    writeF(1.0D);
    writeF(1.0D);
    writeF(_collisionRadius);
    writeF(_collisionHeight);
    writeD((int)(_isAttackable ? _activeChar.getCurrentHp() : 0.0D));
    writeD(_isAttackable ? _activeChar.getMaxHp() : 0);
    writeD(1);
    writeD(0);
  }

  public String getType()
  {
    return "S.ServerObjectInfo";
  }
}