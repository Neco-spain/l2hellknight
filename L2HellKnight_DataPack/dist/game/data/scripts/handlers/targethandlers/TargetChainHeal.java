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
package handlers.targethandlers;
 
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javolution.util.FastList;
 
import l2.hellknight.gameserver.GeoData;
import l2.hellknight.gameserver.handler.ITargetTypeHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.templates.skills.L2TargetType;
 
public class TargetChainHeal implements ITargetTypeHandler
{      
        public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
        {
                List<L2Character> targetList = new FastList<L2Character>();
                if (activeChar instanceof L2PcInstance)
                {
                        L2Character primarytarget = null;
                        if (activeChar.getTarget() instanceof L2Character)
                        {
                                primarytarget = (L2Character) activeChar.getTarget();
                                if (primarytarget == activeChar)
                                        targetList.add(activeChar);
                                else if (GeoData.getInstance().canSeeTarget(activeChar, primarytarget) && L2Skill.checkForAreaFriendlySkills((L2PcInstance) activeChar, primarytarget, skill))
                                        targetList.add(primarytarget);
                        }
                       
                        if (targetList.isEmpty())
                        {
                                activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                               
                                return _emptyTargetList;
                        }
                       
                        for (L2Character o : primarytarget.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
                        {
                                if (o == activeChar)
                                        continue;
                               
                                if (!GeoData.getInstance().canSeeTarget(primarytarget, o))
                                        continue;
                               
                                if (L2Skill.checkForAreaFriendlySkills((L2PcInstance) activeChar, o, skill))
                                        targetList.add(o);
                        }
                       
                        if (targetList.size() <= 11)
                                return targetList.toArray(new L2Character[targetList.size()]);
                        else
                        {
                                Collections.sort(targetList, new Comparator<L2Character>()
                                {
                                        public int compare(L2Character o1, L2Character o2)
                                        {
                                                double percentlost = o1.getCurrentHp() / o1.getMaxHp();
                                                double percentlost2 = o2.getCurrentHp() / o2.getMaxHp();
                                                return Double.compare(percentlost, percentlost2);
                                        }
                                });
                                if (targetList.size() > 11)
                                        ;
                                {
                                        targetList = targetList.subList(0, 11);
                                        if (!targetList.contains(primarytarget))
                                        {
                                                targetList.set(10, targetList.get(0));
                                                targetList.set(0, primarytarget);
                                        }
                                        else if (targetList.get(0) != primarytarget)
                                        {
                                                int pos = 1;
                                                for (;; pos++)
                                                {
                                                        if (targetList.get(pos) == primarytarget)
                                                                break;
                                                       
                                                        if (pos >= 10)
                                                                break;
                                                }
                                                targetList.set(pos, targetList.get(0));
                                                targetList.set(0, primarytarget);
                                        }
                                }
                                return targetList.toArray(new L2Character[targetList.size()]);
                        }
                }
                else
                        return _emptyTargetList;
        }
       
        @Override
        public Enum<L2TargetType> getTargetType()
        {
                return L2TargetType.TARGET_CHAIN_HEAL;
        }
}