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
package com.l2js.config.main;

import com.l2js.Config;
import com.l2js.util.L2Properties;

/**
 * @author L0ngh0rn
 */
public class ConfigGrandBoss extends Config
{
	private final static String path = GRAND_BOSS_CONFIG;
	
	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			Antharas_Wait_Time = getInt(properties, "AntharasWaitTime", 30);
			if (Antharas_Wait_Time < 3 || Antharas_Wait_Time > 60)
				Antharas_Wait_Time = 30;
			Antharas_Wait_Time = Antharas_Wait_Time * 60000;

			Valakas_Wait_Time = getInt(properties, "ValakasWaitTime", 30);
			if (Valakas_Wait_Time < 3 || Valakas_Wait_Time > 60)
				Valakas_Wait_Time = 30;
			Valakas_Wait_Time = Valakas_Wait_Time * 60000;

			Interval_Of_Antharas_Spawn = getInt(properties, "IntervalOfAntharasSpawn", 264);
			if (Interval_Of_Antharas_Spawn < 1 || Interval_Of_Antharas_Spawn > 480)
				Interval_Of_Antharas_Spawn = 192;
			Interval_Of_Antharas_Spawn = Interval_Of_Antharas_Spawn * 3600000;

			Random_Of_Antharas_Spawn = getInt(properties, "RandomOfAntharasSpawn", 72);
			if (Random_Of_Antharas_Spawn < 1 || Random_Of_Antharas_Spawn > 192)
				Random_Of_Antharas_Spawn = 145;
			Random_Of_Antharas_Spawn = Random_Of_Antharas_Spawn * 3600000;

			Interval_Of_Valakas_Spawn = getInt(properties, "IntervalOfValakasSpawn", 264);
			if (Interval_Of_Valakas_Spawn < 1 || Interval_Of_Valakas_Spawn > 480)
				Interval_Of_Valakas_Spawn = 192;
			Interval_Of_Valakas_Spawn = Interval_Of_Valakas_Spawn * 3600000;

			Random_Of_Valakas_Spawn = getInt(properties, "RandomOfValakasSpawn", 72);
			if (Random_Of_Valakas_Spawn < 1 || Random_Of_Valakas_Spawn > 192)
				Random_Of_Valakas_Spawn = 145;
			Random_Of_Valakas_Spawn = Random_Of_Valakas_Spawn * 3600000;

			Interval_Of_Baium_Spawn = getInt(properties, "IntervalOfBaiumSpawn", 168);
			if (Interval_Of_Baium_Spawn < 1 || Interval_Of_Baium_Spawn > 480)
				Interval_Of_Baium_Spawn = 121;
			Interval_Of_Baium_Spawn = Interval_Of_Baium_Spawn * 3600000;

			Random_Of_Baium_Spawn = getInt(properties, "RandomOfBaiumSpawn", 48);
			if (Random_Of_Baium_Spawn < 1 || Random_Of_Baium_Spawn > 192)
				Random_Of_Baium_Spawn = 8;
			Random_Of_Baium_Spawn = Random_Of_Baium_Spawn * 3600000;

			Interval_Of_Core_Spawn = getInt(properties, "IntervalOfCoreSpawn", 60);
			if (Interval_Of_Core_Spawn < 1 || Interval_Of_Core_Spawn > 480)
				Interval_Of_Core_Spawn = 27;
			Interval_Of_Core_Spawn = Interval_Of_Core_Spawn * 3600000;

			Random_Of_Core_Spawn = getInt(properties, "RandomOfCoreSpawn", 24);
			if (Random_Of_Core_Spawn < 1 || Random_Of_Core_Spawn > 192)
				Random_Of_Core_Spawn = 47;
			Random_Of_Core_Spawn = Random_Of_Core_Spawn * 3600000;

			Interval_Of_Orfen_Spawn = getInt(properties, "IntervalOfOrfenSpawn", 48);
			if (Interval_Of_Orfen_Spawn < 1 || Interval_Of_Orfen_Spawn > 480)
				Interval_Of_Orfen_Spawn = 28;
			Interval_Of_Orfen_Spawn = Interval_Of_Orfen_Spawn * 3600000;

			Random_Of_Orfen_Spawn = getInt(properties, "RandomOfOrfenSpawn", 20);
			if (Random_Of_Orfen_Spawn < 1 || Random_Of_Orfen_Spawn > 192)
				Random_Of_Orfen_Spawn = 41;
			Random_Of_Orfen_Spawn = Random_Of_Orfen_Spawn * 3600000;

			Interval_Of_QueenAnt_Spawn = getInt(properties, "IntervalOfQueenAntSpawn", 36);
			if (Interval_Of_QueenAnt_Spawn < 1 || Interval_Of_QueenAnt_Spawn > 480)
				Interval_Of_QueenAnt_Spawn = 19;
			Interval_Of_QueenAnt_Spawn = Interval_Of_QueenAnt_Spawn * 3600000;

			Random_Of_QueenAnt_Spawn = getInt(properties, "RandomOfQueenAntSpawn", 17);
			if (Random_Of_QueenAnt_Spawn < 1 || Random_Of_QueenAnt_Spawn > 192)
				Random_Of_QueenAnt_Spawn = 35;
			Random_Of_QueenAnt_Spawn = Random_Of_QueenAnt_Spawn * 3600000;

			Interval_Of_Zaken_Spawn = getInt(properties, "IntervalOfZakenSpawn", 19);
			if (Interval_Of_Zaken_Spawn < 1 || Interval_Of_Zaken_Spawn > 480)
				Interval_Of_Zaken_Spawn = 19;
			Interval_Of_Zaken_Spawn = Interval_Of_Zaken_Spawn * 3600000;

			Random_Of_Zaken_Spawn = getInt(properties, "RandomOfZakenSpawn", 35);
			if (Random_Of_Zaken_Spawn < 1 || Random_Of_Zaken_Spawn > 192)
				Random_Of_Zaken_Spawn = 35;
			Random_Of_Zaken_Spawn = Random_Of_Zaken_Spawn * 3600000;

			Interval_Of_Frintezza_Spawn = getInt(properties, "IntervalOfFrintezzaSpawn", 48);
			if (Interval_Of_Frintezza_Spawn < 1 || Interval_Of_Frintezza_Spawn > 480)
				Interval_Of_Frintezza_Spawn = 121;
			Interval_Of_Frintezza_Spawn = Interval_Of_Frintezza_Spawn * 3600000;

			Random_Of_Frintezza_Spawn = getInt(properties, "RandomOfFrintezzaSpawn", 8);
			if (Random_Of_Frintezza_Spawn < 1 || Random_Of_Frintezza_Spawn > 192)
				Random_Of_Frintezza_Spawn = 8;
			Random_Of_Frintezza_Spawn = Random_Of_Frintezza_Spawn * 3600000;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}
