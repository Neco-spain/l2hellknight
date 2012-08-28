package l2p.gameserver.model.entity.events.impl;

import l2p.commons.collections.MultiValueSet;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class ClanHallNpcSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject>
{
  public ClanHallNpcSiegeEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void startEvent()
  {
    _oldOwner = ((ClanHall)getResidence()).getOwner();

    broadcastInZone(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()) });

    super.startEvent();
  }

  public void stopEvent(boolean step)
  {
    Clan newOwner = ((ClanHall)getResidence()).getOwner();
    if (newOwner != null)
    {
      if (_oldOwner != newOwner)
      {
        newOwner.broadcastToOnlineMembers(new L2GameServerPacket[] { PlaySound.SIEGE_VICTORY });

        newOwner.incReputation(1700, false, toString());

        if (_oldOwner != null) {
          _oldOwner.incReputation(-1700, false, toString());
        }
      }
      broadcastInZone(new L2GameServerPacket[] { ((SystemMessage2)new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName())).addResidenceName(getResidence()) });
      broadcastInZone(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()) });
    }
    else {
      broadcastInZone(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()) });
    }
    super.stopEvent(step);

    _oldOwner = null;
  }

  public void processStep(Clan clan)
  {
    if (clan != null) {
      ((ClanHall)getResidence()).changeOwner(clan);
    }
    stopEvent(true);
  }

  public void loadSiegeClans()
  {
  }
}