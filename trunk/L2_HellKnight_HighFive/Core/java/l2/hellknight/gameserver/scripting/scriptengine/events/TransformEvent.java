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
package l2.hellknight.gameserver.scripting.scriptengine.events;

import l2.hellknight.gameserver.model.L2Transformation;
import l2.hellknight.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class TransformEvent implements L2Event
{
	private L2Transformation transformation;
	private boolean transforming; // false = untransforming
	
	/**
	 * @return the transformation
	 */
	public L2Transformation getTransformation()
	{
		return transformation;
	}
	
	/**
	 * @param transformation the transformation to set
	 */
	public void setTransformation(L2Transformation transformation)
	{
		this.transformation = transformation;
	}
	
	/**
	 * @return the transforming
	 */
	public boolean isTransforming()
	{
		return transforming;
	}
	
	/**
	 * @param transforming the transforming to set
	 */
	public void setTransforming(boolean transforming)
	{
		this.transforming = transforming;
	}
}
