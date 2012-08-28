package l2m.gameserver.network.clientpackets;

import l2p.commons.lang.ArrayUtils;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.base.RestartType;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.entity.residence.ClanHall;
import l2m.gameserver.model.entity.residence.Fortress;
import l2m.gameserver.model.entity.residence.ResidenceFunction;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ActionFail;
import l2m.gameserver.network.serverpackets.Die;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.utils.ItemFunctions;
import l2m.gameserver.utils.Location;
import l2m.gameserver.utils.TeleportUtils;
import org.apache.commons.lang3.tuple.Pair;

public class RequestRestartPoint extends L2GameClientPacket
{
  private RestartType _restartType;

  protected void readImpl()
  {
    _restartType = ((RestartType)ArrayUtils.valid(RestartType.VALUES, readD()));
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if ((_restartType == null) || (activeChar == null)) {
      return;
    }
    if (activeChar.isFakeDeath())
    {
      activeChar.breakFakeDeath();
      return;
    }

    if ((!activeChar.isDead()) && (!activeChar.isGM()))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFestivalParticipant())
    {
      activeChar.doRevive();
      return;
    }

    switch (1.$SwitchMap$l2p$gameserver$model$base$RestartType[_restartType.ordinal()])
    {
    case 1:
      if (activeChar.isAgathionResAvailable())
        activeChar.doRevive(100.0D);
      else
        activeChar.sendPacket(new IStaticPacket[] { ActionFail.STATIC, new Die(activeChar) });
      break;
    case 2:
      if (activeChar.getPlayerAccess().ResurectFixed) {
        activeChar.doRevive(100.0D);
      } else if (ItemFunctions.removeItem(activeChar, 13300, 1L, true) == 1L)
      {
        activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
        activeChar.doRevive(100.0D);
      }
      else if (ItemFunctions.removeItem(activeChar, 10649, 1L, true) == 1L)
      {
        activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
        activeChar.doRevive(100.0D);
      }
      else {
        activeChar.sendPacket(new IStaticPacket[] { ActionFail.STATIC, new Die(activeChar) });
      }break;
    default:
      Location loc = null;
      Reflection ref = activeChar.getReflection();

      if (ref == ReflectionManager.DEFAULT) {
        for (GlobalEvent e : activeChar.getEvents())
          loc = e.getRestartLoc(activeChar, _restartType);
      }
      if (loc == null) {
        loc = defaultLoc(_restartType, activeChar);
      }
      if (loc != null)
      {
        Pair ask = activeChar.getAskListener(false);
        if ((ask != null) && ((ask.getValue() instanceof ReviveAnswerListener)) && (!((ReviveAnswerListener)ask.getValue()).isForPet())) {
          activeChar.getAskListener(true);
        }
        activeChar.setPendingRevive(true);
        activeChar.teleToLocation(loc, ReflectionManager.DEFAULT);
      }
      else {
        activeChar.sendPacket(new IStaticPacket[] { ActionFail.STATIC, new Die(activeChar) });
      }
    }
  }

  public static Location defaultLoc(RestartType restartType, Player activeChar)
  {
    Location loc = null;
    Clan clan = activeChar.getClan();

    switch (1.$SwitchMap$l2p$gameserver$model$base$RestartType[restartType.ordinal()])
    {
    case 3:
      if ((clan == null) || (clan.getHasHideout() == 0))
        break;
      ClanHall clanHall = activeChar.getClanHall();
      loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CLANHALL);
      if (clanHall.getFunction(5) != null)
        activeChar.restoreExp(clanHall.getFunction(5).getLevel());
      break;
    case 4:
      if ((clan == null) || (clan.getCastle() == 0))
        break;
      Castle castle = activeChar.getCastle();
      loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CASTLE);
      if (castle.getFunction(5) != null)
        activeChar.restoreExp(castle.getFunction(5).getLevel());
      break;
    case 5:
      if ((clan == null) || (clan.getHasFortress() == 0))
        break;
      Fortress fort = activeChar.getFortress();
      loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_FORTRESS);
      if (fort.getFunction(5) != null)
        activeChar.restoreExp(fort.getFunction(5).getLevel());
      break;
    case 6:
    default:
      loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_VILLAGE);
    }

    return loc;
  }
}