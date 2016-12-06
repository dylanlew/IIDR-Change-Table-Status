/****************************************************************************
 ** Licensed Materials - Property of IBM 
 ** IBM InfoSphere Change Data Capture
 ** 5724-U70
 ** 
 ** (c) Copyright IBM Corp. 2001, 2016 All rights reserved.
 ** 
 ** The following sample of source code ("Sample") is owned by International 
 ** Business Machines Corporation or one of its subsidiaries ("IBM") and is 
 ** copyrighted and licensed, not sold. You may use, copy, modify, and 
 ** distribute the Sample for your own use in any form without payment to IBM.
 ** 
 ** The Sample code is provided to you on an "AS IS" basis, without warranty of 
 ** any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR 
 ** IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 ** MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do 
 ** not allow for the exclusion or limitation of implied warranties, so the above 
 ** limitations or exclusions may not apply to you. IBM shall not be liable for 
 ** any damages you suffer as a result of using, copying, modifying or 
 ** distributing the Sample, even if IBM has been advised of the possibility of 
 ** such damages.
 *****************************************************************************/

package com.ibm.replication.iidr.commands.chcclp;

import org.apache.commons.cli.*;

public class SetReplicationStatusParms {

	private Options options;
	private HelpFormatter formatter;
	private CommandLineParser parser;
	private CommandLine commandLine;

	public boolean debug;
	public String datastore;
	public String subscription;
	public String subscriptionStatus;

	public SetReplicationStatusParms(String[] commandLineArguments) throws SetReplicationStatusParmsException {
		// Initialize parameters
		debug = false;
		datastore = "";
		formatter = new HelpFormatter();
		parser = new DefaultParser();
		options = new Options();
		subscription = null;
		subscriptionStatus = null;

		options.addOption("d", false, "Show debug messages");
		options.addOption("ds", true, "Source datastore");
		options.addOption("s", true, "Subscription to select tables from");
		options.addOption("t", true, "Status to set (refresh | active)");

		try {
			commandLine = parser.parse(options, commandLineArguments);
		} catch (ParseException e) {
			sendInvalidParameterException("");
		}

		this.debug = commandLine.hasOption("d");

		// Datastore parameter is mandatory
		if (commandLine.getOptionValue("ds") != null) {
			datastore = commandLine.getOptionValue("ds");

		} else
			sendInvalidParameterException("Datastore (ds parameter) must be specified");

		// Subscription parameter is mandatory
		if (commandLine.getOptionValue("s") != null) {
			subscription = commandLine.getOptionValue("s");

		} else
			sendInvalidParameterException("Subscription must be specified");

		// Subscription parameter is mandatory
		if (commandLine.getOptionValue("t") != null) {
			subscriptionStatus = commandLine.getOptionValue("t");
			if (!subscriptionStatus.equalsIgnoreCase("refresh") && !subscriptionStatus.equalsIgnoreCase("active"))
				sendInvalidParameterException("Subscription status must be REFRESH or ACTIVE");

		} else
			sendInvalidParameterException("Subscription status must be specified");

	}

	// Method to send exception
	private void sendInvalidParameterException(String message) throws SetReplicationStatusParmsException {
		formatter.printHelp("CollectCDCStats", message, this.options, "", true);
		throw new SetReplicationStatusParmsException("Error while validating parameters");
	}

}
