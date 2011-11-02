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
 * @since 09/06/2011
 */
public class ConfigEMail extends Config
{
	private final static String	path	= EMAIL_CONFIG_FILE;

	public static void loadConfig()
	{
		_log.info("Loading: " + path);
		try
		{
			L2Properties properties = new L2Properties(path);
			EMAIL_SERVERINFO_NAME = getString(properties, "ServerInfoName", "Unconfigured L2JS");
			EMAIL_SERVERINFO_ADDRESS = getString(properties, "ServerInfoAddress", "info@myl2js.com");

			EMAIL_SYS_ENABLED = getBoolean(properties, "EmailSystemEnabled", false);
			EMAIL_SYS_HOST = getString(properties, "SmtpServerHost", "smtp.gmail.com");
			EMAIL_SYS_PORT = getInt(properties, "SmtpServerPort", 465);
			EMAIL_SYS_SMTP_AUTH = getBoolean(properties, "SmtpAuthRequired", true);
			EMAIL_SYS_FACTORY = getString(properties, "SmtpFactory", "javax.net.ssl.SSLSocketFactory");
			EMAIL_SYS_FACTORY_CALLBACK = getBoolean(properties, "SmtpFactoryCallback", false);
			EMAIL_SYS_USERNAME = getString(properties, "SmtpUsername", "user@gmail.com");
			EMAIL_SYS_PASSWORD = getString(properties, "SmtpPassword", "password");
			EMAIL_SYS_ADDRESS = getString(properties, "EmailSystemAddress", "noreply@myl2js.com");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + path + " File.");
		}
	}
}