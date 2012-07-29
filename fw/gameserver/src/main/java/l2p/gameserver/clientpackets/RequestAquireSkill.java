package l2p.gameserver.clientpackets;

import l2p.commons.lang.ArrayUtils;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.instances.VillageMasterInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;
import l2p.gameserver.serverpackets.ExAcquirableSkillListByClass;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.SkillTable;

public class RequestAquireSkill extends L2GameClientPacket {
    private AcquireType _type;
    private int _id, _level, _subUnit;

    @Override
    protected void readImpl() {
        _id = readD();
        _level = readD();
        _type = ArrayUtils.valid(AcquireType.VALUES, readD());
        if (_type == AcquireType.SUB_UNIT)
            _subUnit = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getClient().getActiveChar();
        if (player == null || player.getTransformation() != 0 || _type == null)
            return;

        NpcInstance trainer = player.getLastNpc();
        if ((trainer == null || player.getDistance(trainer.getX(), trainer.getY()) > Creature.INTERACTION_DISTANCE)
                && _type != AcquireType.NORMAL)
            return;

        Skill skill = SkillTable.getInstance().getInfo(_id, _level);
        if (skill == null)
            return;

        if (!SkillAcquireHolder.getInstance().isSkillPossible(player, skill, _type))
            return;

        SkillLearn skillLearn = SkillAcquireHolder.getInstance().getSkillLearn(player, _id, _level, _type);

        if (skillLearn == null)
            return;

        if (!checkSpellbook(player, skillLearn)) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
            return;
        }

