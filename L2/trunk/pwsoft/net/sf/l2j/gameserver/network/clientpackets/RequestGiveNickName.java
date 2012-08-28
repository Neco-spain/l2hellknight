package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestGiveNickName extends L2GameClientPacket
{
  private String _target;
  private String _title;

  protected void readImpl()
  {
    _target = readS();
    _title = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.getRequestGiveNickName() < 200L)
    {
      player.logout();
      return;
    }

    player.setRequestGiveNickName();

    if (_title.length() > 16)
    {
      player.sendPacket(Static.NAMING_CHARNAME_UP_TO_16CHARS);
      return;
    }

    if ((player.isNoble()) && (_target.equalsIgnoreCase(player.getName())))
    {
      player.setTitle(_title);
      player.sendPacket(Static.TITLE_CHANGED);
      player.broadcastTitleInfo();
      return;
    }

    if ((player.getClan() == null) || (player.getClan().getLevel() < 3))
    {
      player.sendPacket(Static.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
      return;
    }

    if ((player.getClanPrivileges() & 0x4) == 4)
    {
      L2ClanMember member1 = player.getClan().getClanMember(_target);
      if (member1 == null)
      {
        player.sendPacket(Static.PLAYER_NOT_IN_YOR_CLAN);
        return;
      }

      L2PcInstance member = member1.getPlayerInstance();
      if (member == null)
      {
        player.sendPacket(Static.PLAYER_NOT_IN_GAME);
        return;
      }

      member.setTitle(_title);
      member.sendPacket(Static.TITLE_CHANGED);
      member.broadcastTitleInfo();
    }
  }
}