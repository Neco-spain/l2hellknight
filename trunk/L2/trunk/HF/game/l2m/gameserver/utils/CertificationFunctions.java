package l2m.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.xml.holder.SkillAcquireHolder;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.SkillLearn;
import l2m.gameserver.model.SubClass;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.ClassType2;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.SkillList;
import l2m.gameserver.network.serverpackets.components.CustomMessage;

public class CertificationFunctions
{
  public static final String PATH = "villagemaster/certification/";

  public static void showCertificationList(NpcInstance npc, Player player)
  {
    if (!checkConditions(65, npc, player, true))
    {
      return;
    }

    Functions.show("villagemaster/certification/certificatelist.htm", player, npc, new Object[0]);
  }

  public static void getCertification65(NpcInstance npc, Player player)
  {
    if (!checkConditions(65, npc, player, false))
    {
      return;
    }

    SubClass clzz = player.getActiveClass();
    if (clzz.isCertificationGet(1))
    {
      Functions.show("villagemaster/certification/certificate-already.htm", player, npc, new Object[0]);
      return;
    }

    Functions.addItem(player, 10280, 1L);
    clzz.addCertification(1);
    player.store(true);
  }

  public static void getCertification70(NpcInstance npc, Player player)
  {
    if (!checkConditions(70, npc, player, false))
    {
      return;
    }

    SubClass clzz = player.getActiveClass();

    if (!clzz.isCertificationGet(1))
    {
      Functions.show("villagemaster/certification/certificate-fail.htm", player, npc, new Object[0]);
      return;
    }

    if (clzz.isCertificationGet(2))
    {
      Functions.show("villagemaster/certification/certificate-already.htm", player, npc, new Object[0]);
      return;
    }

    Functions.addItem(player, 10280, 1L);
    clzz.addCertification(2);
    player.store(true);
  }

  public static void getCertification75List(NpcInstance npc, Player player)
  {
    if (!checkConditions(75, npc, player, false))
    {
      return;
    }

    SubClass clzz = player.getActiveClass();

    if ((!clzz.isCertificationGet(1)) || (!clzz.isCertificationGet(2)))
    {
      Functions.show("villagemaster/certification/certificate-fail.htm", player, npc, new Object[0]);
      return;
    }

    if (clzz.isCertificationGet(4))
    {
      Functions.show("villagemaster/certification/certificate-already.htm", player, npc, new Object[0]);
      return;
    }

    Functions.show("villagemaster/certification/certificate-choose.htm", player, npc, new Object[0]);
  }

  public static void getCertification75(NpcInstance npc, Player player, boolean classCertifi)
  {
    if (!checkConditions(75, npc, player, false))
    {
      return;
    }

    SubClass clzz = player.getActiveClass();

    if ((!clzz.isCertificationGet(1)) || (!clzz.isCertificationGet(2)))
    {
      Functions.show("villagemaster/certification/certificate-fail.htm", player, npc, new Object[0]);
      return;
    }

    if (clzz.isCertificationGet(4))
    {
      Functions.show("villagemaster/certification/certificate-already.htm", player, npc, new Object[0]);
      return;
    }

    if (classCertifi)
    {
      ClassId cl = ClassId.VALUES[clzz.getClassId()];
      if (cl.getType2() == null) {
        return;
      }

      Functions.addItem(player, cl.getType2().getCertificateId(), 1L);
    }
    else
    {
      Functions.addItem(player, 10612, 1L);
    }

    clzz.addCertification(4);
    player.store(true);
  }

  public static void getCertification80(NpcInstance npc, Player player)
  {
    if (!checkConditions(80, npc, player, false))
    {
      return;
    }

    SubClass clzz = player.getActiveClass();

    if ((!clzz.isCertificationGet(1)) || (!clzz.isCertificationGet(2)) || (!clzz.isCertificationGet(4)))
    {
      Functions.show("villagemaster/certification/certificate-fail.htm", player, npc, new Object[0]);
      return;
    }

    if (clzz.isCertificationGet(8))
    {
      Functions.show("villagemaster/certification/certificate-already.htm", player, npc, new Object[0]);
      return;
    }

    ClassId cl = ClassId.VALUES[clzz.getClassId()];
    if (cl.getType2() == null) {
      return;
    }
    Functions.addItem(player, cl.getType2().getTransformationId(), 1L);
    clzz.addCertification(8);
    player.store(true);
  }

  public static void cancelCertification(NpcInstance npc, Player player)
  {
    if (player.getInventory().getAdena() < 10000000L)
    {
      player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
      return;
    }

    if (!player.getActiveClass().isBase()) {
      return;
    }
    player.getInventory().reduceAdena(10000000L);

    for (ClassType2 classType2 : ClassType2.VALUES)
    {
      player.getInventory().destroyItemByItemId(classType2.getCertificateId(), player.getInventory().getCountOf(classType2.getCertificateId()));
      player.getInventory().destroyItemByItemId(classType2.getTransformationId(), player.getInventory().getCountOf(classType2.getTransformationId()));
    }

    for (SkillLearn learn : SkillAcquireHolder.getInstance().getAllCertificationLearns())
    {
      if (player.removeSkill(learn.getId(), true) == null) {
        continue;
      }
    }
    for (SubClass subClass : player.getSubClasses().values())
    {
      if (!subClass.isBase()) {
        subClass.setCertification(0);
      }
    }
    player.sendPacket(new SkillList(player));
    Functions.show(new CustomMessage("scripts.services.SubclassSkills.SkillsDeleted", player, new Object[0]), player);
  }

  public static boolean checkConditions(int level, NpcInstance npc, Player player, boolean first)
  {
    if (player.getLevel() < level)
    {
      Functions.show("villagemaster/certification/certificate-nolevel.htm", player, npc, new Object[] { "%level%", Integer.valueOf(level) });
      return false;
    }

    if (player.getActiveClass().isBase())
    {
      Functions.show("villagemaster/certification/certificate-nosub.htm", player, npc, new Object[0]);
      return false;
    }
    return first;
  }

  public static void removeAllSkill() {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      for (SkillLearn learn : SkillAcquireHolder.getInstance().getAllCertificationLearns())
      {
        statement = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=?");
        statement.setInt(1, learn.getId());
        statement.execute();
      }

    }
    catch (Exception e)
    {
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }
}