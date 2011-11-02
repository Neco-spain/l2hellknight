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
package com.l2js.gameserver.model.restriction;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;

import com.l2js.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author L0ngh0rn
 *
 */
public final class GlobalRestrictions
{
	public GlobalRestrictions()
	{
	}
	
	private static enum RestrictionMode implements Comparator<GlobalRestriction>
	{
		playerLoggedIn,
		playerDisconnected, // playerDisconnected
		fakePvPZone;
		
		private final Method _method;
		
		private RestrictionMode()
		{
			for (Method method : GlobalRestriction.class.getMethods())
			{
				if (name().equals(method.getName()))
				{
					_method = method;
					return;
				}
			}
			throw new InternalError();
		}
		
		private boolean equalsMethod(Method method)
		{
			if (!_method.getName().equals(method.getName()))
				return false;
			if (!_method.getReturnType().equals(method.getReturnType()))
				return false;
			return Arrays.equals(_method.getParameterTypes(), method.getParameterTypes());
		}

		private static final RestrictionMode[] VALUES = RestrictionMode.values();

		private static RestrictionMode parse(Method method)
		{
			for (RestrictionMode mode : VALUES)
				if (mode.equalsMethod(method))
					return mode;
			return null;
		}

		@Override
		public int compare(GlobalRestriction o1, GlobalRestriction o2)
		{
			return Double.compare(getPriority(o2), getPriority(o1));
		}

		private double getPriority(GlobalRestriction restriction)
		{
			RestrictionPriority a1 = getMatchingMethod(restriction.getClass()).getAnnotation(RestrictionPriority.class);
			if (a1 != null)
				return a1.value();
			RestrictionPriority a2 = restriction.getClass().getAnnotation(RestrictionPriority.class);
			if (a2 != null)
				return a2.value();
			return RestrictionPriority.DEFAULT_PRIORITY;
		}

		private Method getMatchingMethod(Class<? extends GlobalRestriction> clazz)
		{
			for (Method method : clazz.getMethods())
				if (equalsMethod(method))
					return method;
			throw new InternalError();
		}
	}
	
	private static final GlobalRestriction[][] _restrictions = new GlobalRestriction[RestrictionMode.VALUES.length][0];

	public synchronized static void activate(GlobalRestriction restriction)
	{
		for (Method method : restriction.getClass().getMethods())
		{
			RestrictionMode mode = RestrictionMode.parse(method);
			if (mode == null)
				continue;
			if (method.getAnnotation(DisabledRestriction.class) != null)
				continue;
			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];
			if (!ArrayUtils.contains(restrictions, restriction))
				restrictions = (GlobalRestriction[]) ArrayUtils.add(restrictions, restriction);
			Arrays.sort(restrictions, mode);
			_restrictions[mode.ordinal()] = restrictions;
		}
	}

	public synchronized static void deactivate(GlobalRestriction restriction)
	{
		for (RestrictionMode mode : RestrictionMode.VALUES)
		{
			GlobalRestriction[] restrictions = _restrictions[mode.ordinal()];
			for (int index; (index = ArrayUtils.indexOf(restrictions, restriction)) != -1;)
				restrictions = (GlobalRestriction[]) ArrayUtils.remove(restrictions, index);
			_restrictions[mode.ordinal()] = restrictions;
		}
	}

	static
	{
		activate(new PCRestriction());
	}

	public static void playerLoggedIn(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerLoggedIn.ordinal()])
			restriction.playerLoggedIn(activeChar);	
	}
	
	public static void playerDisconnected(L2PcInstance activeChar)
	{
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.playerDisconnected.ordinal()])
			restriction.playerDisconnected(activeChar);
	}
	
	public static boolean fakePvPZone(L2PcInstance activeChar, L2PcInstance target)
	{
		if ((activeChar == null) || (target == null))
			return false;
		for (GlobalRestriction restriction : _restrictions[RestrictionMode.fakePvPZone.ordinal()])
			if (restriction.fakePvPZone(activeChar, target))
				return true;
		return false;
	}
	
}
