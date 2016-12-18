# IIDR Set Replication Status
==============

This utility command depends on CHCCLP and allows you to set the replication status of direct mapped and rule-based mapped tables. Table statuses can be set to `Active` and `Refresh`. As rule-based mapped tables do not support `Parked` table status, this option is not available.

## Installation
The GitHub repository contains all components required to run the SetReplicationStatus utility, including the Apache Commons and Log4j2 jar files. Besides the CDC Access Server or CDC Management Console, no further programs are needed. Classes have been compiled with Java 1.8, the version that comes with CDC Access Server 11.3.3.3. Therefore, after download, the utility can be used as is, and no compilation is required.

### Downloading the utility
Download and un-zip the master zip file from GitHub through the following link: [Download Zip](https://github.com/fketelaars/IIDR-Change-Table-Status/archive/master.zip).

### Required software versions
There is a strong dependency of the utility on CDC Access Server (or Management Console). At a minimum, the following versions are required:
- CDC Access Server (or Management Console): 11.3.3.3 fix pack 5613

## Usage
Once the tool has been configured, you can perform collect the statistics using the shell/command script that is provided in the utility's home directory.

* Linux/Unix: `SetReplicationStatus.sh -ds <source datastore> -s subscription -t refresh|active [-d]`
* Windows: `SetReplicationStatus.cmd -ds <source datastore> -s subscription -t refresh|active [-d]`


### Parameters
- ds: Specifies the source datastore of the subscriptons you wish to export.
- s: Specifies the subscription for which you want to set the table statuses to Refresh or Active
- t: Indicates the status that must be set: Refresh or Active (case insensitive)
- d: Optional. Displays debug messages for troubleshooting.

### Command example
`SetReplicationStatus.sh -ds CDC_Oracle_cdcdemoa -s S1 -t refresh`

The command connects to the Access Server and then the `CDC_Oracle_cdcdemoa` datastore. Subsequently it finds all mapped tables (direct and rule based) and sets the table status to `refresh`.
