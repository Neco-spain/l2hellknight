package l2p.gameserver.utils;

import java.util.Collection;
import java.util.Map;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.SubClass;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.ClassType2;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.components.CustomMessage;

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

    Collection skillLearnList = SkillAcquireHolder.getInstance().getAvailableSkills(null, AcquireType.CERTIFICATION);
    for (SkillLearn learn : skillLearnList)
    {
      Skill skill = player.getKnownSkill(learn.getId());
      if (skill != null) {
        player.removeSkill(skill, true);
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
}