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
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if ((!player.isGM()) || (player.getAccessLevel() < Config.GM_ALTG_MIN_LEVEL)) {
      return;
    }
    if (player.isParalyzed()) {
      return;
    }
    L2PcInstance target = L2World.getInstance().getPlayer(_targetName);

    if (target == null) {
      return;
    }
    switch (_command)
    {
    case 1:
      sendPacket(new GMViewCharacterInfo(target));
      break;
    case 2:
      if (target.getClan() == null) break;
      sendPacket(new GMViewPledgeInfo(target.getClan(), target)); break;
    case 3:
      sendPacket(new GMViewSkillInfo(target));
      break;
    case 4:
      sendPacket(new GMViewQuestList(target));
      break;
    case 5:
      sendPacket(new GMViewItemList(target));
      break;
    case 6:
      sendPacket(new GMViewWarehouseWithdrawList(target));
    }
  }
}