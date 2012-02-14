package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill.SkillType;

public class RequestDispel extends L2GameClientPacket
{
	private int id, level, charId;

	@Override
	protected void readImpl() throws Exception
	{
        charId = readD();
		id = readD();
		level = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

        if(activeChar.getObjectId() == charId) {
            for(L2Effect e : activeChar.getEffectList().getAllEffects()) {
                if(e.getDisplayId() == id && e.getDisplayLevel() == level) {
                    if(activeChar.isGM() || !e.isOffensive() && !e.getSkill().isMusic() && e.getSkill().getSkillType() != SkillType.TRANSFORMATION)
                        e.exit();
                    else
                        return;
                }
            }
        } else if (activeChar.getPet() != null && activeChar.getPet().getObjectId() == charId) {
            activeChar.getPet().getEffectList().stopEffectByDisplayId(id);
        }
	}
}