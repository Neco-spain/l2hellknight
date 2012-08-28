package l2p.gameserver.serverpackets;

import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

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