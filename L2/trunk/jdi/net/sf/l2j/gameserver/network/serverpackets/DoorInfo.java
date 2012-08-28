package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.L2GameClient;

public class DoorInfo extends L2GameServerPacket
{
  private static final String _S__60_DOORINFO = "[S] 4c DoorInfo";
  private L2DoorInstance _door;
  private final int _staticObjectId;
  private final int _objectId;
  private final boolean _isTargetable;
  private final boolean _isClosed;
  private final boolean _isEnemyOf;
  private final int _maxHp;
  private final int _currentHp;
  private final boolean _showHp;
  private final int _damageGrade;
  private final Castle _castle;
  private final Fort _fort;

  public DoorInfo(L2DoorInstance door, boolean showHp)
  {
    _staticObjectId = door.getDoorId();
    _objectId = door.getObjectId();

    _door = door;
    _isTargetable = true;
    _isClosed = (!door.getOpen());
    _isEnemyOf = door.isEnemyOf(_door);
    _maxHp = door.getMaxHp();
    _currentHp = (int)door.getCurrentHp();
    _showHp = showHp;
    _damageGrade = door.getDamage();

    _castle = door.getCastle();
    _fort = door.getFort();
  }

  protected final void writeImpl()
  {
    writeC(76);
    writeD(_door.getObjectId());
    writeD(_door.getDoorId());

    if (((_castle != null) && (_castle.getSiege().getIsInProgress())) || ((_fort != null) && (_fort.getSiege().getIsInProgress())))
      writeD(1);
    else
      writeD(0);
    writeD(_isTargetable ? 1 : 0);
    writeD(_isClosed ? 1 : 0);
    writeD(_door.isEnemyOf(((L2GameClient)getClient()).getActiveChar()) ? 1 : 0);
    writeD(_currentHp);
    writeD(_maxHp);
    writeD(_showHp ? 1 : 0);
    writeD(_damageGrade);
  }

  public String getType()
  {
    return "[S] 4c DoorInfo";
  }
}