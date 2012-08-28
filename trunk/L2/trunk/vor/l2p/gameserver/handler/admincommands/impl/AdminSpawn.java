package l2p.gameserver.handler.admincommands.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2p.commons.collections.MultiValueSet;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

public class AdminSpawn
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditNPC)
      return false;
    StringTokenizer st;
    NpcInstance target;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminSpawn$Commands[command.ordinal()])
    {
    case 1:
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns.htm"));
      break;
    case 2:
      try
      {
        String val = fullString.substring(18);
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns/" + val + ".htm"));
      }
      catch (StringIndexOutOfBoundsException e)
      {
      }

    case 3:
      st = new StringTokenizer(fullString, " ");
      try
      {
        st.nextToken();
        String id = st.nextToken();
        int mobCount = 1;
        if (st.hasMoreTokens())
          mobCount = Integer.parseInt(st.nextToken());
        spawnMonster(activeChar, id, 0, mobCount);
      }
      catch (Exception e)
      {
      }

    case 4:
    case 5:
      st = new StringTokenizer(fullString, " ");
      try
      {
        st.nextToken();
        String id = st.nextToken();
        int respawnTime = 30;
        int mobCount = 1;
        if (st.hasMoreTokens())
          mobCount = Integer.parseInt(st.nextToken());
        if (st.hasMoreTokens())
          respawnTime = Integer.parseInt(st.nextToken());
        spawnMonster(activeChar, id, respawnTime, mobCount);
      }
      catch (Exception e)
      {
      }

    case 6:
      if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isNpc()))
      {
        activeChar.sendMessage("Please select target NPC or mob.");
        return false;
      }

      st = new StringTokenizer(fullString, " ");
      st.nextToken();
      if (!st.hasMoreTokens())
      {
        activeChar.sendMessage("Please specify AI name.");
        return false;
      }
      String aiName = st.nextToken();
      target = (NpcInstance)activeChar.getTarget();

      Constructor aiConstructor = null;
      try
      {
        if (!aiName.equalsIgnoreCase("npc"))
          aiConstructor = java.lang.Class.forName("l2p.gameserver.ai." + aiName).getConstructors()[0];
      }
      catch (Exception e)
      {
        try
        {
          aiConstructor = ((java.lang.Class)l2p.gameserver.scripts.Scripts.getInstance().getClasses().get("ai." + aiName)).getConstructors()[0];
        }
        catch (Exception e1)
        {
          activeChar.sendMessage("This type AI not found.");
          return false;
        }
      }

      if (aiConstructor != null)
      {
        try
        {
          target.setAI((CharacterAI)aiConstructor.newInstance(new Object[] { target }));
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        target.getAI().startAITask(); } break;
    case 7:
      if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isNpc()))
      {
        activeChar.sendMessage("Please select target NPC or mob.");
        return false;
      }

      st = new StringTokenizer(fullString, " ");
      st.nextToken();

      if (!st.hasMoreTokens())
      {
        activeChar.sendMessage("Please specify AI parameter name.");
        activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
        return false;
      }

      String paramName = st.nextToken();
      if (!st.hasMoreTokens())
      {
        activeChar.sendMessage("Please specify AI parameter value.");
        activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
        return false;
      }
      String paramValue = st.nextToken();
      target = (NpcInstance)activeChar.getTarget();
      target.setParameter(paramName, paramValue);
      target.decayMe();
      target.spawnMe();
      activeChar.sendMessage("AI parameter " + paramName + " succesfully setted to " + paramValue);
      break;
    case 8:
      if ((activeChar.getTarget() == null) || (!activeChar.getTarget().isNpc()))
      {
        activeChar.sendMessage("Please select target NPC or mob.");
        return false;
      }
      target = (NpcInstance)activeChar.getTarget();
      MultiValueSet set = target.getParameters();
      if (!set.isEmpty())
        System.out.println("Dump of Parameters:\r\n" + set.toString());
      else
        System.out.println("Parameters is empty.");
      break;
    case 9:
      GameObject obj = activeChar.getTarget();
      if (!obj.isNpc())
      {
        activeChar.sendMessage("Target is incorrect!");
        return false;
      }

      NpcInstance npc = (NpcInstance)obj;
      npc.setHeading(activeChar.getHeading());
      npc.decayMe();
      npc.spawnMe();
      activeChar.sendMessage("New heading : " + activeChar.getHeading());

      Spawner spawn = npc.getSpawn();
      if (spawn == null)
      {
        activeChar.sendMessage("Spawn for this npc == null!");
        return false;
      }

    case 10:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("Incorrect argument count!");
        return false;
      }

      int id = Integer.parseInt(wordList[1]);
      int id2 = 0;
      if (wordList.length > 2) {
        id2 = Integer.parseInt(wordList[2]);
      }
      int min_x = -2147483648;
      int min_y = -2147483648;
      int min_z = -2147483648;
      int max_x = 2147483647;
      int max_y = 2147483647;
      int max_z = 2147483647;

      String name = "";

      for (NpcInstance _npc : World.getAroundNpc(activeChar)) {
        if ((_npc.getNpcId() == id) || (_npc.getNpcId() == id2))
        {
          name = _npc.getName();
          min_x = Math.min(min_x, _npc.getX());
          min_y = Math.min(min_y, _npc.getY());
          min_z = Math.min(min_z, _npc.getZ());
          max_x = Math.max(max_x, _npc.getX());
          max_y = Math.max(max_y, _npc.getY());
          max_z = Math.max(max_z, _npc.getZ());
        }
      }
      min_x -= 500;
      min_y -= 500;
      max_x += 500;
      max_y += 500;

      System.out.println("(0,'" + name + "'," + min_x + "," + min_y + "," + min_z + "," + max_z + ",0),");
      System.out.println("(0,'" + name + "'," + min_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
      System.out.println("(0,'" + name + "'," + max_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
      System.out.println("(0,'" + name + "'," + max_x + "," + min_y + "," + min_z + "," + max_z + ",0),");

      System.out.println("delete from spawnlist where npc_templateid in (" + id + ", " + id2 + ")" + " and locx <= " + min_x + " and locy <= " + min_y + " and locz <= " + min_z + " and locx >= " + max_x + " and locy >= " + max_y + " and locz >= " + max_z + ";");

      break;
    case 11:
      st = new StringTokenizer(fullString, " ");
      try
      {
        st.nextToken();
        String id3 = st.nextToken();
        int respawnTime = 30;
        int mobCount = 1;
        spawnMonster(activeChar, id3, respawnTime, mobCount);
        try
        {
          new File("dumps").mkdir();
          File f = new File("dumps/spawndump.txt");
          if (!f.exists())
            f.createNewFile();
          FileWriter writer = new FileWriter(f, true);
          writer.write("<spawn count=\"1\" respawn=\"60\" respawn_random=\"0\" period_of_day=\"none\">\n\t<point x=\"" + activeChar.getLoc().x + "\" y=\"" + activeChar.getLoc().y + "\" z=\"" + activeChar.getLoc().z + "\" h=\"" + activeChar.getLoc().h + "\" />\n\t" + "<npc id=\"" + Integer.parseInt(id3) + "\" /><!--" + NpcHolder.getInstance().getTemplate(Integer.parseInt(id3)).getName() + "-->\n" + "</spawn>\n");
          writer.close();
        }
        catch (Exception e)
        {
        }

      }
      catch (Exception e)
      {
      }

    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void spawnMonster(Player activeChar, String monsterId, int respawnTime, int mobCount)
  {
    GameObject target = activeChar.getTarget();
    if (target == null) {
      target = activeChar;
    }
    Pattern pattern = Pattern.compile("[0-9]*");
    Matcher regexp = pattern.matcher(monsterId);
    NpcTemplate template;
    NpcTemplate template;
    if (regexp.matches())
    {
      int monsterTemplate = Integer.parseInt(monsterId);
      template = NpcHolder.getInstance().getTemplate(monsterTemplate);
    }
    else
    {
      monsterId = monsterId.replace('_', ' ');
      template = NpcHolder.getInstance().getTemplateByName(monsterId);
    }

    if (template == null)
    {
      activeChar.sendMessage("Incorrect monster template.");
      return;
    }

    try
    {
      SimpleSpawner spawn = new SimpleSpawner(template);
      spawn.setLoc(target.getLoc());
      spawn.setAmount(mobCount);
      spawn.setHeading(activeChar.getHeading());
      spawn.setRespawnDelay(respawnTime);
      spawn.setReflection(activeChar.getReflection());

      if (RaidBossSpawnManager.getInstance().isDefined(template.getNpcId())) {
        activeChar.sendMessage("Raid Boss " + template.name + " already spawned.");
      }
      else {
        spawn.init();
        if (respawnTime == 0)
          spawn.stopRespawn();
        activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
      }
    }
    catch (Exception e)
    {
      activeChar.sendMessage("Target is not ingame.");
    }
  }

  private static enum Commands
  {
    admin_show_spawns, 
    admin_spawn, 
    admin_spawn_monster, 
    admin_spawn_index, 
    admin_spawn1, 
    admin_setheading, 
    admin_setai, 
    admin_setaiparam, 
    admin_dumpparams, 
    admin_generate_loc, 
    admin_dumpspawn;
  }
}