package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.util.Util;

public final class RequestGetItemFromPet extends L2GameClientPacket
{
  private static final String REQUESTGETITEMFROMPET__C__8C = "[C] 8C RequestGetItemFromPet";
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
    if ((player == null) || (player.getPet() == null) || (!(player.getPet() instanceof L2PetInstance))) return;
    L2PetInstance pet = (L2PetInstance)player.getPet();

    if (_amount < 0)
    {
      Util.handleIllegalPlayerAction(player, "[RequestGetItemFromPet] count < 0! ban! oid: " + _objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
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

  public String getType()
  {
    return "[C] 8C RequestGetItemFromPet";
  }
}