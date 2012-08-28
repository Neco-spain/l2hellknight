package scripts.commands.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.MonsterRace;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.MonRaceInfo;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminMonsterRace
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_mons" };

  private static final int REQUIRED_LEVEL = Config.GM_MONSTERRACE;
  protected static int state = -1;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if ((!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))
      {
        return false;
      }
    }

    if (command.equalsIgnoreCase("admin_mons"))
    {
      handleSendPacket(activeChar);
    }
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }

  private void handleSendPacket(L2PcInstance activeChar)
  {
    int[][] codes = { { -1, 0 }, { 0, 15322 }, { 13765, -1 }, { -1, 0 } };
    MonsterRace race = MonsterRace.getInstance();

    if (state == -1)
    {
      state += 1;
      race.newRace();
      race.newSpeeds();
      MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());

      activeChar.sendPacket(spk);
      activeChar.broadcastPacket(spk);
    }
    else if (state == 0)
    {
      state += 1;
      SystemMessage sm = SystemMessage.id(SystemMessageId.MONSRACE_RACE_START);
      sm.addNumber(0);
      activeChar.sendPacket(sm);
      PlaySound SRace = new PlaySound(1, "S_Race", 0, 0, 0, 0, 0);
      activeChar.sendPacket(SRace);
      activeChar.broadcastPacket(SRace);
      PlaySound SRace2 = new PlaySound(0, "ItemSound2.race_start", 1, 121209259, 12125, 182487, -3559);

      activeChar.sendPacket(SRace2);
      activeChar.broadcastPacket(SRace2);
      MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());

      activeChar.sendPacket(spk);
      activeChar.broadcastPacket(spk);

      ThreadPoolManager.getInstance().scheduleGeneral(new RunRace(codes, activeChar), 5000L);
    }
  }

  private static class RunEnd
    implements Runnable
  {
    private L2PcInstance activeChar;

    public RunEnd(L2PcInstance pActiveChar)
    {
      activeChar = pActiveChar;
    }

    public void run()
    {
      DeleteObject obj = null;
      for (int i = 0; i < 8; i++)
      {
        obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
        activeChar.sendPacket(obj);
        activeChar.broadcastPacket(obj);
      }
      AdminMonsterRace.state = -1;
    }
  }

  class RunRace
    implements Runnable
  {
    private int[][] codes;
    private L2PcInstance activeChar;

    public RunRace(int[][] pCodes, L2PcInstance pActiveChar)
    {
      codes = pCodes;
      activeChar = pActiveChar;
    }

    public void run()
    {
      MonRaceInfo spk = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());

      activeChar.sendPacket(spk);
      activeChar.broadcastPacket(spk);
      ThreadPoolManager.getInstance().scheduleGeneral(new AdminMonsterRace.RunEnd(activeChar), 30000L);
    }
  }
}