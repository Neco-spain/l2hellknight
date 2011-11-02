package com.l2js.gameserver.network.serverpackets;


import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.L2SkillLearn;
import com.l2js.gameserver.datatables.SkillTreesData;
import com.l2js.gameserver.model.base.UsablePacketItem;
import com.l2js.gameserver.model.base.UsablePacketSkill;
import javolution.util.FastList;



/**
 * Created by IntelliJ IDEA.
 * User: Keiichi, Bacek
 * Date: 24.05.2011
 * Time: 12:04:56
 * To change this template use File | Settings | File Templates.
 */
public class ExAcquirableSkillListByClass extends L2GameServerPacket
{
    private static final String _S__FE_EXACQUIRABLESKILLLISTBYCLASS = "[S] F9 ExAcquirableSkillListByClass";
    private FastList<L2SkillLearn> skills;

    
	public ExAcquirableSkillListByClass(L2PcInstance player)
    {
		if(skills == null)
		{
			skills = new FastList<L2SkillLearn>();
		}

        FastList<L2SkillLearn> skill = SkillTreesData.getInstance().getAvailableSkills(player, player.getClassId(),false,false); 
        for (L2SkillLearn s : skill)
        {
            skills.add(s);
        }
        
        this.skills = skills;

    }


    @Override
    protected final void writeImpl()
    {
        writeC(0xfe);
        writeH(0xf9);


        writeD(skills.size());
        for (L2SkillLearn sk : skills)
        {
			writeD(sk.getSkillId());
            writeD(sk.getSkillLevel());
            writeD(sk.getReuse());
            writeH(sk.getGetLevel());

            writeD(sk.getRequiredItems().size());
			for(UsablePacketItem item : sk.getRequiredItems()) {
                writeD(item.itemId());
                writeQ(item.count());
            }

            writeD(sk.getPrequisiteSkills().size());
            for(UsablePacketSkill skill : sk.getPrequisiteSkills()) {
                writeD(skill.id());
                writeD(skill.level());
            }

        } 
		FastList.recycle(skills);
	}
	
    @Override
    public String getType()
    {
        return _S__FE_EXACQUIRABLESKILLLISTBYCLASS;
    }

}
