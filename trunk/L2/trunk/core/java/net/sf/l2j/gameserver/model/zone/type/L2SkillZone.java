package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2SkillZone extends L2ZoneType
{
	private L2Skill _skill;
    public L2SkillZone(int id)
    {
        super(id);
    }

    public void setParameter(String name, String value)
    {
        if(name.equals("skillId"))
        {
            _skillId = Integer.parseInt(value);
            if (_skillLvl > 0)
                _skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
        }
        else if(name.equals("skillLvl"))
        {
            _skillLvl = Integer.parseInt(value);
            if (_skillId > 0)
                _skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
        }
        else
        if(name.equals("onSiege"))
            _onSiege = Boolean.parseBoolean(value);
        else
            super.setParameter(name, value);
    }

    protected void onEnter(L2Character character)
    {
    	{
            if ((character instanceof L2PcInstance))
            {
              ((L2PcInstance)character).enterDangerArea();
              if (_skillId == 4559)
                for (L2Effect e : character.getAllEffects()) {
                  if (e == null)
                    continue;
                  if ((e.getSkill().getId() >= 4551) && (e.getSkill().getId() <= 4554))
                    e.exit(); 
                }
            }
            _skill.getEffects(character, character);
          }
    }

    protected void onExit(L2Character character)
    {
        if((character instanceof L2PcInstance) || (character instanceof L2SummonInstance))
        {
            character.stopSkillEffects(_skillId);
            if(character instanceof L2PcInstance)
                ((L2PcInstance)character).exitDangerArea();
        }
    }

    protected void onDieInside(L2Character character)
    {
    }

    protected void onReviveInside(L2Character character)
    {
    }

    private int _skillId;
    private int _skillLvl;
    @SuppressWarnings("unused")
	private boolean _onSiege;
}
