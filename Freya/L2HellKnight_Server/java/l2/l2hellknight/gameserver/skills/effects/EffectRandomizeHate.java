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

package l2.hellknight.gameserver.skills.effects;

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;

import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.EffectTemplate;
import l2.hellknight.gameserver.templates.L2EffectType;
import l2.hellknight.util.Rnd;

public class EffectRandomizeHate extends L2Effect
{
        public EffectRandomizeHate(Env env, EffectTemplate template)
        {
                super(env, template);
        }
        
        /**
         * 
         * @see l2.hellknight.gameserver.model.L2Effect#getEffectType()
         */
        @Override
        public L2EffectType getEffectType()
        {
                return L2EffectType.RANDOMIZE_HATE;
        }
        
        /**
         * 
         * @see l2.hellknight.gameserver.model.L2Effect#onStart()
         */
        @Override
        public boolean onStart()
        {
                if (getEffected() == null || getEffected() == getEffector())
                        return false;
                
                // Effect is for mobs only.
                if (!(getEffected() instanceof L2Attackable))
                        return false;
                
                L2Attackable effectedMob = (L2Attackable) getEffected();
                
                List<L2Character> targetList = new FastList<L2Character>();
                
                // Getting the possible targets
                
                Collection<L2Character> chars = getEffected().getKnownList().getKnownCharacters();
                for (L2Character cha : chars)
                {
                        if (cha != null && (cha != effectedMob) && (cha != getEffector()))
                        {
                                // Aggro cannot be transfared to a mob of the same faction.
                                if (cha instanceof L2Attackable && ((L2Attackable) cha).getFactionId() != null && ((L2Attackable) cha).getFactionId().equals(effectedMob.getFactionId()))
                                        continue;
                                
                                targetList.add(cha);
                        }
                }
                // if there is no target, exit function
                if (targetList.isEmpty())
                        return true;
                
                // Choosing randomly a new target
                final L2Character target = targetList.get(Rnd.get(targetList.size()));
                
                final int hate = effectedMob.getHating(getEffector());
                effectedMob.stopHating(getEffector());
                effectedMob.addDamageHate(target, 0, hate);
                
                return true;
        }
        
        /**
         * 
         * @see l2.hellknight.gameserver.model.L2Effect#onActionTime()
         */
        @Override
        public boolean onActionTime()
        {
                return false;
        }
}