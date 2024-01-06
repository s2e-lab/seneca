#!/bin/sh
################################################################################
# $Id: warcp.sh,v 1.2 2005/04/09 17:19:14 colinmacleod Exp $
#
# This script copies over jsp files which have changed. You need to run it from
# the directory it is in. (Run the script with no arguments for usage help.)
#
# Since: ivata groupware 0.9 (ivata) op 0.10 (2004-12-29)
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
# $Log: warcp.sh,v $
# Revision 1.2  2005/04/09 17:19:14  colinmacleod
# Changed copyright text to GPL v2 explicitly.
#
# Revision 1.1.1.1  2005/03/10 17:50:12  colinmacleod
# Restructured ivata op around Hibernate/PicoContainer.
# Renamed ivata groupware.
#
# Revision 1.3  2004/12/31 19:26:15  colinmacleod
# added checking for addressbook (lower/upper case)
#
# Revision 1.2  2004/12/29 09:39:10  colinmacleod
# Added exit 0 at the end.
#
# Revision 1.1  2004/12/29 09:37:35  colinmacleod
# First version in CVS.
#
################################################################################

args=$#

if [ "$args" -ne 2 ]; then
  echo "$0: script to copy over jsp files under development." 1>&2
  echo "    this script must be run from the openportal/war directory." 1>&2
  echo "" 1>&2
  echo "Usage: $0 {system} {file}" 1>&2
  echo "    where:" 1>&2
  echo "          {system}:     one of the ivata op subsystems (addressbook, core, etc.)" 1>&2
  echo "          {file}:       jsp file to be copied over" 1>&2
  exit -1;
fi

system=$1
file=$2
targetfile=$file
targetdir=$system


# some exceptions to the targetdir = system...
if [ "x$targetdir" = "xaddressbook" ]; then
  targetdir=addressBook
fi
if [ "x$targetdir" = "xcore" ]; then
  targetdir='.'
fi
if [ "x$targetdir" = "xsearch" ]; then
  targetdir='.'
fi
if [ "x$targetdir" = "xsecurity" ]; then
  targetdir='.'
fi
if [ "x$targetdir" = "xwar" ]; then
  targetdir='.'
  system=package/war
fi
if [ "x$targetdir" = "xwebgui" ]; then
  targetdir='.'
fi
if [ "x$targetdir" = "xwebmail" ]; then
  targetdir=mail
fi
# everything which is in a WEB-INF directory, goes to the WEB-INF directory!
dir=`dirname $file`
test=`echo "x$file" | grep "^WEB-INF"`
if [ "x$test" != "x" ]; then
  targetdir='.'
fi
# if you specified a style, that goes into the style directory
test=`echo "x$file" | grep "^xstyle"`
if [ "x$test" != "x" ]; then
  # some juggling here - the targetdir comes after the style
  targetfile=`echo $file | sed "s/^style\///"`
  targetdir=style/$targetdir
fi

cp -v ../../$system/src/web/$file target/ivatagroupware-war/$targetdir/$targetfile
exit 0;

