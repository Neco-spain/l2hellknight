package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.RankPrivs;

public class ManagePledgePower extends L2GameServerPacket
{
  private int _action;
  private int _clanId;
  private int privs;

  public ManagePledgePower(Player player, int action, int rank)
  {
    _clanId = player.getClanId();
    _action = action;
    RankPrivs temp = player.getClan().getRankPrivs(rank);
    privs = (temp == null ? 0 : temp.getPrivs());
    player.sendPacket(new PledgeReceiveUpdatePower(privs));
  }

  protected final void writeImpl()
  {
    writeC(42);
    writeD(_clanId);
    writeD(_action);
    writeD(privs);
  }
}