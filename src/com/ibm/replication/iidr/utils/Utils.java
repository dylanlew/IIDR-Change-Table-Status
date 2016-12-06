package com.ibm.replication.iidr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utils {

	private static SimpleDateFormat logDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
	private static SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static Logger logger = LogManager.getLogger();

	public static String convertLogDateToIso(String inputDate) {

		String isoDate = null;
		try {
			Date date = logDateFormat.parse(inputDate);
			isoDate = isoDateFormat.format(date);
		} catch (ParseException e) {
			logger.error("Error while parsing date " + inputDate + ": " + e.getMessage());
			isoDate = "0000-01-01 00:00:00";
		}
		return isoDate;
	}

	/**
	 * Gets the schema name from the qualified name. If a qualified name is not
	 * specified, return an empty string as the schema name
	 * 
	 * @param qualifiedName
	 * @return The schema name
	 */
	public static String getSchema(String qualifiedName) {
		String schemaName = "";
		if (qualifiedName.contains("."))
			schemaName = qualifiedName.split("\\.")[0];
		return schemaName;
	}

	/**
	 * Gets the table name from the qualified name. If a qualified name is not
	 * specified, return an empty string as the table name
	 * 
	 * @param qualifiedName
	 * @return The table name
	 */
	public static String getTable(String qualifiedName) {
		String tableName = "";
		if (qualifiedName.contains("."))
			tableName = qualifiedName.split("\\.")[1];
		return tableName;
	}

}
