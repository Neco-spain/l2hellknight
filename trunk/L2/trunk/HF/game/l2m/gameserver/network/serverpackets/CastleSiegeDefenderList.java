package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.entity.events.objects.SiegeClanObject;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;

public class CastleSiegeDefenderList extends L2GameServerPacket
{
  public static int OWNER = 1;
  public static int WAITING = 2;
  public static int ACCEPTED = 3;
  public static int REFUSE = 4;
  private int _id;
  private int _registrationValid;
  private List<DefenderClan> _defenderClans = Collections.emptyList();

  public CastleSiegeDefenderList(Castle castle)
  {
    _id = castle.getId();
    _registrationValid = ((!castle.getSiegeEvent().isRegistrationOver()) && (castle.getOwner() != null) ? 1 : 0);

    List defenders = castle.getSiegeEvent().getObjects("defenders");
    List defendersWaiting = castle.getSiegeEvent().getObjects("defenders_waiting");
    List defendersRefused = castle.getSiegeEvent().getObjects("defenders_refused");
    _defenderClans = new ArrayList(defenders.size() + defendersWaiting.size() + defendersRefused.size());
    if (castle.getOwner() != null)
      _defenderClans.add(new DefenderClan(castle.getOwner(), OWNER, 0));
    for (SiegeClanObject siegeClan : defenders)
      _defenderClans.add(new DefenderClan(siegeClan.getClan(), ACCEPTED, (int)(siegeClan.getDate() / 1000L)));
    for (SiegeClanObject siegeClan : defendersWaiting)
      _defenderClans.add(new DefenderClan(siegeClan.getClan(), WAITING, (int)(siegeClan.getDate() / 1000L)));
    for (SiegeClanObject siegeClan : defendersRefused)
      _defenderClans.add(new DefenderClan(siegeClan.getClan(), REFUSE, (int)(siegeClan.getDate() / 1000L)));
  }

  protected final void writeImpl()
  {
    writeC(203);
    writeD(_id);
    writeD(0);
    writeD(_registrationValid);
    writeD(0);

    writeD(_defenderClans.size());
    writeD(_defenderClans.size());
    for (DefenderClan defenderClan : _defenderClans)
    {
      Clan clan = defenderClan._clan;

      writeD(clan.getClanId());
      writeS(clan.getName());
      writeS(clan.getLeaderName());
      writeD(clan.getCrestId());
      writeD(defenderClan._time);
      writeD(defenderClan._type);
      writeD(clan.getAllyId());
      Alliance alliance = clan.getAlliance();
      if (alliance != null)
      {
        writeS(alliance.getAllyName());
        writeS(alliance.getAllyLeaderName());
        writeD(alliance.getAllyCrestId());
      }
      else
      {
        writeS("");
        writeS("");
        writeD(0);
      }
    }
  }

  private static class DefenderClan {
    private Clan _clan;
    private int _type;
    private int _time;

    public DefenderClan(Clan clan, int type, int time) {
      _clan = clan;
      _type = type;
      _time = time;
    }
  }
}