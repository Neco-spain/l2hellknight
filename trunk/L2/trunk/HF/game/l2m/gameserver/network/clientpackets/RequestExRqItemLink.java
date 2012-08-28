package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.ItemInfoCache;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.network.serverpackets.ActionFail;
import l2m.gameserver.network.serverpackets.ExRpItemLink;

public class RequestExRqItemLink extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    ItemInfo item;
    if ((item = ItemInfoCache.getInstance().get(_objectId)) == null)
      sendPacket(ActionFail.STATIC);
    else
      sendPacket(new ExRpItemLink(item));
  }
}