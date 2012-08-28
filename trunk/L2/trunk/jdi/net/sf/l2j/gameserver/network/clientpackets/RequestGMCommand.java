package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.GMViewCharacterInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewItemList;
import net.sf.l2j.gameserver.network.serverpackets.GMViewPledgeInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewQuestList;
import net.sf.l2j.gameserver.network.serverpackets.GMViewSkillInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public final class RequestGMCommand extends L2GameClientPacket
{
  private static final String _C__6E_REQUESTGMCOMMAND = "[C] 6e RequestGMCommand";
  static Logger _log = Logger.getLogger(RequestGMCommand.class.getName());
  private String _targetName;
  private int _command;

  protected void readImpl()
  {
    _targetName = readS();
    _command = readD();
  }

  protected void runImpl()
  {
    if ((!((L2GameClient)getClient()).getActiveChar().isGM()) || (((L2GameClient)getClient()).getActiveChar().getAccessLevel() < Config.GM_ALTG_MIN_LEVEL)) {
      return;
    }
    L2PcInstance player = L2World.getInstance().getPlayer(_targetName);

    if (player == null) {
      return;
    }
    switch (_command)
    {
    case 1:
      sendPacket(new GMViewCharacterInfo(player));
      break;
    case 2:
      if (player.getClan() == null) break;
      sendPacket(new GMViewPledgeInfo(player.getClan(), player)); break;
    case 3:
      sendPacket(new GMViewSkillInfo(player));
      break;
    case 4:
      sendPacket(new GMViewQuestList(player));
      break;
    case 5:
      sendPacket(new GMViewItemList(player));
      break;
    case 6:
      sendPacket(new GMViewWarehouseWithdrawList(player));
    }
  }

  public String getType()
  {
    return "[C] 6e RequestGMCommand";
  }
}