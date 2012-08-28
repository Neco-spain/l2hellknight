package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Couple;

public class CoupleManager
{
  private static final Logger _log = Logger.getLogger(CoupleManager.class.getName());
  private static CoupleManager _instance;
  private FastList<Couple> _couples;

  public static final CoupleManager getInstance()
  {
    if (_instance == null)
    {
      _log.info("L2JMOD: Initializing CoupleManager");
      _instance = new CoupleManager();
      _instance.load();
    }
    return _instance;
  }

  public void reload()
  {
    getCouples().clear();
    load();
  }

  private final void load()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("Select id from mods_wedding order by id");
      ResultSet rs = statement.executeQuery();

      while (rs.next())
      {
        getCouples().add(new Couple(rs.getInt("id")));
      }

      statement.close();

      _log.info("Loaded: " + getCouples().size() + " couples(s)");
    }
    catch (Exception e)
    {
      _log.severe("Exception: CoupleManager.load(): " + e.getMessage());
    } finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public final Couple getCouple(int coupleId) {
    int index = getCoupleIndex(coupleId);
    if (index >= 0) return (Couple)getCouples().get(index);
    return null;
  }

  public void createCouple(L2PcInstance player1, L2PcInstance player2)
  {
    if ((player1 != null) && (player2 != null))
    {
      if ((player1.getPartnerId() == 0) && (player2.getPartnerId() == 0))
      {
        int _player1id = player1.getObjectId();
        int _player2id = player2.getObjectId();

        Couple _new = new Couple(player1, player2);
        getCouples().add(_new);
        player1.setPartnerId(_player2id);
        player2.setPartnerId(_player1id);
        player1.setCoupleId(_new.getId());
        player2.setCoupleId(_new.getId());
      }
    }
  }

  public void checkCouple(L2PcInstance activeChar)
  {
    if (activeChar.isMarried())
    {
      if (activeChar.getInventory().getItemByItemId(9140) == null)
      {
        activeChar.addItem("Couple", 9140, 1, activeChar, true);
        activeChar.getInventory().updateDatabase();
      }

      L2Skill skill = SkillTable.getInstance().getInfo(3260, 1);
      activeChar.addSkill(skill, true);
      skill = SkillTable.getInstance().getInfo(3261, 1);
      activeChar.addSkill(skill, true);
      skill = SkillTable.getInstance().getInfo(3262, 1);
      activeChar.addSkill(skill, true);
    }
    else
    {
      for (L2ItemInstance item : activeChar.getInventory().getItems())
      {
        if (item.getItemId() != 9140)
          continue;
        activeChar.destroyItem("Removing Couple", item, activeChar, true);
        activeChar.getInventory().updateDatabase();
      }

      L2Skill skill = SkillTable.getInstance().getInfo(3260, 1);
      activeChar.removeSkill(skill, true);
      skill = SkillTable.getInstance().getInfo(3261, 1);
      activeChar.removeSkill(skill, true);
      skill = SkillTable.getInstance().getInfo(3262, 1);
      activeChar.removeSkill(skill, true);
    }
  }

  public void deleteCouple(int coupleId)
  {
    int index = getCoupleIndex(coupleId);
    Couple couple = (Couple)getCouples().get(index);
    if (couple != null)
    {
      L2PcInstance player1 = (L2PcInstance)L2World.getInstance().findObject(couple.getPlayer1Id());
      L2PcInstance player2 = (L2PcInstance)L2World.getInstance().findObject(couple.getPlayer2Id());
      if (player1 != null)
      {
        player1.setPartnerId(0);
        player1.setMarried(false);
        player1.setCoupleId(0);
      }

      if (player2 != null)
      {
        player2.setPartnerId(0);
        player2.setMarried(false);
        player2.setCoupleId(0);
      }

      couple.divorce();
      getCouples().remove(index);
    }
  }

  public final int getCoupleIndex(int coupleId)
  {
    int i = 0;
    for (Couple temp : getCouples())
    {
      if ((temp != null) && (temp.getId() == coupleId)) return i;
      i++;
    }
    return -1;
  }

  public final FastList<Couple> getCouples()
  {
    if (_couples == null) _couples = new FastList();
    return _couples;
  }
}