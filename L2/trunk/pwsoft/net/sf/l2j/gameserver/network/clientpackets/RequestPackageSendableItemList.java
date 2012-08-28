package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PackageSendableList;

public final class RequestPackageSendableItemList extends L2GameClientPacket
{
  private int _objectID;

  protected void readImpl()
  {
    _objectID = readD();
  }

  public void runImpl()
  {
    L2ItemInstance[] items = ((L2GameClient)getClient()).getActiveChar().getInventory().getAvailableItems(true);

    sendPacket(new PackageSendableList(items, _objectID));
  }
}