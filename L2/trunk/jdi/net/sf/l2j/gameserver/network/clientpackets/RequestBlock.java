package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestBlock extends L2GameClientPacket
{
  private static final String _C__A0_REQUESTBLOCK = "[C] A0 RequestBlock";
  private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
  private static final int BLOCK = 0;
  private static final int UNBLOCK = 1;
  private static final int BLOCKLIST = 2;
  private static final int ALLBLOCK = 3;
  private static final int ALLUNBLOCK = 4;
  private String _name;
  private Integer _type;
  private L2PcInstance _target;

  protected void readImpl()
  {
    _type = Integer.valueOf(readD());

    if ((_type.intValue() == 0) || (_type.intValue() == 1))
    {
      _name = readS();
      _target = L2World.getInstance().getPlayer(_name);
    }
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    switch (_type.intValue())
    {
    case 0:
    case 1:
      if (_target == null)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST));
        return;
      }

      if (_target.isGM())
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_AN_A_GM));
        return;
      }

      if (_type.intValue() == 0)
        BlockList.addToBlockList(activeChar, _target);
      else
        BlockList.removeFromBlockList(activeChar, _target);
      break;
    case 2:
      BlockList.sendListToOwner(activeChar);
      break;
    case 3:
      BlockList.setBlockAll(activeChar, true);
      break;
    case 4:
      BlockList.setBlockAll(activeChar, false);
      break;
    default:
      _log.info("Unknown 0x0a block type: " + _type);
    }
  }

  public String getType()
  {
    return "[C] A0 RequestBlock";
  }
}