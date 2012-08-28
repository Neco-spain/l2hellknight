package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Creature;

public class NickNameChanged extends L2GameServerPacket
{
  private final int objectId;
  private final String title;

  public NickNameChanged(Creature cha)
  {
    objectId = cha.getObjectId();
    title = cha.getTitle();
  }

  protected void writeImpl()
  {
    writeC(204);
    writeD(objectId);
    writeS(title);
  }
}