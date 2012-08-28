package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinParty;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinParty extends L2GameClientPacket
{
  private static final String _C__29_REQUESTJOINPARTY = "[C] 29 RequestJoinParty";
  private static Logger _log = Logger.getLogger(RequestJoinParty.class.getName());
  private String _name;
  private int _itemDistribution;

  protected void readImpl()
  {
    _name = readS();
    _itemDistribution = readD();
  }

  protected void runImpl()
  {
    L2PcInstance requestor = ((L2GameClient)getClient()).getActiveChar();
    L2PcInstance target = L2World.getInstance().getPlayer(_name);

    if (requestor == null) {
      return;
    }
    if (target == null)
    {
      requestor.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return;
    }

    if (target.isInParty())
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_ALREADY_IN_PARTY);
      msg.addString(target.getName());
      requestor.sendPacket(msg);
      return;
    }

    if (target.getAppearance().getInvisible())
    {
      requestor.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
      return;
    }

    if (target == requestor)
    {
      requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      return;
    }

    if ((target.isCursedWeaponEquiped()) || (requestor.isCursedWeaponEquiped()))
    {
      requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      return;
    }

    if ((target.isInJail()) || (requestor.isInJail()))
    {
      SystemMessage sm = SystemMessage.sendString("Player is in Jail");
      requestor.sendPacket(sm);
      return;
    }

    if ((target.isInOlympiadMode()) || (requestor.isInOlympiadMode())) {
      return;
    }
    if ((target.isInDuel()) || (requestor.isInDuel())) {
      return;
    }
    if (!requestor.isInParty())
    {
      createNewParty(target, requestor);
    }
    else if (requestor.getParty().isInDimensionalRift())
    {
      requestor.sendMessage("You can't invite a player when in Dimensional Rift.");
    }
    else
    {
      addTargetToParty(target, requestor);
    }
  }

  private void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
  {
    if (requestor.getParty().getMemberCount() + requestor.getParty().getPendingInvitationNumber() >= 9)
    {
      requestor.sendPacket(new SystemMessage(SystemMessageId.PARTY_FULL));
      return;
    }

    if (!requestor.getParty().isLeader(requestor))
    {
      requestor.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEADER_CAN_INVITE));
      return;
    }

    if (!target.isProcessingRequest())
    {
      requestor.onTransactionRequest(target);
      target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
      requestor.getParty().increasePendingInvitationNumber();

      if (Config.DEBUG) {
        _log.fine("sent out a party invitation to:" + target.getName());
      }
      SystemMessage msg = new SystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY);
      msg.addString(target.getName());
      requestor.sendPacket(msg);
    }
    else
    {
      msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
      requestor.sendPacket(msg);

      if (Config.DEBUG)
        _log.warning(requestor.getName() + " already received a party invitation");
    }
    SystemMessage msg = null;
  }

  private void createNewParty(L2PcInstance target, L2PcInstance requestor)
  {
    if (!target.isProcessingRequest())
    {
      requestor.setParty(new L2Party(requestor, _itemDistribution));

      requestor.onTransactionRequest(target);
      target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
      requestor.getParty().increasePendingInvitationNumber();

      if (Config.DEBUG) {
        _log.fine("sent out a party invitation to:" + target.getName());
      }
      SystemMessage msg = new SystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY);
      msg.addString(target.getName());
      requestor.sendPacket(msg);
    }
    else
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
      msg.addString(target.getName());
      requestor.sendPacket(msg);

      if (Config.DEBUG)
        _log.warning(requestor.getName() + " already received a party invitation");
    }
  }

  public String getType()
  {
    return "[C] 29 RequestJoinParty";
  }
}