package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.instances.NpcInstance;

public class SpawnEmitter extends L2GameServerPacket
{
  private int _monsterObjId;
  private int _playerObjId;

  public SpawnEmitter(NpcInstance monster, Player player)
  {
    _playerObjId = player.getObjectId();
    _monsterObjId = monster.getObjectId();
  }

  protected final void writeImpl()
  {
    writeEx(93);

    writeD(_monsterObjId);
    writeD(_playerObjId);
    writeD(0);
  }
}