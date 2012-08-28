package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class TitleUpdate extends L2GameServerPacket
{
  private String _title;
  private int _objectId;
  private boolean can_writeimpl = false;

  public TitleUpdate(L2PcInstance cha)
  {
    _objectId = cha.getObjectId();
    _title = cha.getTitle();

    if (_title.length() > 16)
    {
      cha.sendMessage("\u041D\u0435 \u0431\u043E\u043B\u0435\u0435 16 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432");
      return;
    }

    can_writeimpl = true;
  }

  protected void writeImpl()
  {
    if (!can_writeimpl) {
      return;
    }
    writeC(204);
    writeD(_objectId);
    writeS(_title);
  }

  public String getType()
  {
    return "S.TitleUpdate";
  }
}