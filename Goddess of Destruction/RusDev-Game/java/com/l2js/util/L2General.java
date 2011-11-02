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
package com.l2js.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author L0ngh0rn
 *
 */
public class L2General
{
	protected static final Logger _log = Logger.getLogger(L2General.class.getName());
	
	public static String[] getMemoryInfo()
	{
		double max = Runtime.getRuntime().maxMemory() / 1024; // maxMemory is the upper limit the jvm can use
		double allocated = Runtime.getRuntime().totalMemory() / 1024; // totalMemory the size of the current allocation pool
		double nonAllocated = max - allocated; // non allocated memory till jvm limit
		double cached = Runtime.getRuntime().freeMemory() / 1024; // freeMemory the unused memory in the allocation pool
		double used = allocated - cached; // really used memory
		double useable = max - used; // allocated, but non-used and non-allocated memory
		DecimalFormat df = new DecimalFormat(" (0.0000'%')");
		DecimalFormat df2 = new DecimalFormat(" # 'KB'");
		return new String[] {
				"+----",
				"| Global Memory Informations at " + getRealTime().toString() + ":",
				"|    |",
				"| Allowed Memory:" + df2.format(max),
				"|    |= Allocated Memory:" + df2.format(allocated) + df.format(allocated / max * 100), 
				"|    |= Non-Allocated Memory:" + df2.format(nonAllocated) + df.format(nonAllocated / max * 100), 
				"| Allocated Memory:" + df2.format(allocated),
				"|    |= Used Memory:" + df2.format(used) + df.format(used / max * 100),
				"|    |= Unused (cached) Memory:" + df2.format(cached) + df.format(cached / max * 100), 
				"| Useable Memory:" + df2.format(useable) + df.format(useable / max * 100),
				"+----" 
		};
	}

	public static String getRealTime()
	{
		SimpleDateFormat String = new SimpleDateFormat("H:mm:ss");
		return String.format(new Date());
	}

	public static void printMemoryInfo()
	{
		Tools.printSection("Memory");
		for (String line : getMemoryInfo())
			_log.info(line);
	}

	public static void printCpuInfo()
	{
		Tools.printSection("CPU");
		_log.info("Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
		_log.info("Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
	}

	public static void printOSInfo()
	{
		Tools.printSection("OS");
		_log.info("OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"));
		_log.info("OS Arch: " + System.getProperty("os.arch"));
	}
}
