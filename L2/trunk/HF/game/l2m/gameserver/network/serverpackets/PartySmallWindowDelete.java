package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;

public class PartySmallWindowDelete extends L2GameServerPacket
{
  private final int _objId;
  private final String _name;

  public PartySmallWindowDelete(Player member)
  {
    _objId = member.getObjectId();
    _name = member.getName();
  }

  protected final void writeImpl()
  {
    writeC(81);
    writeD(_objId);
    writeS(_name);
  }
}