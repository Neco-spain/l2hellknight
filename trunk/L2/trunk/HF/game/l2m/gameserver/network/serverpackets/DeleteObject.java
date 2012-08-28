package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class DeleteObject extends L2GameServerPacket
{
  private int _objectId;

  public DeleteObject(GameObject obj)
  {
    _objectId = obj.getObjectId();
  }

  protected final void writeImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (activeChar.getObjectId() == _objectId)) {
      return;
    }
    writeC(8);
    writeD(_objectId);
    writeD(1);
  }

  public String getType()
  {
    return super.getType() + " " + GameObjectsStorage.findObject(_objectId) + " (" + _objectId + ")";
  }
}