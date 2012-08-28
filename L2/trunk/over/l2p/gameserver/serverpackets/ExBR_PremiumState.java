package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

public class ExBR_PremiumState extends L2GameServerPacket
{
  private int _objectId;
  private int _state;

  public ExBR_PremiumState(Player activeChar, boolean state)
  {
    _objectId = activeChar.getObjectId();
    _state = (state ? 1 : 0);
  }

  protected void writeImpl()
  {
    writeEx(217);
    writeD(_objectId);
    writeC(_state);
  }
}