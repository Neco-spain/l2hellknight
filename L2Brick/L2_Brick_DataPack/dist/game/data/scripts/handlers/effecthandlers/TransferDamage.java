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
package handlers.effecthandlers;

import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.templates.effects.EffectTemplate;
import l2.brick.gameserver.templates.skills.L2EffectType;

/**
 *
 * @author UnAfraid
 */

public class TransferDamage extends L2Effect
{  
   public TransferDamage(Env env, EffectTemplate template)
   {
      super(env, template);
   }

   public TransferDamage(Env env, L2Effect effect)
   {
      super(env, effect);
   }

   /**
    *
    * @see l2.brick.gameserver.model.L2Effect#getEffectType()
    */
   @Override
   public L2EffectType getEffectType()
   {
      return L2EffectType.DAMAGE_TRANSFER;
   }

   /**
    *
    * @see l2.brick.gameserver.model.L2Effect#onStart()
    */
   @Override
   public boolean onStart()
   {
      if (getEffected() instanceof L2Playable && getEffector() instanceof L2PcInstance)
    	  ((L2Playable) getEffected()).setTransferDamageTo((L2PcInstance) getEffector());
      return true;
   }

   /**
    *
    * @see l2.brick.gameserver.model.L2Effect#onExit()
    */
   @Override
   public void onExit()
   {
      if (getEffected() instanceof L2Playable && getEffector() instanceof L2PcInstance)
         ((L2Playable) getEffected()).setTransferDamageTo(null);
   }

   /**
    *
    * @see l2.brick.gameserver.model.L2Effect#onActionTime()
    */
   @Override
   public boolean onActionTime()
   {
      return false;
   }
}
