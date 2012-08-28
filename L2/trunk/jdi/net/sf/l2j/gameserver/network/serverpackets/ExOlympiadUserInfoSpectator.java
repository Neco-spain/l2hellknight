package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

public class ExOlympiadUserInfoSpectator extends L2GameServerPacket
{
  private static final String _S__FE_29_OLYMPIADUSERINFOSPECTATOR = "[S] FE:29 OlympiadUserInfoSpectator";
  private static int _side;
  private static L2PcInstance _player;

  public ExOlympiadUserInfoSpectator(L2PcInstance player, int side)
  {
    _player = player;
    _side = side;
  }

  protected final void writeImpl()
  {
    writeC(254);
    writeH(41);
    writeC(_side);
    writeD(_player.getObjectId());
    writeS(_player.getName());
    writeD(_player.getClassId().getId());
    writeD((int)_player.getCurrentHp());
    writeD(_player.getMaxHp());
    writeD((int)_player.getCurrentCp());
    writeD(_player.getMaxCp());
  }

  public String getType()
  {
    return "[S] FE:29 OlympiadUserInfoSpectator";
  }
}