        switch (_type) {
            case NORMAL:
                learnSimpleNextLevel(player, skillLearn, skill);
                break;
            case TRANSFORMATION:
                learnSimpleNextLevel(player, skillLearn, skill);
                if (trainer != null)
                    trainer.showTransformationSkillList(player, AcquireType.TRANSFORMATION);
                break;
            case COLLECTION:
                learnSimpleNextLevel(player, skillLearn, skill);
                if (trainer != null)
                    NpcInstance.showCollectionSkillList(player);
                break;
            case TRANSFER_CARDINAL:
            case TRANSFER_EVA_SAINTS:
            case TRANSFER_SHILLIEN_SAINTS:
                learnSimple(player, skillLearn, skill);
                if (trainer != null)
                    trainer.showTransferSkillList(player);
                break;
            case FISHING:
                learnSimpleNextLevel(player, skillLearn, skill);
                if (trainer != null)
                    NpcInstance.showFishingSkillList(player);
                break;
            case CLAN:
                learnClanSkill(player, skillLearn, trainer, skill);
                break;
            case SUB_UNIT:
                learnSubUnitSkill(player, skillLearn, trainer, skill, _subUnit);
                break;
            case CERTIFICATION:
                if (!player.getActiveClass().isBase()) {
                    player.sendPacket(SystemMsg.THIS_SKILL_CANNOT_BE_LEARNED_WHILE_IN_THE_SUBCLASS_STATE);
                    return;
                }
                learnSimpleNextLevel(player, skillLearn, skill);
                if (trainer != null)
                    trainer.showTransformationSkillList(player, AcquireType.CERTIFICATION);
                break;
        }
    }

    /**
     * Изучение следующего возможного уровня скилла
     */
    private void learnSimpleNextLevel(Player player, SkillLearn skillLearn, Skill skill) {
        final int skillLevel = player.getSkillLevel(skillLearn.getId(), 0);
        if (skillLevel != skillLearn.getLevel() - 1)
            return;

        learnSimple(player, skillLearn, skill);
    }

    private void learnSimple(Player player, SkillLearn skillLearn, Skill skill) {
        if (player.getSp() < skillLearn.getCost()) {
            player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL);
            return;
        }

        if (!skillLearn.getRequiredItems().isEmpty()) {
            for (int itemId : skillLearn.getRequiredItems().keySet()) {
                if(player.getInventory().getCountOf(itemId) < skillLearn.getRequiredItems().get(itemId)) {
                    player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
                    return;
                }
            }
            for(int itemId : skillLearn.getRequiredItems().keySet()) {
                player.consumeItem(itemId, skillLearn.getRequiredItems().get(itemId));
            }
        }


        if (_type == AcquireType.TRANSFER_CARDINAL || _type == AcquireType.TRANSFER_EVA_SAINTS || _type == AcquireType.TRANSFER_SHILLIEN_SAINTS)
        {
            int item_id = 0;
            switch (_type)
            {
                case TRANSFER_CARDINAL:
                    item_id = 15307;
                    break;
                case TRANSFER_EVA_SAINTS:
                    item_id = 15308;
                    break;
                case TRANSFER_SHILLIEN_SAINTS:
                    item_id = 15309;
                    break;
                default:
                    player.sendMessage("There is no skills for your class.");
                    return;
            }
            if(player.getInventory().getCountOf(item_id) < 1)
            {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
                return;
            }

            player.getInventory().destroyItemByItemId(item_id, 1);
            player.sendPacket(SystemMessage2.removeItems(item_id, 1));
        }


        if(!skillLearn.getRemovedSkillsForPlayer(player).isEmpty()) {
            for(Skill del_skill : skillLearn.getRemovedSkillsForPlayer(player)) {
                player.removeSkill(del_skill, true);
            }
        }
        player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));

        player.setSp(player.getSp() - skillLearn.getCost());
        player.addSkill(skill, true);
        player.sendUserInfo();
        player.updateStats();
        player.sendSkillList();

        RequestExEnchantSkill.updateSkillShortcuts(player, skill.getId(), skill.getLevel());
    }

    private static void learnClanSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, Skill skill) {
        if (!(trainer instanceof VillageMasterInstance))
            return;

        if (!player.isClanLeader()) {
            player.sendPacket(SystemMsg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return;
        }

        Clan clan = player.getClan();
        final int skillLevel = clan.getSkillLevel(skillLearn.getId(), 0);
        if (skillLevel != skillLearn.getLevel() - 1) // можно выучить только следующий уровень
            return;
        if (clan.getReputationScore() < skillLearn.getCost()) {
            player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
            return;
        }

        if (!skillLearn.getRequiredItems().isEmpty()) {
            for (int itemId : skillLearn.getRequiredItems().keySet()) {
                if(player.getInventory().getCountOf(itemId) < skillLearn.getRequiredItems().get(itemId)) {
                    player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
                    return;
                }
            }
            for(int itemId : skillLearn.getRequiredItems().keySet()) {
                player.consumeItem(itemId, skillLearn.getRequiredItems().get(itemId));
            }
        }

        clan.incReputation(-skillLearn.getCost(), false, "AquireSkill: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
        clan.addSkill(skill, true);
        clan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));

        NpcInstance.showClanSkillList(player);
    }

    private static void learnSubUnitSkill(Player player, SkillLearn skillLearn, NpcInstance trainer, Skill skill, int id) {
        Clan clan = player.getClan();
        if (clan == null)
            return;
        SubUnit sub = clan.getSubUnit(id);
        if (sub == null)
            return;

        if ((player.getClanPrivileges() & Clan.CP_CL_TROOPS_FAME) != Clan.CP_CL_TROOPS_FAME) {
            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }

        int lvl = sub.getSkillLevel(skillLearn.getId(), 0);
        if (lvl >= skillLearn.getLevel()) {
            player.sendPacket(SystemMsg.THIS_SQUAD_SKILL_HAS_ALREADY_BEEN_ACQUIRED);
            return;
        }

        if (lvl != (skillLearn.getLevel() - 1)) {
            player.sendPacket(SystemMsg.THE_PREVIOUS_LEVEL_SKILL_HAS_NOT_BEEN_LEARNED);
            return;
        }

        if (clan.getReputationScore() < skillLearn.getCost()) {
            player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
            return;
        }

        if (!skillLearn.getRequiredItems().isEmpty()) {
            for (int itemId : skillLearn.getRequiredItems().keySet()) {
                if(player.getInventory().getCountOf(itemId) < skillLearn.getRequiredItems().get(itemId)) {
                    player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL);
                    return;
                }
            }
            for(int itemId : skillLearn.getRequiredItems().keySet()) {
                player.consumeItem(itemId, skillLearn.getRequiredItems().get(itemId));
            }
        }

        clan.incReputation(-skillLearn.getCost(), false, "AquireSkill2: " + skillLearn.getId() + ", lvl " + skillLearn.getLevel());
        sub.addSkill(skill, true);
        player.sendPacket(new SystemMessage2(SystemMsg.THE_CLAN_SKILL_S1_HAS_BEEN_ADDED).addSkillName(skill));

        if (trainer != null)
            NpcInstance.showSubUnitSkillList(player);
    }

    private static boolean checkSpellbook(Player player, SkillLearn skillLearn) {
        if (Config.ALT_DISABLE_SPELLBOOKS)
            return true;

        if (!skillLearn.getRequiredItems().isEmpty()) {
            for (int itemId : skillLearn.getRequiredItems().keySet()) {
                if(player.getInventory().getCountOf(itemId) < skillLearn.getRequiredItems().get(itemId))
                    return false;
            }
        }
        return true;
    }
}