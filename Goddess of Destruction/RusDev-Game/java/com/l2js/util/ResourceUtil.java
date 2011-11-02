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

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author L0ngh0rn
 */
public class ResourceUtil
{
	private static final Logger _log = Logger.getLogger(ResourceUtil.class.getName());

	private ResourceUtil()
	{
	}

	public static void closeChannel(final Channel channel)
	{
		if (channel != null)
		{
			try
			{
				channel.close();
			}
			catch (final IOException ex)
			{
				_log.log(Level.WARNING, "Failed to close channel", ex);
			}
		}
	}

	public static void closeInputStream(final InputStream inputStream)
	{
		if (inputStream != null)
		{
			try
			{
				inputStream.close();
			}
			catch (final IOException ex)
			{
				_log.log(Level.WARNING, "Failed to close input stream", ex);
			}
		}
	}

	public static void closeXMLStreamReader(final XMLStreamReader reader)
	{
		if (reader != null)
		{
			try
			{
				reader.close();
			}
			catch (final XMLStreamException ex)
			{
				_log.log(Level.WARNING, "Failed to close xml stream reader", ex);
			}
		}
	}

	public static void closeConnection(final Connection connection)
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (final SQLException ex)
			{
				_log.log(Level.WARNING, "Failed to close connection", ex);
			}
		}
	}

	public static void closeResultSet(final ResultSet resultSet)
	{
		if (resultSet != null)
		{
			try
			{
				resultSet.close();
			}
			catch (final SQLException ex)
			{
				_log.log(Level.WARNING, "Failed to close resultset", ex);
			}
		}
	}

	public static void closeStatement(final Statement statement)
	{
		if (statement != null)
		{
			try
			{
				statement.close();
			}
			catch (final SQLException ex)
			{
				_log.log(Level.WARNING, "Failed to close statement", ex);
			}
		}
	}
}
