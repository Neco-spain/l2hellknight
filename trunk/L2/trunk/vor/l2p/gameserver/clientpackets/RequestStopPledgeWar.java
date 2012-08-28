package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.ClanTable;

public class RequestStopPledgeWar extends L2GameClientPacket
{
  private String _pledgeName;

  protected void readImpl()
  {
    _pledgeName = readS(32);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Clan playerClan = activeChar.getClan();
    if (playerClan == null) {
      return;
    }
    if ((activeChar.getClanPrivileges() & 0x20) != 32)
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT, ActionFail.STATIC });
      return;
    }

    Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

    if (clan == null)
    {
      activeChar.sendPacket(new IStaticPacket[] { SystemMsg.CLAN_NAME_IS_INVALID, ActionFail.STATIC });
      return;
    }

    if (!playerClan.isAtWarWith(clan.getClanId()))
    {
      activeChar.sendPacket(new IStaticPacket[] { Msg.YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_TO_S1_CLAN, ActionFail.STATIC });
      return;
    }

    for (UnitMember mbr : playerClan) {
      if ((mbr.isOnline()) && (mbr.getPlayer().isInCombat()))
      {
        activeChar.sendPacket(new IStaticPacket[] { Msg.A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE, ActionFail.STATIC });
        return;
      }
    }
    ClanTable.getInstance().stopClanWar(playerClan, clan);
  }
}