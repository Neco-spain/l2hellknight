package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import scripts.communitybbs.Manager.RegionBBSManager;

public final class Logout extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPAV() < 100L) {
      return;
    }

    player.sCPAV();

    player.getInventory().updateDatabase();

    if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
    {
      player.sendPacket(Static.CANT_LOGOUT_WHILE_FIGHTING);
      player.sendActionFailed();
      return;
    }

    if (player.getActiveTradeList() != null)
    {
      player.cancelActiveTrade();
      if (player.getTransactionRequester() != null) {
        player.getTransactionRequester().setTransactionRequester(null);
      }
      player.setTransactionRequester(null);
    }

    if (player.atEvent) {
      player.sendMessage("A superior power doesn't allow you to leave the event");
      return;
    }

    if (player.isInOfflineMode()) {
      player.closeNetConnection();
      return;
    }

    if (player.isFestivalParticipant()) {
      if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
        player.sendMessage("You cannot log out while you are a participant in a festival.");
        return;
      }
      L2Party playerParty = player.getParty();

      if (playerParty != null) {
        player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
      }
    }
    if (player.isFlying()) {
      player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
    }

    TvTEvent.onLogout(player);
    RegionBBSManager.getInstance().changeCommunityBoard();

    player.deleteMe();
  }
}