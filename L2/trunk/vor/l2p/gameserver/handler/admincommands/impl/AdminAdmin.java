package l2p.gameserver.handler.admincommands.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.instancemanager.SoDManager;
import l2p.gameserver.instancemanager.SoIManager;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.EventTrigger;
import l2p.gameserver.serverpackets.ExChangeClientEffectInfo;
import l2p.gameserver.serverpackets.ExSendUIEvent;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.utils.Location;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminAdmin
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (activeChar.getPlayerAccess().Menu)
    {
      switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminAdmin$Commands[command.ordinal()])
      {
      case 1:
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
        break;
      case 2:
        if (wordList.length == 1)
          activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs.htm"));
        else {
          try
          {
            activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs" + wordList[1] + ".htm"));
          }
          catch (StringIndexOutOfBoundsException e)
          {
          }
        }
      case 3:
        try
        {
          playAdminSound(activeChar, wordList[1]);
        }
        catch (StringIndexOutOfBoundsException e)
        {
        }

      case 4:
        if (activeChar.getMessageRefusal())
        {
          activeChar.unsetVar("gm_silence");
          activeChar.setMessageRefusal(false);
          activeChar.sendPacket(Msg.MESSAGE_ACCEPTANCE_MODE);
          activeChar.sendEtcStatusUpdate();
        }
        else
        {
          if (Config.SAVE_GM_EFFECTS)
            activeChar.setVar("gm_silence", "true", -1L);
          activeChar.setMessageRefusal(true);
          activeChar.sendPacket(Msg.MESSAGE_REFUSAL_MODE);
          activeChar.sendEtcStatusUpdate();
        }
        break;
      case 5:
        try
        {
          if (wordList[1].equalsIgnoreCase("on"))
          {
            activeChar.setTradeRefusal(true);
            Functions.sendDebugMessage(activeChar, "tradeoff enabled");
          }
          else if (wordList[1].equalsIgnoreCase("off"))
          {
            activeChar.setTradeRefusal(false);
            Functions.sendDebugMessage(activeChar, "tradeoff disabled");
          }
        }
        catch (Exception ex)
        {
          if (activeChar.getTradeRefusal())
            Functions.sendDebugMessage(activeChar, "tradeoff currently enabled");
          else {
            Functions.sendDebugMessage(activeChar, "tradeoff currently disabled");
          }
        }
      case 6:
        String html = wordList[1];
        try
        {
          if (html != null)
            activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + html));
          else
            Functions.sendDebugMessage(activeChar, "Html page not found");
        }
        catch (Exception npe)
        {
          Functions.sendDebugMessage(activeChar, "Html page not found");
        }

      case 7:
        if (wordList.length < 2)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //setnpcstate state");
          return false;
        }
