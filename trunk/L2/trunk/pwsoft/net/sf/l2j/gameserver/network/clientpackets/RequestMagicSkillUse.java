package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
  private int _magicId;
  private boolean _ctrlPressed;
  private boolean _shiftPressed;

  protected void readImpl()
  {
    _magicId = readD();
    _ctrlPressed = (readD() != 0);
    _shiftPressed = (readC() != 0);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.getCPC() < 100L) {
      player.sendActionFailed();
      return;
    }

    player.setCPC();

    if (player.isOutOfControl()) {
      player.sendActionFailed();
      return;
    }

    if (player.isDead()) {
      player.sendActionFailed();
      return;
    }

    if (player.isFakeDeath()) {
      player.stopFakeDeath(null);
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(_magicId, player.getSkillLevel(_magicId));

    if (skill == null) {
      player.sendActionFailed();
      return;
    }

    if ((skill.isPassive()) || (skill.isChance())) {
      return;
    }

    if ((skill.isToggle()) && (player.getFirstEffect(_magicId) != null)) {
      player.stopSkillEffects(_magicId);
      return;
    }

    if ((player.isCursedWeaponEquiped()) && (skill.isNotForCursed())) {
      player.sendPacket(Static.NOT_FOR_CURSED);
      player.sendActionFailed();
      return;
    }

    L2Object target = player.getTarget();
    if (target != null) {
      if (target.isL2VillageMaster()) {
        player.sendActionFailed();
        return;
      }

      if (skill.getId() == 246) {
        if (!target.isL2Artefact()) {
          player.sendActionFailed();
          return;
        }

        if (!player.isInSiegeRuleArea()) {
          player.sendPacket(Static.WRONG_PLACE_CAST_RULE);
          player.sendActionFailed();
          return;
        }
      }
    }

    try
    {
      if ((skill.isBattleForceSkill()) || (skill.isSpellForceSkill())) {
        player.setGroundSkillLoc(null);
        if (skill.checkForceCondition(player, _magicId)) {
          player.useMagic(skill, _ctrlPressed, _shiftPressed);
        } else {
          player.sendPacket(Static.NOT_ENOUGH_FORCES);
          player.sendActionFailed();
          return;
        }
      } else if (skill.checkCondition(player, player, false)) {
        player.useMagic(skill, _ctrlPressed, _shiftPressed);
      } else {
        player.sendActionFailed();
      }
    } catch (Exception e) {
      player.sendActionFailed();
    }
  }
}