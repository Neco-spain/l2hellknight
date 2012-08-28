package l2p.gameserver.clientpackets;

import l2p.commons.lang.ArrayUtils;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.SubClass;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.instances.VillageMasterInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.SkillTable;

public class RequestAquireSkill extends L2GameClientPacket
{
  private AcquireType _type;
  private int _id;
  private int _level;
  private int _subUnit;

  protected void readImpl()
  {
    _id = readD();
    _level = readD();
    _type = ((AcquireType)ArrayUtils.valid(AcquireType.VALUES, readD()));
    if (_type == AcquireType.SUB_UNIT)
      _subUnit = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getTransformation() != 0) || (_type == null)) {
      return;
    }
    NpcInstance trainer = player.getLastNpc();
    if (((trainer == null) || (player.getDistance(trainer.getX(), trainer.getY()) > 200.0D)) && (!player.isGM())) {
      return;
    }
    Skill skill = SkillTable.getInstance().getInfo(_id, _level);
    if (skill == null) {
      return;
    }
    if (!SkillAcquireHolder.getInstance().isSkillPossible(player, skill, _type)) {
      return;
    }
    SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, _id, _level, _type);

    if (skillLearn == null) {
      return;
    }
    if (!checkSpellbook(player, skillLearn))
    {
      player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
      return;
    }

    switch (1.$SwitchMap$l2p$gameserver$model$base$AcquireType[_type.ordinal()])
    {
    case 1:
      learnSimpleNextLevel(player, skillLearn, skill);
      if (trainer == null) break;
      trainer.showSkillList(player); break;
    case 2:
      learnSimpleNextLevel(player, skillLearn, skill);
      if (trainer == null) break;
      trainer.showTransformationSkillList(player, AcquireType.TRANSFORMATION); break;
    case 3:
      learnSimpleNextLevel(player, skillLearn, skill);
      if (trainer == null) break;
      NpcInstance.showCollectionSkillList(player); break;
    case 4:
    case 5:
    case 6:
      learnSimple(player, skillLearn, skill);
      if (trainer == null) break;
      trainer.showTransferSkillList(player); break;
    case 7:
      learnSimpleNextLevel(player, skillLearn, skill);
      if (trainer == null) break;
      NpcInstance.showFishingSkillList(player); break;
    case 8:
      learnClanSkill(player, skillLearn, trainer, skill);
      break;
    case 9:
      learnSubUnitSkill(player, skillLearn, trainer, skill, _subUnit);
      break;
    case 10:
      if (!player.getActiveClass().isBase())
      {
        player.sendPacket(SystemMsg.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE);
        return;
      }
      learnSimpleNextLevel(player, skillLearn, skill);
      if (trainer == null) break;
      trainer.showTransformationSkillList(player, AcquireType.CERTIFICATION);
    }
  }

  private static void learnSimpleNextLevel(Player player, SkillLearn skillLearn, Skill skill)
  {
    int skillLevel = player.getSkillLevel(Integer.valueOf(skillLearn.getId()), 0);
    if (skillLevel != skillLearn.getLevel() - 1) {
      return;
    }
    learnSimple(player, skillLearn, skill);
  }

  private static void learnSimple(Player player, SkillLearn skillLearn, Skill skill)
  {
    if (player.getSp() < skillLearn.getCost())
    {
      player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL);
      return;
    }

    if ((skillLearn.getItemId() > 0) && 
      (!player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount()))) {
      return;
    }
    player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));

    player.setSp(player.getSp() - skillLearn.getCost());
    player.addSkill(skill, true);
    player.sendUserInfo();
    player.updateStats();

    player.sendPacket(new SkillList(player));

    RequestExEnchantSkill.updateSkillShortcuts(player, skill.getId(), skill.getLevel());
  }

  private static void learnClanSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, Skill skill)
  {
    if (!(trainer instanceof VillageMasterInstance)) {
      return;
    }
    if (!player.isClanLeader())
    {
      player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
      return;
    }

    Clan clan = player.getClan();
    int skillLevel = clan.getSkillLevel(skillLearn.getId(), 0);
    if (skillLevel != skillLearn.getLevel() - 1)
      return;
    if (clan.getReputationScore() < skillLearn.getCost())
    {
      player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
      return;
    }

    if ((skillLearn.getItemId() > 0) && 
      (!player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount()))) {
      return;
    }
    clan.incReputation(-skillLearn.getCost(), false, "AquireSkill: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
    clan.addSkill(skill, true);
    clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill) });

    NpcInstance.showClanSkillList(player);
  }

  private static void learnSubUnitSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, Skill skill, int id)
  {
    Clan clan = player.getClan();
    if (clan == null)
      return;
    SubUnit sub = clan.getSubUnit(id);
    if (sub == null) {
      return;
    }
    if ((player.getClanPrivileges() & 0x200) != 512)
    {
      player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
      return;
    }

    int lvl = sub.getSkillLevel(skillLearn.getId(), 0);
    if (lvl >= skillLearn.getLevel())
    {
      player.sendPacket(SystemMsg.THIS_SQUAD_SKILL_HAS_ALREADY_BEEN_ACQUIRED);
      return;
    }

    if (lvl != skillLearn.getLevel() - 1)
    {
      player.sendPacket(SystemMsg.THE_PREVIOUS_LEVEL_SKILL_HAS_NOT_BEEN_LEARNED);
      return;
    }

    if (clan.getReputationScore() < skillLearn.getCost())
    {
      player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
      return;
    }

    if ((skillLearn.getItemId() > 0) && 
      (!player.consumeItem(skillLearn.getItemId(), skillLearn.getItemCount()))) {
      return;
    }
    clan.incReputation(-skillLearn.getCost(), false, "AquireSkill2: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
    sub.addSkill(skill, true);
    player.sendPacket(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));

    if (trainer != null)
      NpcInstance.showSubUnitSkillList(player);
  }

  private static boolean checkSpellbook(Player player, SkillLearn skillLearn)
  {
    if (Config.ALT_DISABLE_SPELLBOOKS) {
      return true;
    }
    if (skillLearn.getItemId() == 0) {
      return true;
    }

    if (skillLearn.isClicked()) {
      return false;
    }
    return player.getInventory().getCountOf(skillLearn.getItemId()) >= skillLearn.getItemCount();
  }
}