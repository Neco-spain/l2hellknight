package l2rt.gameserver.network.clientpackets;

import javolution.util.FastList;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.UsablePacketItem;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExGetCrystalizingEstimation;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;

public class RequestCrystallizeEstimate extends L2GameClientPacket
{
	private int _itemObjId;
	private long _count;

	public void readImpl()
	{
		_itemObjId = readD();
		_count = readQ();
	}

  public void runImpl()
  {
    L2Player player = getClient().getActiveChar();
    if (player == null)
      return;

    if (_count > 1L)
      _count = 1L;

    L2ItemInstance item = player.getInventory().getItemByObjectId(Integer.valueOf(_itemObjId));
    if ((item == null) || (!(item.canBeCrystallized(player, true))))
    {
      player.sendActionFailed();
      return;
    }

    int crystalAmount = item.getItem().getCrystalCount();
    int crystalId = item.getItem().getCrystalType().cry;

    player.CrystallizationProducts = FastList.newInstance();
    player.CrystallizationProducts.add(new UsablePacketItem(crystalId, crystalAmount, 100.0F));
    player.CrystallizationProducts.add(new UsablePacketItem(57, 5000, 65.0F));
    player.sendPacket(new L2GameServerPacket[] { new ExGetCrystalizingEstimation(player.CrystallizationProducts) });
  }
}