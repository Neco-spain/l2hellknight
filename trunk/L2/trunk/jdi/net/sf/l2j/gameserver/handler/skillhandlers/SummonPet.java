package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.stat.PetStat;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class SummonPet
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SUMMON_PET };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    L2NpcTemplate lastNpcTemplate = activeChar.getLastPetNT();
    L2ItemInstance lastItem = activeChar.getLastPetItem();

    activeChar.setLastPetNT(null);
    activeChar.setLastPetItem(null);

    L2PcInstance player = (L2PcInstance)activeChar;

    if ((lastNpcTemplate == null) || (lastItem == null))
    {
      player.sendMessage("Error...");
      return;
    }

    player.sendPacket(new MagicSkillLaunched(player, 2046, 1));
    L2PetInstance petSummon = L2PetInstance.spawnPet(lastNpcTemplate, player, lastItem);
    if (petSummon == null) {
      return;
    }
    try
    {
      petSummon.setTitle(player.getName());

      if (!petSummon.isRespawned())
      {
        petSummon.setCurrentHp(petSummon.getMaxHp());
        petSummon.setCurrentMp(petSummon.getMaxMp());
        petSummon.getStat().setExp(petSummon.getExpForThisLevel());
        petSummon.setCurrentFed(petSummon.getMaxFed());
      }

      petSummon.setRunning();

      if (!petSummon.isRespawned()) {
        petSummon.store();
      }
      player.setPet(petSummon);

      L2World.getInstance().storeObject(petSummon);

      petSummon.spawnMe(player.getX() + 50, player.getY() + 100, player.getZ());

      player.sendPacket(new PetInfo(petSummon));

      lastItem.setEnchantLevel(petSummon.getLevel());

      if (petSummon.getCurrentFed() <= 0)
        ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(player, petSummon), 60000L);
      else {
        petSummon.startFeed(false);
      }
      petSummon.setFollowStatus(true);
      petSummon.setShowSummonAnimation(false);
    }
    catch (Throwable e)
    {
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }

  static class PetSummonFeedWait
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private L2PetInstance _petSummon;

    PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
    {
      _activeChar = activeChar;
      _petSummon = petSummon;
    }

    public void run()
    {
      try
      {
        if (_petSummon.getCurrentFed() <= 0)
          _petSummon.unSummon(_activeChar);
        else
          _petSummon.startFeed(false);
      }
      catch (Throwable e)
      {
      }
    }
  }
}