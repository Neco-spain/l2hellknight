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
package com.l2js.config.mods;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigCustom extends Config
{
	private final static String path = L2JS_CUSTOM_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			SERVER_NAME = getString(properties, "ServerName", "L2jS");
			ALLOW_VALID_ENCHANT = getBoolean(properties, "AllowValidEnchant", false);
			ALLOW_VALID_EQUIP_ITEM = getBoolean(properties, "AllowValidEquipItem", false);
			DESTROY_ENCHANT_ITEM = getBoolean(properties, "DestroyEnchantItem", false);
			PUNISH_PLAYER = getBoolean(properties, "PunishPlayer", false);
			PVP_ALLOW_REWARD = getBoolean(properties, "PvpAllowReward", false);
			PVP_REWARD = getStringArray(properties, "PvpReward", "57,500000;5575,500".split("\\;"), ";");
			ALLOW_PVP_COLOR_SYSTEM = getBoolean(properties, "AllowPvPColorSystem", false);
			ALLOW_PVP_COLOR_NAME = getBoolean(properties, "AllowPvPColorName", false);
			ALLOW_PVP_COLOR_TITLE = getBoolean(properties, "AllowPvPColorTitle", false);
			try
			{
				String _configLine = getString(properties, "SystemPvPColor",
						"50,FFFFFF,FFFF77;100,FFFFFF,FFFF77;150,FFFFFF,FFFF77;250,FFFFFF,FFFF77;500,FFFFFF,FFFF77");
				if (ALLOW_PVP_COLOR_SYSTEM)
					SYSTEM_PVP_COLOR = new SystemPvPColor(_configLine);
			}
			catch (Exception e)
			{
				_log.warning("SystemPvPColor[loadCustomConfig()]: invalid config property -> SYSTEM_PVP_COLOR");
			}
			AUGMENTATION_WEAPONS_PVP = getBoolean(properties, "AugmentationWeaponsPvP", false);
			ELEMENTAL_ITEM_PVP = getBoolean(properties, "ElementalItemPvP", false);
			ELEMENTAL_CUSTOM_LEVEL_ENABLE = getBoolean(properties, "ElementalCustomLevelEnable", false);
			ELEMENTAL_LEVEL_WEAPON = getInt(properties, "ElementalLevelWeapon", 14);
			ELEMENTAL_LEVEL_WEAPON = (ELEMENTAL_LEVEL_WEAPON < 1 ? 1 : ELEMENTAL_LEVEL_WEAPON);
			ELEMENTAL_LEVEL_WEAPON = (ELEMENTAL_LEVEL_WEAPON > 14 ? 14 : ELEMENTAL_LEVEL_WEAPON);
			ELEMENTAL_LEVEL_ARMOR = getInt(properties, "ElementalLevelArmor", 14);
			ELEMENTAL_LEVEL_ARMOR = (ELEMENTAL_LEVEL_ARMOR < 1 ? 1 : ELEMENTAL_LEVEL_ARMOR);
			ELEMENTAL_LEVEL_ARMOR = (ELEMENTAL_LEVEL_ARMOR > 14 ? 14 : ELEMENTAL_LEVEL_ARMOR);
			ENTER_HELLBOUND_WITHOUT_QUEST = getBoolean(properties, "EnterHellBoundWithoutQuest", false);
			CUSTOM_SPAWNLIST_TABLE = getBoolean(properties, "CustomSpawnlistTable", true);
			SAVE_GMSPAWN_ON_CUSTOM = getBoolean(properties, "SaveGmSpawnOnCustom", true);
			CUSTOM_NPC_TABLE = getBoolean(properties, "CustomNpcTable", true);
			CUSTOM_NPC_SKILLS_TABLE = getBoolean(properties, "CustomNpcSkillsTable", true);
			CUSTOM_ARMORSETS_TABLE = getBoolean(properties, "CustomArmorSetsTable", true);
			CUSTOM_TELEPORT_TABLE = getBoolean(properties, "CustomTeleportTable", true);
			CUSTOM_DROPLIST_TABLE = getBoolean(properties, "CustomDroplistTable", true);
			CUSTOM_MERCHANT_TABLES = getBoolean(properties, "CustomMerchantTables", true);
			CUSTOM_NPCBUFFER_TABLES = getBoolean(properties, "CustomNpcBufferTables", true);
			CUSTOM_SKILLS_LOAD = getBoolean(properties, "CustomSkillsLoad", true);
			CUSTOM_ITEMS_LOAD = getBoolean(properties, "CustomItemsLoad", true);
			CUSTOM_MULTISELL_LOAD = getBoolean(properties, "CustomMultisellLoad", true);
			SIZE_MESSAGE_HTML_NPC = getInt(properties, "SizeHTMLMessageNPC", 20480);
			SIZE_MESSAGE_HTML_QUEST = getInt(properties, "SizeHTMLMessageQuest", 20480);
			SIZE_MESSAGE_SHOW_BOARD = getInt(properties, "SizeHTMLShowBoard", 20480);
			ALLOW_MANA_POTIONS = getBoolean(properties, "AllowManaPotions", true);
			DISABLE_MANA_POTIONS_IN_PVP = getBoolean(properties, "DisableManaPotionsInPvp", false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
