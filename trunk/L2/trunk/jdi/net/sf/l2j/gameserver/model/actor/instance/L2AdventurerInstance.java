package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.ExQuestInfo;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2AdventurerInstance extends L2FolkInstance
{
  public L2AdventurerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("npcfind_byid"))
    {
      try
      {
        int bossId = Integer.parseInt(command.substring(12).trim());
        switch (1.$SwitchMap$net$sf$l2j$gameserver$instancemanager$RaidBossSpawnManager$StatusEnum[RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId).ordinal()])
        {
        case 1:
        case 2:
          L2Spawn spawn = (L2Spawn)RaidBossSpawnManager.getInstance().getSpawns().get(Integer.valueOf(bossId));
          player.sendPacket(new RadarControl(0, 1, spawn.getLocx(), spawn.getLocy(), spawn.getLocz()));

          break;
        case 3:
          player.sendMessage("This Boss isn't in game - notify L2J Eon Dev Team");
        }

      }
      catch (NumberFormatException e)
      {
        _log.warning("Invalid Bypass to Server command parameter.");
      }
    }
    else if (command.startsWith("raidInfo"))
    {
      int bossLevel = Integer.parseInt(command.substring(9).trim());
      String filename = "data/html/adventurer_guildsman/raid_info/info.htm";
      if (bossLevel != 0) filename = "data/html/adventurer_guildsman/raid_info/level" + bossLevel + ".htm";
      showChatWindow(player, bossLevel, filename);
    }
    else if (command.equalsIgnoreCase("questlist"))
    {
      player.sendPacket(new ExQuestInfo());
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";

    if (val == 0) pom = "" + npcId; else {
      pom = npcId + "-" + val;
    }
    return "data/html/adventurer_guildsman/" + pom + ".htm";
  }

  private void showChatWindow(L2PcInstance player, int bossLevel, String filename) {
    showChatWindow(player, filename);
  }
}