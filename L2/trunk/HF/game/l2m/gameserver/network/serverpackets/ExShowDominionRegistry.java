package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.entity.residence.Dominion;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;

public class ExShowDominionRegistry extends L2GameServerPacket
{
  private int _dominionId;
  private String _ownerClanName;
  private String _ownerLeaderName;
  private String _ownerAllyName;
  private int _clanReq;
  private int _mercReq;
  private int _warTime;
  private int _currentTime;
  private boolean _registeredAsPlayer;
  private boolean _registeredAsClan;
  private List<TerritoryFlagsInfo> _flags = Collections.emptyList();

  public ExShowDominionRegistry(Player activeChar, Dominion dominion)
  {
    _dominionId = dominion.getId();

    Clan owner = dominion.getOwner();
    Alliance alliance = owner.getAlliance();

    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)dominion.getSiegeEvent();
    _ownerClanName = owner.getName();
    _ownerLeaderName = owner.getLeaderName();
    _ownerAllyName = (alliance == null ? "" : alliance.getAllyName());
    _warTime = (int)(dominion.getSiegeDate().getTimeInMillis() / 1000L);
    _currentTime = (int)(System.currentTimeMillis() / 1000L);
    _mercReq = siegeEvent.getObjects("defender_players").size();
    _clanReq = (siegeEvent.getObjects("defenders").size() + 1);
    _registeredAsPlayer = siegeEvent.getObjects("defender_players").contains(Integer.valueOf(activeChar.getObjectId()));
    _registeredAsClan = (siegeEvent.getSiegeClan("defenders", activeChar.getClan()) != null);

    List dominions = ResidenceHolder.getInstance().getResidenceList(Dominion.class);
    _flags = new ArrayList(dominions.size());
    for (Dominion d : dominions)
      _flags.add(new TerritoryFlagsInfo(d.getId(), d.getFlags()));
  }

  protected void writeImpl()
  {
    writeEx(144);

    writeD(_dominionId);
    writeS(_ownerClanName);
    writeS(_ownerLeaderName);
    writeS(_ownerAllyName);
    writeD(_clanReq);
    writeD(_mercReq);
    writeD(_warTime);
    writeD(_currentTime);
    writeD(_registeredAsClan);
    writeD(_registeredAsPlayer);
    writeD(1);
    writeD(_flags.size());
    for (TerritoryFlagsInfo cf : _flags)
    {
      writeD(cf.id);
      writeD(cf.flags.length);
      for (int flag : cf.flags)
        writeD(flag);
    }
  }

  private class TerritoryFlagsInfo {
    public int id;
    public int[] flags;

    public TerritoryFlagsInfo(int id_, int[] flags_) {
      id = id_;
      flags = flags_;
    }
  }
}