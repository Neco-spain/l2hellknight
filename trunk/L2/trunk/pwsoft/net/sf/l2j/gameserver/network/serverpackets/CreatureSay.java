package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class CreatureSay extends L2GameServerPacket
{
  private int _objectId;
  private int _textType;
  private String _charName;
  private String _text;
  private boolean _fromServer = false;

  public CreatureSay(int objectId, int messageType, String charName, String text)
  {
    _objectId = objectId;
    _textType = messageType;
    _charName = charName;
    _text = text;
  }

  protected final void writeImpl()
  {
    writeC(74);
    writeD(_objectId);
    writeD(_textType);
    writeS(_charName);
    writeS(_text);

    L2PcInstance _pci = ((L2GameClient)getClient()).getActiveChar();
    if (_pci != null)
    {
      _pci.broadcastSnoop(_textType, _charName, _text);
    }
  }
}