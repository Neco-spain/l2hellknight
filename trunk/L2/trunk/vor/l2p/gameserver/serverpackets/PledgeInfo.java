package l2p.gameserver.serverpackets;

import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;

public class PledgeInfo extends L2GameServerPacket
{
  private int clan_id;
  private String clan_name;
  private String ally_name;

  public PledgeInfo(Clan clan)
  {
    clan_id = clan.getClanId();
    clan_name = clan.getName();
    ally_name = (clan.getAlliance() == null ? "" : clan.getAlliance().getAllyName());
  }

  protected final void writeImpl()
  {
    writeC(137);
    writeD(clan_id);
    writeS(clan_name);
    writeS(ally_name);
  }
}