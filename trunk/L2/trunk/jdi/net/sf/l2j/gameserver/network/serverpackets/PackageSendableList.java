package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.templates.L2Item;

public class PackageSendableList extends L2GameServerPacket
{
  private static final String _S__C3_PACKAGESENDABLELIST = "[S] C3 PackageSendableList";
  private L2ItemInstance[] _items;
  private int _playerObjId;

  public PackageSendableList(L2ItemInstance[] items, int playerObjId)
  {
    _items = items;
    _playerObjId = playerObjId;
  }

  protected void writeImpl()
  {
    writeC(195);

    writeD(_playerObjId);
    writeD(((L2GameClient)getClient()).getActiveChar().getAdena());
    writeD(_items.length);
    for (L2ItemInstance item : _items)
    {
      writeH(item.getItem().getType1());
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(item.getCount());
      writeH(item.getItem().getType2());
      writeH(0);
      writeD(item.getItem().getBodyPart());
      writeH(item.getEnchantLevel());
      writeH(0);
      writeH(0);
      writeD(item.getObjectId());
    }
  }

  public String getType()
  {
    return "[S] C3 PackageSendableList";
  }
}