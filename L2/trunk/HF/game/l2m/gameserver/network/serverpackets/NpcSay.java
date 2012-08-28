package l2m.gameserver.serverpackets;

import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.network.serverpackets.components.ChatType;
import l2m.gameserver.network.serverpackets.components.NpcString;

public class NpcSay extends NpcStringContainer
{
  private int _objId;
  private int _type;
  private int _id;

  public NpcSay(NpcInstance npc, ChatType chatType, String text)
  {
    this(npc, chatType, NpcString.NONE, new String[] { text });
  }

  public NpcSay(NpcInstance npc, ChatType chatType, NpcString npcString, String[] params)
  {
    super(npcString, params);
    _objId = npc.getObjectId();
    _id = npc.getNpcId();
    _type = chatType.ordinal();
  }

  protected final void writeImpl()
  {
    writeC(48);
    writeD(_objId);
    writeD(_type);
    writeD(1000000 + _id);
    writeElements();
  }
}