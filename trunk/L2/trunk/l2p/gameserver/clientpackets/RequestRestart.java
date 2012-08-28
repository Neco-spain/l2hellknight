package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.loginservercon.SessionKey;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.network.GameClient.GameClientState;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.RestartResponse;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;

public class RequestRestart extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if (activeChar.isInObserverMode())
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponse.FAIL, ActionFail.STATIC });
      return;
    }

    if (activeChar.isInCombat())
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponse.FAIL, ActionFail.STATIC });
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING, RestartResponse.FAIL, ActionFail.STATIC });
      return;
    }

    if ((activeChar.isBlocked()) && (!activeChar.isFlying()))
    {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.OutOfControl", activeChar, new Object[0]));
      activeChar.sendPacket(new IStaticPacket[] { RestartResponse.FAIL, ActionFail.STATIC });
      return;
    }

    if ((activeChar.isFestivalParticipant()) && 
      (SevenSignsFestival.getInstance().isFestivalInitialized()))
    {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.Festival", activeChar, new Object[0]));
      activeChar.sendPacket(new IStaticPacket[] { RestartResponse.FAIL, ActionFail.STATIC });
      return;
    }

    if (getClient() != null)
      ((GameClient)getClient()).setState(GameClient.GameClientState.AUTHED);
    activeChar.restart();

    CharacterSelectionInfo cl = new CharacterSelectionInfo(((GameClient)getClient()).getLogin(), ((GameClient)getClient()).getSessionKey().playOkID1);
    sendPacket(new L2GameServerPacket[] { RestartResponse.OK, cl });
    ((GameClient)getClient()).setCharSelection(cl.getCharInfo());
  }
}