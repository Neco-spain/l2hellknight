package l2m.gameserver.serverpackets;

import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
  private int clan_id;
  private int clan_level;
  private int clan_rank;
  private int clan_rep;
  private int crest_id;
  private int ally_id;
  private int ally_crest;
  private int atwar;
  private int _territorySide;
  private String ally_name = "";
  private int HasCastle;
  private int HasHideout;
  private int HasFortress;

  public PledgeShowInfoUpdate(Clan clan)
  {
    clan_id = clan.getClanId();
    clan_level = clan.getLevel();
    HasCastle = clan.getCastle();
    HasHideout = clan.getHasHideout();
    HasFortress = clan.getHasFortress();
    clan_rank = clan.getRank();
    clan_rep = clan.getReputationScore();
    crest_id = clan.getCrestId();
    ally_id = clan.getAllyId();
    atwar = clan.isAtWar();
    _territorySide = clan.getWarDominion();
    Alliance ally = clan.getAlliance();
    if (ally != null)
    {
      ally_name = ally.getAllyName();
      ally_crest = ally.getAllyCrestId();
    }
  }

  protected final void writeImpl()
  {
    writeC(142);

    writeD(clan_id);
    writeD(crest_id);
    writeD(clan_level);
    writeD(HasCastle);
    writeD(HasHideout);
    writeD(HasFortress);
    writeD(clan_rank);
    writeD(clan_rep);
    writeD(0);
    writeD(0);
    writeD(ally_id);
    writeS(ally_name);
    writeD(ally_crest);
    writeD(atwar);

    writeD(0);
    writeD(_territorySide);
  }
}