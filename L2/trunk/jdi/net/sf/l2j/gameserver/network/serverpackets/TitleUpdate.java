package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class TitleUpdate extends L2GameServerPacket
{
  private static final String _S__CC_TITLE_UPDATE = "[S] cc TitleUpdate";
  private String _title;
  private int _objectId;

  public TitleUpdate(L2PcInstance cha)
  {
    _objectId = cha.getObjectId();
    _title = cha.getTitle();
  }

  protected void writeImpl()
  {
    writeC(204);
    writeD(_objectId);
    writeS(_title);
  }

  public String getType()
  {
    return "[S] cc TitleUpdate";
  }
}