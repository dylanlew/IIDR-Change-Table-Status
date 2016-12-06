package com.ibm.replication.iidr.commands.chcclp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.ibm.replication.cdc.scripting.EmbeddedScript;
import com.ibm.replication.cdc.scripting.EmbeddedScriptException;
import com.ibm.replication.cdc.scripting.ResultStringKeyValues;
import com.ibm.replication.cdc.scripting.ResultStringTable;
import com.ibm.replication.iidr.utils.Settings;
import com.ibm.replication.iidr.utils.Utils;

public class SetReplicationStatus {

	static Logger logger;

	private Settings settings;
	private SetReplicationStatusParms parms;

	private EmbeddedScript script;

	public SetReplicationStatus(String[] commandLineArguments) throws ConfigurationException, EmbeddedScriptException,
			SetReplicationStatusParmsException, FileNotFoundException, IOException {

		System.setProperty("log4j.configurationFile",
				System.getProperty("user.dir") + File.separatorChar + "conf" + File.separatorChar + "log4j2.xml");
		logger = LogManager.getLogger();

		PropertiesConfiguration versionInfo = new PropertiesConfiguration(
				"conf" + File.separator + "version.properties");

		logger.info(MessageFormat.format("Version: {0}.{1}.{2}, date: {3}",
				new Object[] { versionInfo.getString("buildVersion"), versionInfo.getString("buildRelease"),
						versionInfo.getString("buildMod"), versionInfo.getString("buildDate") }));

		settings = new Settings(this.getClass().getSimpleName() + ".properties");
		parms = new SetReplicationStatusParms(commandLineArguments);

		// Debug logging?
		if (parms.debug) {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			Configuration config = ctx.getConfiguration();
			LoggerConfig loggerConfig = config.getLoggerConfig("com.ibm.replication.iidr");
			loggerConfig.setLevel(Level.DEBUG);
			ctx.updateLoggers();
		}

		try {
			// Opening CHCCLP script (mandatory for embedded CHCCLP)
			logger.debug("Opening CHCCLP script");
			script = new EmbeddedScript();
			script.open();

			// Connect to the access server
			connectServer();

			processSubscription();

		} catch (EmbeddedScriptException e) {
			logger.error("Failed to to execute script " + script.getResultMessage());
			logger.error("Result Code : " + script.getResultCode());
		}

		logger.info("Finished setting the replication status");

	}

	// Connect to the Access Server
	private void connectServer() throws EmbeddedScriptException {
		logger.info("Connecting to the access server " + settings.asHostName);
		script.execute("connect server hostname " + settings.asHostName + " port " + settings.asPort + " username "
				+ settings.asUserName + " password " + settings.asPassword);
	}

	// Connect to a datastore
	private void connectDatastore(String datastoreName, String datastoreContext) throws EmbeddedScriptException {
		logger.info("Connecting to " + datastoreContext + " datastore " + datastoreName);
		script.execute("connect datastore name " + datastoreName + " context " + datastoreContext);
	}

	private void processSubscription() throws EmbeddedScriptException {

		// Connect to the source datastore
		connectDatastore(parms.datastore, "source");

		// Subscription routine
		logger.debug("Getting details for subscription " + parms.subscription);
		script.execute("show subscription name " + parms.subscription);
		ResultStringKeyValues subscriptionAttributes = (ResultStringKeyValues) script.getResult();
		String targetDatastore = subscriptionAttributes.getValue("Target Datastore");
		logger.debug("Target datastore for subscription " + parms.subscription + " is " + targetDatastore);

		// If target datastore different from source, connect to it
		if (!parms.datastore.equals(targetDatastore)) {
			logger.debug("Connecting to target datastore " + targetDatastore);
			script.execute("connect datastore name " + targetDatastore + " context target");
		}

		// Now that source and target datastores are set, select subscription
		script.execute("select subscription name " + parms.subscription);

		// Process direct table mappings
		logger.info(MessageFormat.format("Processing direct database table mappings for subscription {0}",
				new Object[] { parms.subscription }));
		script.execute(MessageFormat.format("list table mappings name {0}", new Object[] { parms.subscription }));
		ResultStringTable mappingsTables = (ResultStringTable) script.getResult();
		for (int tmTableRow = 0; tmTableRow < mappingsTables.getRowCount(); tmTableRow++) {
			String tmTableSchema = Utils.getSchema(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE"));
			String tmTableName = Utils.getTable(mappingsTables.getValueAt(tmTableRow, "SOURCE TABLE"));
			logger.debug(
					"Setting status of table " + tmTableSchema + "." + tmTableName + " to " + parms.subscriptionStatus);
			script.execute(MessageFormat.format("select table mapping sourceSchema {0} sourceTable {1}",
					new Object[] { tmTableSchema, tmTableName }));
			if (parms.subscriptionStatus.equalsIgnoreCase("active"))
				script.execute(MessageFormat.format("mark capture point", new Object[] { tmTableSchema, tmTableName }));
			else if (parms.subscriptionStatus.equals("refresh"))
				script.execute(MessageFormat.format("flag refresh", new Object[] { tmTableSchema, tmTableName }));

		}

		// Process rules-based table mappings
		logger.info(MessageFormat.format("Processing rules-based database table mappings for subscription {0}",
				new Object[] { parms.subscription }));
		script.execute(MessageFormat.format("list rule set tables name {0}", new Object[] { parms.subscription }));

		ResultStringTable rsMappingsTables = (ResultStringTable) script.getResult();
		for (int tmTableRow = 0; tmTableRow < rsMappingsTables.getRowCount(); tmTableRow++) {
			String tmTableSchema = rsMappingsTables.getValueAt(tmTableRow, "SCHEMA");
			String tmTableName = rsMappingsTables.getValueAt(tmTableRow, "TABLE NAME");
			logger.debug(
					"Setting status of table " + tmTableSchema + "." + tmTableName + " to " + parms.subscriptionStatus);
			if (parms.subscriptionStatus.equalsIgnoreCase("active"))
				script.execute(MessageFormat.format("mark capture point schema {0} table {1}",
						new Object[] { tmTableSchema, tmTableName }));
			else if (parms.subscriptionStatus.equals("refresh"))
				script.execute(MessageFormat.format("flag refresh schena {0} table {1}",
						new Object[] { tmTableSchema, tmTableName }));

		}

	}

	public static void main(String[] args) throws ConfigurationException, FileNotFoundException,
			EmbeddedScriptException, SetReplicationStatusParmsException, IOException {

		// Only set arguments when testing
		if (args.length == 1 && args[0].equalsIgnoreCase("*Testing*")) {
			args = "-d -ds CDC_Oracle_cdcdemoa -s CDC_RB -t active".split(" ");
		}

		new SetReplicationStatus(args);

	}

}
