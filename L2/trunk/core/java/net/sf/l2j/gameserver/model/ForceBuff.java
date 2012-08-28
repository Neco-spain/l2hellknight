/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
package net.sf.l2j.gameserver.model;

import java.util.concurrent.Future;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.effects.EffectForce;
import net.sf.l2j.gameserver.util.Util;

/**
 * @author kombat, Forsaiken
 */
public final class ForceBuff
{
    final int _skillCastRange;
    final int _forceId;
    L2PcInstance _caster;
    L2PcInstance _target;
    Future<?> _geoCheckTask;
    
    public L2PcInstance getCaster()
    {
        return _caster;
    }
    
    public L2PcInstance getTarget()
    {
        return _target;
    }
    
    public ForceBuff(L2PcInstance caster, L2PcInstance target, L2Skill skill)
    {
        _skillCastRange = skill.getCastRange();
        _caster = caster;
        _target = target;
        _forceId = skill.getForceId();
        
        L2Effect effect = _target.getFirstEffect(_forceId);
        if (effect != null)
            ((EffectForce)effect).increaseForce();
        else
            SkillTable.getInstance().getInfo(_forceId, 1).getEffects(_caster, _target);
        
        _geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
    }
    
    public void delete()
    {
        _caster.setForceBuff(null);
        L2Effect effect = _target.getFirstEffect(_forceId);
        if (effect != null)
            ((EffectForce)effect).decreaseForce();
        
        _geoCheckTask.cancel(true);
    }
    
    class GeoCheckTask implements Runnable
    {
        public void run()
        {
            try
            {
                if (!Util.checkIfInRange(_skillCastRange, _caster, _target, true))
                    delete();
                
                if (!GeoData.getInstance().canSeeTarget(_caster, _target))
                    delete();
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }
}
