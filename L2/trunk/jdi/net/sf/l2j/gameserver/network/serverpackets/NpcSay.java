package net.sf.l2j.gameserver.network.serverpackets;

public final class NpcSay extends L2GameServerPacket
{
  private static final String _S__02_NPCSAY = "[S] 02 NpcSay";
  private int _objectId;
  private int _textType;
  private int _npcId;
  private String _text;

  public NpcSay(int objectId, int messageType, int npcId, String text)
  {
    _objectId = objectId;
    _textType = messageType;
    _npcId = (1000000 + npcId);
    _text = text;
  }

  protected final void writeImpl()
  {
    writeC(2);
    writeD(_objectId);
    writeD(_textType);
    writeD(_npcId);
    writeS(_text);
  }

  public String getType()
  {
    return "[S] 02 NpcSay";
  }
}