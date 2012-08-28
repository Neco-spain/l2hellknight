package l2m.gameserver.serverpackets;

import l2m.gameserver.network.serverpackets.components.ChatType;
import l2m.gameserver.network.serverpackets.components.NpcString;
import l2m.gameserver.network.serverpackets.components.SysString;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class Say2 extends NpcStringContainer
{
  private ChatType _type;
  private SysString _sysString;
  private SystemMsg _systemMsg;
  private int _objectId;
  private String _charName;

  public Say2(int objectId, ChatType type, SysString st, SystemMsg sm)
  {
    super(NpcString.NONE, new String[0]);
    _objectId = objectId;
    _type = type;
    _sysString = st;
    _systemMsg = sm;
  }

  public Say2(int objectId, ChatType type, String charName, String text)
  {
    this(objectId, type, charName, NpcString.NONE, new String[] { text });
  }

  public Say2(int objectId, ChatType type, String charName, NpcString npcString, String[] params)
  {
    super(npcString, params);
    _objectId = objectId;
    _type = type;
    _charName = charName;
  }

  protected final void writeImpl()
  {
    writeC(74);
    writeD(_objectId);
    writeD(_type.ordinal());
    switch (1.$SwitchMap$l2p$gameserver$serverpackets$components$ChatType[_type.ordinal()])
    {
    case 1:
      writeD(_sysString.getId());
      writeD(_systemMsg.getId());
      break;
    default:
      writeS(_charName);
      writeElements();
    }
  }
}