#!/bin/sh
################################################################################
# $Id: generateCurrent.sh,v 1.2 2005/04/29 02:48:15 colinmacleod Exp $
#
# This script can be used to generate the current database schema and data for
# the chosen dbs.
#
# Since: ivata groupware 0.11 (2005-04-26)
# Author: Colin MacLeod <colin.macleod@ivata.com>
# $Revision: 1.2 $
#
################################################################################
# Copyright (c) 2001 - 2005 ivata limited.
# All rights reserved.
# ---------------------------------------------------------
# ivata groupware may be redistributed under the GNU General Public
# License as published by the Free Software Foundation;
# version 2 of the License.
#
# These programs are free software; you can redistribute them and/or
# modify them under the terms of the GNU General Public License
# as published by the Free Software Foundation; version 2 of the License.
#
# These programs are distributed in the hope that they will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#
# See the GNU General Public License in the file LICENSE.txt for more
# details.
#
# If you would like a copy of the GNU General Public License write to
#
# Free Software Foundation, Inc.
# 59 Temple Place - Suite 330
# Boston, MA 02111-1307, USA.
#
#
# To arrange commercial support and licensing, contact ivata at
#                  http://www.ivata.com/contact.jsp
################################################################################
#
# $Log: generateCurrent.sh,v $
# Revision 1.2  2005/04/29 02:48:15  colinmacleod
# Data bugfixes.
# Changed primary key back to Integer.
#
# Revision 1.1  2005/04/27 15:06:37  colinmacleod
# Added script to create current torque-generated
# database scripts.
#
################################################################################
scriptDir=`dirname $0`

if [ "x$scriptDir" == "x." ]; then
    scriptDir=`pwd`"/$scriptDir"
fi

projectDir=`echo $scriptDir | sed "s/\/build\/src\/sh//"`
hibernateDir="$projectDir/hibernate"
exportDir="$projectDir/hibernate/export"
startdbDir="$projectDir/package/startdb"
installDir="$projectDir/package/install"
srcDBDir="$installDir/src/db"

# make maven in the hibernate dir to make sure it is up to date
echo ""
echo "---------------------------"
echo "BUILD HIBERNATE FILES"
cd "$hibernateDir"
maven

generateDB() {
    db=$1
    torqueDB=$2
    dialect=$3

    # first create the target directory, if it doesn't already exist
    dbDir="$srcDBDir/$db"
    if test ! -e "$dbDir"; then
        echo ""
        echo "---------------------------"
        echo "MAKE DIR $dbDir"
        mkdir -pv "$dbDir"
    fi

    echo ""
    echo "---------------------------"
    echo "GENERATING SCHEMA $db  ($dialect)"


    # go to the hibernate directory and run maven
    echo CHANGE DIR to $exportDir
    cd $exportDir

    echo maven -D"hibernate.dialect=$dialect" hibernate:schema-export
    maven -D"hibernate.dialect=$dialect" hibernate:schema-export || exit -1

    schemaSource="$exportDir/target/schema/schema-current.sql"
    schemaTarget="$dbDir/schema-current.sql"
    if test ! -e "$schemaSource"; then
        echo "No schema source '$schemaSource'"
        exit -1;
    fi

    echo ""
    echo "---------------------------"
    echo "GENERATING DATA $db  ($torqueDB)"

    # go to the startdb directory and run maven
    echo CHANGE DIR to $startdbDir
    cd $startdbDir
    echo maven -D "torque.database=$torqueDB" -D "torque.database.adaptor=$torqueDB" torque:sql torque:datasql || exit -1
    maven -D "torque.database=$torqueDB" -D "torque.database.adaptor=$torqueDB" torque:sql torque:datasql || exit -1

    # copy the files over
    dataSource="$startdbDir/target/sql/ivatagroupware-data.sql"
    dataTarget="$dbDir/data-current.sql"
    if test ! -e "$dataSource"; then
        echo "No data source '$dataSource'"
        exit -1;
    fi
    if [ "$db" == hypersonic ]; then
        # don't know why this is necessary
        echo "Replacing quotes in hypersonic data"
        perl -p -i -e  "s/\\\'/\'\'/g" "$dataSource"
    fi
    mv -v "$dataSource" "$dataTarget"
    mv -v "$schemaSource" "$schemaTarget"
}



generateDB db2 db2 net.sf.hibernate.dialect.DB2Dialect
generateDB db2as400 db2400 net.sf.hibernate.dialect.DB2400Dialect
generateDB hypersonic hypersonic net.sf.hibernate.dialect.HSQLDialect
generateDB sqlserver mssql net.sf.hibernate.dialect.SQLServerDialect
generateDB mysql mysql net.sf.hibernate.dialect.MySQLDialect
generateDB oracle oracle net.sf.hibernate.dialect.OracleDialect
generateDB pgsql postgresql net.sf.hibernate.dialect.PostgreSQLDialect
generateDB sapdb sapdb net.sf.hibernate.dialect.SAPDBDialect
generateDB sybase sybase net.sf.hibernate.dialect.SybaseDialect

