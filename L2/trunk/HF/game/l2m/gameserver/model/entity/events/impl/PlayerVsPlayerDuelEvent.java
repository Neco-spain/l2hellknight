package l2m.gameserver.model.entity.events.impl;

import java.util.List;
import l2p.commons.collections.MultiValueSet;
import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Request;
import l2m.gameserver.model.Request.L2RequestType;
import l2m.gameserver.model.base.TeamType;
import l2m.gameserver.model.entity.events.objects.DuelSnapshotObject;
import l2m.gameserver.network.serverpackets.ExDuelAskStart;
import l2m.gameserver.network.serverpackets.ExDuelEnd;
import l2m.gameserver.network.serverpackets.ExDuelReady;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.SocialAction;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class PlayerVsPlayerDuelEvent extends DuelEvent
{
  public PlayerVsPlayerDuelEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  protected PlayerVsPlayerDuelEvent(int id, String name)
  {
    super(id, name);
  }

  public boolean canDuel(Player player, Player target, boolean first)
  {
    IStaticPacket sm = canDuel0(player, target);
    if (sm != null)
    {
      player.sendPacket(sm);
      return false;
    }

    sm = canDuel0(target, player);
    if (sm != null)
    {
      player.sendPacket(SystemMsg.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
      return false;
    }

    return true;
  }

  public void askDuel(Player player, Player target)
  {
    Request request = new Request(Request.L2RequestType.DUEL, player, target).setTimeout(10000L);
    request.set("duelType", 0);
    player.setRequest(request);
    target.setRequest(request);

    player.sendPacket(new SystemMessage2(SystemMsg.C1_HAS_BEEN_CHALLENGED_TO_A_DUEL).addName(target));
    target.sendPacket(new IStaticPacket[] { new SystemMessage2(SystemMsg.C1_HAS_CHALLENGED_YOU_TO_A_DUEL).addName(player), new ExDuelAskStart(player.getName(), 0) });
  }

  public void createDuel(Player player, Player target)
  {
    PlayerVsPlayerDuelEvent duelEvent = new PlayerVsPlayerDuelEvent(getDuelType(), player.getObjectId() + "_" + target.getObjectId() + "_duel");
    cloneTo(duelEvent);

    duelEvent.addObject(BLUE_TEAM, new DuelSnapshotObject(player, TeamType.BLUE));
    duelEvent.addObject(RED_TEAM, new DuelSnapshotObject(target, TeamType.RED));
    duelEvent.sendPacket(new ExDuelReady(this));
    duelEvent.reCalcNextTime(false);
  }

  public void stopEvent()
  {
    clearActions();

    updatePlayers(false, false);

    for (DuelSnapshotObject d : this)
    {
      d.getPlayer().sendPacket(new ExDuelEnd(this));
      GameObject target = d.getPlayer().getTarget();
      if (target != null) {
        d.getPlayer().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, target);
      }
    }
    switch (1.$SwitchMap$l2p$gameserver$model$base$TeamType[_winner.ordinal()])
    {
    case 1:
      sendPacket(SystemMsg.THE_DUEL_HAS_ENDED_IN_A_TIE);
      break;
    case 2:
    case 3:
      List winners = getObjects(_winner.name());
      List lossers = getObjects(_winner.revert().name());

      sendPacket(new SystemMessage2(SystemMsg.C1_HAS_WON_THE_DUEL).addName(((DuelSnapshotObject)winners.get(0)).getPlayer()));

      for (DuelSnapshotObject d : lossers) {
        d.getPlayer().broadcastPacket(new L2GameServerPacket[] { new SocialAction(d.getPlayer().getObjectId(), 7) });
      }
    }

    removeObjects(RED_TEAM);
    removeObjects(BLUE_TEAM);
  }

  public void onDie(Player player)
  {
    TeamType team = player.getTeam();
    if ((team == TeamType.NONE) || (_aborted)) {
      return;
    }
    boolean allDead = true;
    List objs = getObjects(team.name());
    for (DuelSnapshotObject obj : objs)
    {
      if (obj.getPlayer() == player) {
        obj.setDead();
      }
      if (!obj.isDead()) {
        allDead = false;
      }
    }
    if (allDead)
    {
      _winner = team.revert();

      stopEvent();
    }
  }

  public int getDuelType()
  {
    return 0;
  }

  public void playerExit(Player player)
  {
    if ((_winner != TeamType.NONE) || (_aborted)) {
      return;
    }
    _winner = player.getTeam().revert();
    _aborted = false;

    stopEvent();
  }

  public void packetSurrender(Player player)
  {
    playerExit(player);
  }

  protected long startTimeMillis()
  {
    return System.currentTimeMillis() + 5000L;
  }
}