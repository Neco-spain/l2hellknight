package l2rt.gameserver.network.serverpackets;

import javolution.util.FastList;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2SkillLearn;
import l2rt.gameserver.model.base.UsablePacketItem;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.util.GArray;

public class ExAcquirableSkillListByClass extends L2GameServerPacket
{
    private FastList<L2SkillLearn> skills;
	
	public ExAcquirableSkillListByClass(L2Player player)
    {
		skills = new FastList<L2SkillLearn>();

		GArray<L2SkillLearn> skill = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId()); 
		//Кривая затычка от повторов скилов
		for (L2SkillLearn s : skill)
		{
			boolean knownSkill = false;
			for (L2SkillLearn sk : skills)
			{
				if(s.getId() == sk.getId())
				{
					knownSkill = true;
				}
			}
			if(!knownSkill)
			{
				skills.add(s);
			}
			
			knownSkill = false;
		}
    }


    @Override
    protected final void writeImpl()
    {
        writeC(0xfe);
        writeH(0xf9);


        writeD(skills.size());
        for (L2SkillLearn sk : skills)
        {
			writeD(sk.getId());
            writeD(sk.getLevel());
            writeD(sk.getReuse());
            writeH(sk.getMinLevel());

            writeD(sk.getRequiredItems().size());
			for(UsablePacketItem item : sk.getRequiredItems()) {
                writeD(item.itemId());
                writeQ(item.count());
            }

			writeD(0);
           // writeD(sk.getRequiredSkills().size());
           /* writeD(sk.getPrequisiteSkills().size());            //TODO
            for(UsablePacketSkill skill : sk.getPrequisiteSkills()) {
                writeD(skill.id());
                writeD(skill.level());
            }      */

        } 
		FastList.recycle(skills);
	}

}