GameObject target = activeChar.getTarget();
        int state;
        try { state = Integer.parseInt(wordList[1]);
        }
        catch (NumberFormatException e)
        {
          Functions.sendDebugMessage(activeChar, "You must specify state");
          return false;
        }
        if (!target.isNpc())
        {
          Functions.sendDebugMessage(activeChar, "You must target an NPC");
          return false;
        }
        NpcInstance npc = (NpcInstance)target;
        npc.setNpcState(state);
        break;
      case 8:
        try
        {
          String val = fullString.substring(15).trim();

          String[] vals = val.split(" ");
          int range = NumberUtils.toInt(vals[0], 0);
          astate = vals.length > 1 ? NumberUtils.toInt(vals[1], 0) : 0;

          for (NpcInstance n : activeChar.getAroundNpc(range, 200))
            n.setNpcState(astate);
        }
        catch (Exception e)
        {
          int astate;
          Functions.sendDebugMessage(activeChar, "Usage: //setareanpcstate [range] [state]");
        }

      case 9:
        if (wordList.length < 2)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //showmovie id");
          return false;
        }
        int id;
        try {
          id = Integer.parseInt(wordList[1]);
        }
        catch (NumberFormatException e)
        {
          Functions.sendDebugMessage(activeChar, "You must specify id");
          return false;
        }
        activeChar.showQuestMovie(id);
        break;
      case 10:
        if (wordList.length < 2)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //setzoneinfo id");
          return false;
        }
        int stateid;
        try {
          stateid = Integer.parseInt(wordList[1]);
        }
        catch (NumberFormatException e)
        {
          Functions.sendDebugMessage(activeChar, "You must specify id");
          return false;
        }
        activeChar.broadcastPacket(new L2GameServerPacket[] { new ExChangeClientEffectInfo(stateid) });
        break;
      case 11:
        if (wordList.length < 2)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //eventtrigger id");
          return false;
        }
        int triggerid;
        try {
          triggerid = Integer.parseInt(wordList[1]);
        }
        catch (NumberFormatException e)
        {
          Functions.sendDebugMessage(activeChar, "You must specify id");
          return false;
        }
        activeChar.broadcastPacket(new L2GameServerPacket[] { new EventTrigger(triggerid, true) });
        break;
      case 12:
        GameObject ob = activeChar.getTarget();
        if (!ob.isPlayer())
        {
          Functions.sendDebugMessage(activeChar, "Only player target is allowed");
          return false;
        }
        Player pl = ob.getPlayer();
        List _s = new ArrayList();
        _s.add("==========TARGET STATS:");
        _s.add("==Magic Resist: " + pl.calcStat(Stats.MAGIC_RESIST, null, null));
        _s.add("==Magic Power: " + pl.calcStat(Stats.MAGIC_POWER, 1.0D, null, null));
        _s.add("==Skill Power: " + pl.calcStat(Stats.SKILL_POWER, 1.0D, null, null));
        _s.add("==Cast Break Rate: " + pl.calcStat(Stats.CAST_INTERRUPT, 1.0D, null, null));

        _s.add("==========Powers:");
        _s.add("==Bleed: " + pl.calcStat(Stats.BLEED_POWER, 1.0D, null, null));
        _s.add("==Poison: " + pl.calcStat(Stats.POISON_POWER, 1.0D, null, null));
        _s.add("==Stun: " + pl.calcStat(Stats.STUN_POWER, 1.0D, null, null));
        _s.add("==Root: " + pl.calcStat(Stats.ROOT_POWER, 1.0D, null, null));
        _s.add("==Mental: " + pl.calcStat(Stats.MENTAL_POWER, 1.0D, null, null));
        _s.add("==Sleep: " + pl.calcStat(Stats.SLEEP_POWER, 1.0D, null, null));
        _s.add("==Paralyze: " + pl.calcStat(Stats.PARALYZE_POWER, 1.0D, null, null));
        _s.add("==Cancel: " + pl.calcStat(Stats.CANCEL_POWER, 1.0D, null, null));
        _s.add("==Debuff: " + pl.calcStat(Stats.DEBUFF_POWER, 1.0D, null, null));

        _s.add("==========PvP Stats:");
        _s.add("==Phys Attack Dmg: " + pl.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0D, null, null));
        _s.add("==Phys Skill Dmg: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0D, null, null));
        _s.add("==Magic Skill Dmg: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0D, null, null));
        _s.add("==Phys Attack Def: " + pl.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0D, null, null));
        _s.add("==Phys Skill Def: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0D, null, null));
        _s.add("==Magic Skill Def: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0D, null, null));

        _s.add("==========Reflects:");
        _s.add("==Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, null, null));
        _s.add("==Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, null, null));
        _s.add("==Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, null, null));
        _s.add("==Counterattack: Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_DAMAGE_PERCENT, null, null));
        _s.add("==Counterattack: Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, null, null));
        _s.add("==Counterattack: Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, null, null));

        _s.add("==========MP Consume Rate:");
        _s.add("==Magic Skills: " + pl.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, 1.0D, null, null));
        _s.add("==Phys Skills: " + pl.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, 1.0D, null, null));
        _s.add("==Music: " + pl.calcStat(Stats.MP_DANCE_SKILL_CONSUME, 1.0D, null, null));

        _s.add("==========Shield:");
        _s.add("==Shield Defence: " + pl.calcStat(Stats.SHIELD_DEFENCE, null, null));
        _s.add("==Shield Defence Rate: " + pl.calcStat(Stats.SHIELD_RATE, null, null));
        _s.add("==Shield Defence Angle: " + pl.calcStat(Stats.SHIELD_ANGLE, null, null));

        _s.add("==========Etc:");
        _s.add("==Fatal Blow Rate: " + pl.calcStat(Stats.FATALBLOW_RATE, null, null));
        _s.add("==Phys Skill Evasion Rate: " + pl.calcStat(Stats.PSKILL_EVASION, null, null));
        _s.add("==Counterattack Rate: " + pl.calcStat(Stats.COUNTER_ATTACK, null, null));
        _s.add("==Pole Attack Angle: " + pl.calcStat(Stats.POLE_ATTACK_ANGLE, null, null));
        _s.add("==Pole Target Count: " + pl.calcStat(Stats.POLE_TARGET_COUNT, 1.0D, null, null));
        _s.add("==========DONE.");

        for (String s : _s)
          Functions.sendDebugMessage(activeChar, s);
        break;
      case 13:
        if (wordList.length < 5)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //uievent isHide doIncrease startTime endTime Text");
          return false; } boolean hide;
        boolean increase;
        int startTime;
        int endTime;
        String text;
        try { hide = Boolean.parseBoolean(wordList[1]);
          increase = Boolean.parseBoolean(wordList[2]);
          startTime = Integer.parseInt(wordList[3]);
          endTime = Integer.parseInt(wordList[4]);
          text = wordList[5];
        }
        catch (NumberFormatException e)
        {
          Functions.sendDebugMessage(activeChar, "Invalid format");
          return false;
        }
        activeChar.broadcastPacket(new L2GameServerPacket[] { new ExSendUIEvent(activeChar, hide, increase, startTime, endTime, new String[] { text }) });
        break;
      case 14:
        if (wordList.length < 1)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //opensod minutes");
          return false;
        }
        SoDManager.openSeed(Integer.parseInt(wordList[1]) * 60 * 1000L);
        break;
      case 15:
        SoDManager.closeSeed();
        break;
      case 16:
        if (wordList.length < 1)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //setsoistage stage[1-5]");
          return false;
        }
        SoIManager.setCurrentStage(Integer.parseInt(wordList[1]));
        break;
      case 17:
        if (wordList.length < 1)
        {
          Functions.sendDebugMessage(activeChar, "USAGE: //soinotify [1-3]");
          return false;
        }
        switch (Integer.parseInt(wordList[1]))
        {
        case 1:
          SoIManager.notifyCohemenesKill();
          break;
        case 2:
          SoIManager.notifyEkimusKill();
          break;
        case 3:
          SoIManager.notifyHoEDefSuccess();
        }

        break;
      case 18:
        GameObject obj2 = activeChar.getTarget();
        if (!obj2.isNpc())
        {
          Functions.sendDebugMessage(activeChar, "Only NPC target is allowed");
          return false;
        }
        ((NpcInstance)obj2).broadcastCharInfo();
        break;
      case 19:
        Functions.sendDebugMessage(activeChar, "Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
        break;
      case 20:
        StringTokenizer st = new StringTokenizer(fullString, " ");
        try
        {
          st.nextToken();
          try
          {
            new File("dumps").mkdir();
            File f = new File("dumps/locdump.txt");
            if (!f.exists())
              f.createNewFile();
            Functions.sendDebugMessage(activeChar, "Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
            FileWriter writer = new FileWriter(f, true);
            writer.write("Loc: " + activeChar.getLoc().x + ", " + activeChar.getLoc().y + ", " + activeChar.getLoc().z + "\n");
            writer.close();
          }
          catch (Exception e)
          {
          }

        }
        catch (Exception e)
        {
        }

      case 21:
        if (activeChar.isUndying())
        {
          activeChar.setUndying(false);
          Functions.sendDebugMessage(activeChar, "Undying state has been disabled.");
        }
        else
        {
          activeChar.setUndying(true);
          Functions.sendDebugMessage(activeChar, "Undying state has been enabled.");
        }
      }

      return true;
    }

    if (activeChar.getPlayerAccess().CanTeleport)
    {
      switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminAdmin$Commands[command.ordinal()])
      {
      case 6:
        String html = wordList[1];
        try
        {
          if (html != null) {
            if (html.startsWith("tele"))
              activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + html));
            else
              activeChar.sendMessage("Access denied");
          }
          else activeChar.sendMessage("Html page not found");
        }
        catch (Exception npe)
        {
          activeChar.sendMessage("Html page not found");
        }
      }

      return true;
    }

    return false;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  public void playAdminSound(Player activeChar, String sound)
  {
    activeChar.broadcastPacket(new L2GameServerPacket[] { new PlaySound(sound) });
    activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
    activeChar.sendMessage("Playing " + sound + ".");
  }

  private static enum Commands
  {
    admin_admin, 
    admin_play_sounds, 
    admin_play_sound, 
    admin_silence, 
    admin_tradeoff, 
    admin_cfg, 
    admin_config, 
    admin_show_html, 
    admin_setnpcstate, 
    admin_setareanpcstate, 
    admin_showmovie, 
    admin_setzoneinfo, 
    admin_eventtrigger, 
    admin_debug, 
    admin_uievent, 
    admin_opensod, 
    admin_closesod, 
    admin_setsoistage, 
    admin_soinotify, 
    admin_forcenpcinfo, 
    admin_loc, 
    admin_locdump, 
    admin_undying;
  }
}