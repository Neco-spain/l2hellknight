package l2p.gameserver.model.instances;

import java.util.Map;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.serverpackets.ExShowQuestInfo;
import l2p.gameserver.serverpackets.RadarControl;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.templates.spawn.SpawnRange;
import l2p.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdventurerInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  private static final Logger _log = LoggerFactory.getLogger(AdventurerInstance.class);

  public AdventurerInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (command.startsWith("npcfind_byid"))
    {
      try {
        int bossId = Integer.parseInt(command.substring(12).trim());
        switch (1.$SwitchMap$l2p$gameserver$instancemanager$RaidBossSpawnManager$Status[RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId).ordinal()])
        {
        case 1:
        case 2:
          Spawner spawn = (Spawner)RaidBossSpawnManager.getInstance().getSpawnTable().get(Integer.valueOf(bossId));

          Location loc = spawn.getCurrentSpawnRange().getRandomLoc(spawn.getReflection().getGeoIndex());

          player.sendPacket(new IStaticPacket[] { new RadarControl(2, 2, loc), new RadarControl(0, 1, loc) });
          break;
        case 3:
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2AdventurerInstance.BossNotInGame", player, new Object[0]).addNumber(bossId));
        }

      }
      catch (NumberFormatException e)
      {
        _log.warn("AdventurerInstance: Invalid Bypass to Server command parameter.");
      }
    } else if (command.startsWith("raidInfo"))
    {
      int bossLevel = Integer.parseInt(command.substring(9).trim());

      String filename = "adventurer_guildsman/raid_info/info.htm";
      if (bossLevel != 0) {
        filename = "adventurer_guildsman/raid_info/level" + bossLevel + ".htm";
      }
      showChatWindow(player, filename, new Object[0]);
    }
    else if (command.equalsIgnoreCase("questlist")) {
      player.sendPacket(ExShowQuestInfo.STATIC);
    } else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }
    return "adventurer_guildsman/" + pom + ".htm";
  }
}