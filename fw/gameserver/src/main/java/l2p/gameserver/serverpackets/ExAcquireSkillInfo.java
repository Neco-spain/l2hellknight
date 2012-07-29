package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.SkillLearn;

/**
 * @author : Ragnarok
 * @date : 26.01.12  18:03
 */
public class ExAcquireSkillInfo extends L2GameServerPacket {
    private Player player;
    private SkillLearn skillLearn;

    public ExAcquireSkillInfo(Player player, SkillLearn skillLearn) {
        this.player = player;
        this.skillLearn = skillLearn;
    }

    @Override
    protected void writeImpl() {
        writeEx(0xFB);

        writeD(skillLearn.getId());//Skill ID
        writeD(skillLearn.getLevel());//Skill Level
        writeD(skillLearn.getCost());// sp_cost
        writeH(skillLearn.getMinLevel());// Required Level

        writeD(skillLearn.getRequiredItems().size());
        for(int itemId : skillLearn.getRequiredItems().keySet()) {
            writeD(itemId);
            writeQ(skillLearn.getRequiredItems().get(itemId));
        }

        writeD(skillLearn.getRemovedSkillsForPlayer(player).size());// deletedSkillsSize
        for (Skill skill : skillLearn.getRemovedSkillsForPlayer(player)) {
            writeD(skill.getId());// skillId
            writeD(skill.getLevel());// skillLvl
        }
    }
}
