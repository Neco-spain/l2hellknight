package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;

public final class RequestGetItemFromPet extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());
  private int _objectId;
  private int _amount;
  private int _unknown;

  protected void readImpl()
  {
    _objectId = readD();
    _amount = readD();
    _unknown = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getPet() == null) || (!player.getPet().isPet())) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPU() < 400L)
    {
      player.sendActionFailed();
      return;
    }

    player.sCPU();

    if (player.getActiveEnchantItem() != null)
    {
      player.setActiveEnchantItem(null);
      player.sendPacket(new EnchantResult(0, true));
      player.sendActionFailed();
      return;
    }

    if (player.getActiveTradeList() != null)
    {
      player.cancelActiveTrade();
      player.sendActionFailed();
      return;
    }

    if (player.getActiveWarehouse() != null)
    {
      player.cancelActiveWarehouse();
      player.sendActionFailed();
      return;
    }

    L2PetInstance pet = (L2PetInstance)player.getPet();

    if (_amount < 0)
    {
      return;
    }
    if (_amount == 0) {
      return;
    }
    if (pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
    {
      _log.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
    }
  }
}