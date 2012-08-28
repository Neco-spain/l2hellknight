package l2p.gameserver.model.entity.events.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.instancemanager.PlayerMessageStack;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.actions.StartStopAction;
import l2p.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject.SiegeClanComparatorImpl;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.items.ClanWarehouse;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.ClanTable;
import org.napile.primitive.maps.IntObjectMap;

public class ClanHallAuctionEvent extends SiegeEvent<ClanHall, AuctionSiegeClanObject>
{
  private Calendar _endSiegeDate = Calendar.getInstance();

  public ClanHallAuctionEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void reCalcNextTime(boolean onStart)
  {
    clearActions();
    _onTimeActions.clear();

    Clan owner = ((ClanHall)getResidence()).getOwner();

    _endSiegeDate.setTimeInMillis(0L);

    if ((((ClanHall)getResidence()).getAuctionLength() == 0) && (owner == null))
    {
      ((ClanHall)getResidence()).getSiegeDate().setTimeInMillis(System.currentTimeMillis());
      ((ClanHall)getResidence()).getSiegeDate().set(7, 2);
      ((ClanHall)getResidence()).getSiegeDate().set(11, 15);
      ((ClanHall)getResidence()).getSiegeDate().set(12, 0);
      ((ClanHall)getResidence()).getSiegeDate().set(13, 0);
      ((ClanHall)getResidence()).getSiegeDate().set(14, 0);

      ((ClanHall)getResidence()).setAuctionLength(7);
      ((ClanHall)getResidence()).setAuctionMinBid(((ClanHall)getResidence()).getBaseMinBid());
      ((ClanHall)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
      ((ClanHall)getResidence()).update();

      _onTimeActions.clear();
      addOnTimeAction(0, new StartStopAction("event", true));
      addOnTimeAction(((ClanHall)getResidence()).getAuctionLength() * 86400, new StartStopAction("event", false));

      _endSiegeDate.setTimeInMillis(((ClanHall)getResidence()).getSiegeDate().getTimeInMillis() + ((ClanHall)getResidence()).getAuctionLength() * 86400000L);

      registerActions();
    }
    else if ((((ClanHall)getResidence()).getAuctionLength() != 0) || (owner == null))
    {
      long endDate = ((ClanHall)getResidence()).getSiegeDate().getTimeInMillis() + ((ClanHall)getResidence()).getAuctionLength() * 86400000L;

      if (endDate <= System.currentTimeMillis()) {
        ((ClanHall)getResidence()).getSiegeDate().setTimeInMillis(System.currentTimeMillis());
      }
      _endSiegeDate.setTimeInMillis(((ClanHall)getResidence()).getSiegeDate().getTimeInMillis() + ((ClanHall)getResidence()).getAuctionLength() * 86400000L);

      _onTimeActions.clear();
      addOnTimeAction(0, new StartStopAction("event", true));
      addOnTimeAction(((ClanHall)getResidence()).getAuctionLength() * 86400, new StartStopAction("event", false));

      registerActions();
    }
  }

  public void stopEvent(boolean step)
  {
    List siegeClanObjects = removeObjects("attackers");

    AuctionSiegeClanObject[] clans = (AuctionSiegeClanObject[])siegeClanObjects.toArray(new AuctionSiegeClanObject[siegeClanObjects.size()]);
    Arrays.sort(clans, SiegeClanObject.SiegeClanComparatorImpl.getInstance());

    Clan oldOwner = ((ClanHall)getResidence()).getOwner();
    AuctionSiegeClanObject winnerSiegeClan = clans.length > 0 ? clans[0] : null;

    if (winnerSiegeClan != null)
    {
      SystemMessage2 msg = (SystemMessage2)new SystemMessage2(SystemMsg.THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN).addString(winnerSiegeClan.getClan().getName());
      for (AuctionSiegeClanObject $siegeClan : siegeClanObjects)
      {
        Player player = $siegeClan.getClan().getLeader().getPlayer();
        if (player != null)
          player.sendPacket(msg);
        else {
          PlayerMessageStack.getInstance().mailto($siegeClan.getClan().getLeaderId(), msg);
        }
        if ($siegeClan != winnerSiegeClan)
        {
          long returnBid = $siegeClan.getParam() - ()($siegeClan.getParam() * 0.1D);

          $siegeClan.getClan().getWarehouse().addItem(57, returnBid);
        }
      }

      SiegeClanDAO.getInstance().delete(getResidence());

      if (oldOwner != null) {
        oldOwner.getWarehouse().addItem(57, ((ClanHall)getResidence()).getDeposit());
      }
      ((ClanHall)getResidence()).setAuctionLength(0);
      ((ClanHall)getResidence()).setAuctionMinBid(0L);
      ((ClanHall)getResidence()).setAuctionDescription("");
      ((ClanHall)getResidence()).getSiegeDate().setTimeInMillis(0L);
      ((ClanHall)getResidence()).getLastSiegeDate().setTimeInMillis(0L);
      ((ClanHall)getResidence()).getOwnDate().setTimeInMillis(System.currentTimeMillis());
      ((ClanHall)getResidence()).setJdbcState(JdbcEntityState.UPDATED);

      ((ClanHall)getResidence()).changeOwner(winnerSiegeClan.getClan());
      ((ClanHall)getResidence()).startCycleTask();
    }
    else if (oldOwner != null)
    {
      Player player = oldOwner.getLeader().getPlayer();
      if (player != null)
        player.sendPacket(SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED);
      else {
        PlayerMessageStack.getInstance().mailto(oldOwner.getLeaderId(), SystemMsg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED.packet(null));
      }
    }

    super.stopEvent(step);
  }

  public boolean isParticle(Player player)
  {
    return false;
  }

  public AuctionSiegeClanObject newSiegeClan(String type, int clanId, long param, long date)
  {
    Clan clan = ClanTable.getInstance().getClan(clanId);
    return clan == null ? null : new AuctionSiegeClanObject(type, clan, param, date);
  }

  public Calendar getEndSiegeDate()
  {
    return _endSiegeDate;
  }
}