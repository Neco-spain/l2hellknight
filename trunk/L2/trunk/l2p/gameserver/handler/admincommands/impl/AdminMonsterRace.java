package l2p.gameserver.handler.admincommands.impl;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.entity.MonsterRace;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.DeleteObject;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MonRaceInfo;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.PlaySound.Type;
import l2p.gameserver.utils.Location;

public class AdminMonsterRace
  implements IAdminCommandHandler
{
  protected static int state = -1;

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (fullString.equalsIgnoreCase("admin_mons"))
    {
      if (!activeChar.getPlayerAccess().MonsterRace)
        return false;
      handleSendPacket(activeChar);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void handleSendPacket(Player activeChar)
  {
    int[][] codes = { { -1, 0 }, { 0, 15322 }, { 13765, -1 }, { -1, 0 } };
    MonsterRace race = MonsterRace.getInstance();

    if (state == -1)
    {
      state += 1;
      race.newRace();
      race.newSpeeds();
      activeChar.broadcastPacket(new L2GameServerPacket[] { new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds()) });
    }
    else if (state == 0)
    {
      state += 1;
      activeChar.sendPacket(Msg.THEYRE_OFF);
      activeChar.broadcastPacket(new L2GameServerPacket[] { new PlaySound("S_Race") });

      activeChar.broadcastPacket(new L2GameServerPacket[] { new PlaySound(PlaySound.Type.SOUND, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559)) });
      activeChar.broadcastPacket(new L2GameServerPacket[] { new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds()) });

      ThreadPoolManager.getInstance().schedule(new RunRace(codes, activeChar), 5000L);
    }
  }

  class RunEnd extends RunnableImpl
  {
    private Player activeChar;

    public RunEnd(Player activeChar)
    {
      this.activeChar = activeChar;
    }

    public void runImpl()
      throws Exception
    {
      for (int i = 0; i < 8; i++)
      {
        NpcInstance obj = MonsterRace.getInstance().getMonsters()[i];

        activeChar.broadcastPacket(new L2GameServerPacket[] { new DeleteObject(obj) });
      }

      AdminMonsterRace.state = -1;
    }
  }

  class RunRace extends RunnableImpl
  {
    private int[][] codes;
    private Player activeChar;

    public RunRace(int[][] codes, Player activeChar)
    {
      this.codes = codes;
      this.activeChar = activeChar;
    }

    public void runImpl()
      throws Exception
    {
      activeChar.broadcastPacket(new L2GameServerPacket[] { new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds()) });
      ThreadPoolManager.getInstance().schedule(new AdminMonsterRace.RunEnd(AdminMonsterRace.this, activeChar), 30000L);
    }
  }

  private static enum Commands
  {
    admin_mons;
  }
}