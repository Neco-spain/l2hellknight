package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.tables.SkillTreeTable;

import java.util.ArrayList;
import java.util.List;


/**
 * format   d (dddc)
 */
public class SkillList extends L2GameServerPacket {
    private List<Skill> _skills;
    private boolean canEnchant;
    private Player player;

    public SkillList(Player player) {
        _skills = new ArrayList<Skill>(player.getAllSkills());
        canEnchant = player.getTransformation() == 0;
        this.player = player;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x5f);
        writeD(_skills.size());

        for (Skill temp : _skills) {
            writeD(temp.isActive() || temp.isToggle() ? 0 : 1); // deprecated? клиентом игнорируется
            writeD(temp.getDisplayLevel());
            writeD(temp.getDisplayId());
            writeD(temp.getReuseGroupId());// Reuse Group
            writeC(player.isUnActiveSkill(temp.getId()) ? 0x01 : 0x00); // иконка скилла серая если не 0
            writeC(canEnchant ? SkillTreeTable.isEnchantable(temp) : 0); // для заточки: если 1 скилл можно точить
        }
        writeD(0x00); // Unknown (GOD: Harmony)
    }
}