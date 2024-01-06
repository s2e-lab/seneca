#!/bin/sh
################################################################################
# $Id: sqltool.sh,v 1.3 2005/04/29 02:48:15 colinmacleod Exp $
#
# This little script was hacked together to hep me test against the startup db.
#
# To use it, you need to put a file called sqltool.rc in your home drive. It
# should contain the following (without the first hash character!):
#
# urlid startdb
# url jdbc:hsqldb:res:/db/igwstart
# username sa
# password
#
# You need to include the ivatagroupware/build/src/sh directory in your path,
# then you can just call 'sqltool.sh' from the command line.
#
# Since: ivata groupware 0.11 (2005-03-25)
# Author: Colin MacLeod <colin.macleod@ivata.com>
# $Revision: 1.3 $
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
# $Log: sqltool.sh,v $
# Revision 1.3  2005/04/29 02:48:15  colinmacleod
# Data bugfixes.
# Changed primary key back to Integer.
#
# Revision 1.2  2005/04/27 15:07:37  colinmacleod
# Changed for local file db (not memory).
#
# Revision 1.1  2005/04/11 10:53:45  colinmacleod
# Simple wrapper to HSQL sqltool.
#
################################################################################

TESTLIB=`dirname $0`/../../../testWeb/src/web/WEB-INF/lib

CLASSPATH="$TESTLIB/hsqldb-1.7.3.0.jar:$TESTLIB/ivatagroupware-startdb-0.11.jar"
export CLASSPATH
java org.hsqldb.util.SqlTool mem
################################################################################

