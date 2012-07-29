package l2p.gameserver.serverpackets;

import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.actor.instances.player.ShortCut;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.tables.SkillTable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author : Ragnarok
 * @date : 12.01.12  0:11
 */
public class ExAcquirableSkillListByClass extends L2GameServerPacket {
    private Player player;
    private Collection<SkillLearn> skills;

    public ExAcquirableSkillListByClass(Player player) {
        this.player = player;
        skills = new ArrayList<SkillLearn>();
        for (SkillLearn skill : SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL, true)) {
            if (skill.getCost() != 0 || !skill.getRequiredItems().isEmpty()) { // Скилы, дающиеся бесплатно не отображаются в списке
                skills.add(skill);
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeEx(0xF9);

        writeD(skills.size());
        for (SkillLearn skillLearn : skills) {
            writeD(skillLearn.getId());// skill id
            writeD(skillLearn.getLevel());// skill level
            writeD(skillLearn.getCost());// sp_cost
            writeH(skillLearn.getMinLevel());// Required Level

            writeD(skillLearn.getRequiredItems().size()); // reuiredItemsListSize
            for (int itemId : skillLearn.getRequiredItems().keySet()) {
                writeD(itemId);// itemId
                writeQ(skillLearn.getRequiredItems().get(itemId));// Count
            }

            writeD(skillLearn.getRemovedSkillsForPlayer(player).size());// deletedSkillsSize
            for (Skill skill : skillLearn.getRemovedSkillsForPlayer(player)) {
                writeD(skill.getId());// skillId
                writeD(skill.getLevel());// skillLvl
            }
        }
    }
}
