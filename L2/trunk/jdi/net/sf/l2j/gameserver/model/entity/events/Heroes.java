package net.sf.l2j.gameserver.model.entity.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.serverpackets.SkillList;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

public class Heroes
{
  protected static Logger _log = Logger.getLogger(Heroes.class.getName());
  private Map heroes;
  private static Heroes ins;

  public Heroes()
  {
    heroes = new FastMap();
  }

  public static Heroes getInstance()
  {
    if (ins == null)
      ins = new Heroes();
    return ins;
  }

  public boolean hasHeroes()
  {
    return heroes.size() > 0;
  }

  public Map getHeroes()
  {
    return heroes;
  }

  public void removeHero(int id)
  {
    heroes.remove(Integer.valueOf(id));
  }

  public void removeHero(L2PcInstance client)
  {
    removeHero(client.getObjectId());
    client.setHero(false);
    L2Skill[] al2skill;
    int j = (al2skill = HeroSkillTable.GetHeroSkills()).length;
    for (int i = 0; i < j; i++)
    {
      L2Skill skill = al2skill[i];
      client.removeSkill(skill);
    }

    client.sendPacket(new SkillList());
    client.broadcastUserInfo();
  }

  public void addHero(L2PcInstance client, int days)
  {
    Calendar t = Calendar.getInstance();
    t.add(10, 24 * days);
    heroes.put(Integer.valueOf(client.getObjectId()), Long.valueOf(t.getTimeInMillis()));
    client.broadcastPacket(new SocialAction(client.getObjectId(), 16));
    onEnterWorld(client);
    saveHeroes();
  }

  public boolean timeIsExpired(int id)
  {
    if (!hasAccess(id))
      return true;
    long target = ((Long)heroes.get(Integer.valueOf(id))).longValue();
    Calendar now = Calendar.getInstance();
    if (target - now.getTimeInMillis() > 0L)
    {
      return false;
    }

    removeHero(id);
    return true;
  }

  public boolean hasAccess(int id)
  {
    return heroes.get(Integer.valueOf(id)) != null;
  }

  public void onEnterWorld(L2PcInstance client)
  {
    if (timeIsExpired(client.getObjectId()))
    {
      removeHero(client.getObjectId());
      return;
    }
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(((Long)heroes.get(Integer.valueOf(client.getObjectId()))).longValue());
    client.sendMessage("\u041E\u0431\u043D\u0443\u043B\u0435\u043D\u0438\u0435 \u0432\u0430\u0448\u0435\u0433\u043E \u0441\u0442\u0430\u0442\u0443\u0441\u0430 \u0433\u0435\u0440\u043E\u044F \u043F\u0440\u043E\u0438\u0437\u043E\u0439\u0434\u0435\u0442: " + date.getTime());
    client.setHero(true);

    if (client.getBaseClass() == client.getClassId().getId())
    {
      L2Skill[] al2skill;
      int j = (al2skill = HeroSkillTable.GetHeroSkills()).length;
      for (int i = 0; i < j; i++)
      {
        L2Skill skill = al2skill[i];
        client.addSkill(skill, false);
      }
      client.sendPacket(new SkillList());
    }
    client.broadcastUserInfo();
  }

  public void engineInit()
  {
    load();
    _log.info("Constant heroes: loaded " + heroes.size() + " heroes.");
  }

  private void load()
  {
    String line = null;
    LineNumberReader lnr = null;
    heroes = new FastMap();
    try
    {
      lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("config/heroes.ini"))));
      while ((line = lnr.readLine()) != null) {
        if ((line.trim().length() == 0) || (line.startsWith("#")))
          continue;
        line = line.replaceAll(" ", "");
        if (!line.startsWith("HERO"))
          continue;
        String[] t = line.substring(5, line.length() - 1).split(",");
        int object = Integer.parseInt(t[0]);
        long time = Long.parseLong(t[1]);
        heroes.put(Integer.valueOf(object), Long.valueOf(time));
      }

    }
    catch (Exception e)
    {
      _log.warning("Heroes.load() " + lnr.getLineNumber());
      e.printStackTrace();
    }
  }

  public void saveHeroes()
  {
    String pattern = "";
    for (Iterator iterator = heroes.keySet().iterator(); iterator.hasNext(); )
    {
      int id = ((Integer)iterator.next()).intValue();
      long time2 = ((Long)heroes.get(Integer.valueOf(id))).longValue();
      if (!timeIsExpired(id)) {
        pattern = pattern + "HERO[" + id + "," + time2 + "]\n";
      }
    }
    File file = new File("config/heroes.ini");
    if (file.exists())
      file.delete();
    try
    {
      file.createNewFile();
      FileWriter fw = new FileWriter(file);
      fw.write("# Eon Interlude.\n");
      fw.write("# HERO[(int)objectId,(long)expire]\n");
      fw.write("# ===============================\n\n");
      fw.write(pattern);
      fw.flush();
      fw.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}