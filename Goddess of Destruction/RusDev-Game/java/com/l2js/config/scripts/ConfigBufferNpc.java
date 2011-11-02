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
package com.l2js.config.scripts;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigBufferNpc extends Config
{
	private final static String path = L2JS_BUFFER_NPC_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			BUFFER_NPC_ID = getInt(properties, "BufferNpcID", 70028);
			BUFFER_NPC_MIN_LEVEL = getInt(properties, "BufferNpcMinLevel", 40);
			BUFFER_NPC_ENABLE_READY = getBoolean(properties, "BufferNpcEnableReady", true);
			BUFFER_NPC_ENABLE_SCHEME = getBoolean(properties, "BufferNpcEnableScheme", true);
			BUFFER_NPC_NUMBER_SCHEME = getInt(properties, "BufferNpcNubmerScheme", 3);
			BUFFER_NPC_FEE_SCHEME = getIntArray(properties, "BufferNpcFeeScheme", new int[]
			{
					3470, 1
			}, ",");
			BUFFER_NPC_ENABLE_SELECT = getBoolean(properties, "BufferNpcEnableSelect", true);
			BUFFER_NPC_ENABLE_PET = getBoolean(properties, "BufferNpcEnablePet", true);
			BUFFER_NPC_ENABLE_RECOVER = getBoolean(properties, "BufferNpcEnableRecover", true);
			BUFFER_NPC_ENABLE_RECOVER_EVENT = getBoolean(properties, "BufferNpcEnableRecoverInEvent", false);
			BUFFER_NPC_FEE_RECOVER = getIntArray(properties, "BufferNpcFeeRecover", new int[]
			{
					57, 1000000
			}, ",");
			BUFFER_NPC_ENABLE_REMOVE = getBoolean(properties, "BufferNpcEnableRemove", true);
			BUFFER_NPC_FEE_REMOVE = getIntArray(properties, "BufferNpcFeeRemove", new int[]
			{
					57, 1000000
			}, ",");
			BUFFER_NPC_REMOVE_AMOUNT = getBoolean(properties, "BufferNpcRemoveAmount", false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
