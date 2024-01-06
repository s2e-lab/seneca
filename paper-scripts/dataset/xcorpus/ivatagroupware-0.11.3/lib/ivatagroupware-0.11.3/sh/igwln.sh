#!/bin/sh
################################################################################
# $Id: igwln.sh,v 1.4 2005/04/30 13:07:04 colinmacleod Exp $
#
# This script creates a dummy war project containing hard links all the others,
# so MyEclipse is happy to deploy our distributed webapp.
# (I tried using symlinks, but MyEclipse wasn't having it.)
#
# Being a shell script which requires hard links, none of this works on Windows,
# or even cygwin, I'm afraid. If you're using Windows, you have to build the war
# file and deploy it manually.
#
# ======== Program Parameters & Features ========
#
# Run with no parameters, the program will create all missing hard links, in a
# dummy subproject called "testWeb".
#
# *** Example: ***
#       igwln.sh     --> creates missing hard links in testWeb/src/web
#
# If you specify "clear" as a first parameter, the entire webapp directory
# (testWeb/src/web) is deleted and recreated with all links.
#
# *** Example: ***
#       igwln.sh clear  --> recreates all hard links in testWeb/src/web
#
#
# Create a symlink to this file called igwrm and it can be used to remove a link
# like this
#
#       igwrm {system} {file}
#
# where:
#       system:   one of the project subprojects, such as addressbook,
#                     calendar, core, library, etc.
#       file:     name of a web file in that directory.
#
# *** Examples: ***
#       igwrm addressbook index.jsp   --> remove link
#                                         testWeb/src/web/addressBook/index.jsp
#       igwrm library submit.jsp      --> remove link
#                                         testWeb/src/web/library/submit.jsp
#       igwrm webmail compose.jsp     --> remove link
#                                         testWeb/src/web/mail/compose.jsp
#
# This script was hacked out of the ashes of warcp.sh.
#
# Since: ivata groupware 0.10 (2005-03-04)
# Author: Colin MacLeod <colin.macleod@ivata.com>
# $Revision: 1.4 $
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
# $Log: igwln.sh,v $
# Revision 1.4  2005/04/30 13:07:04  colinmacleod
# Added conf files from ivatamasks.
#
# Revision 1.3  2005/04/11 09:13:43  colinmacleod
# Added groovy files.
#
# Revision 1.2  2005/04/09 17:19:14  colinmacleod
# Changed copyright text to GPL v2 explicitly.
#
# Revision 1.1.1.1  2005/03/10 17:50:13  colinmacleod
# Restructured ivata op around Hibernate/PicoContainer.
# Renamed ivata groupware.
#
################################################################################

# set to true to debug! this will cause the program not to actually do anything
# but will let you see what the target file and directory would have been, had
# you acted.
debug=false

scriptDir=`dirname $0`
scriptName=`basename $0`

if [ "x$scriptDir" == "x." ]; then
    scriptDir=`pwd`"/$scriptDir"
fi

projectDir=`echo $scriptDir | sed "s/\/build\/src\/sh//"`
testWebDir="$projectDir/testWeb"
srcWeb="$testWebDir/src/web"
srcXML="$testWebDir/src/xml"
classesDir="$srcWeb/WEB-INF/classes"

# put out some debugging info at the start
if [ "x$debug" == "xtrue" ]; then
    echo "DEBUG MODE: script will not act."
    echo "script dir: $scriptDir"
    echo "project dir: $projectDir"
    echo "testWeb dir: $testWebDir"
    echo
fi

# work out the target directory and file name based on the system and input file
getTarget() {
    local system=$1
    local file=$2
    targetFile="$file"
    targetDir="$system"

    if [ "x$targetDir" = "xaddressbook" ]; then
      targetDir=addressBook
    fi
    if [ "x$targetDir" = "xcore" ]; then
      targetDir='.'
    fi
    if [ "x$targetDir" = "xsearch" ]; then
      targetDir='.'
    fi
    if [ "x$targetDir" = "xsecurity" ]; then
      targetDir='.'
    fi
    if [ "x$targetDir" = "xwar" ]; then
      targetDir='.'
    fi
    if [ "x$targetDir" = "xwebgui" ]; then
      targetDir='.'
    fi
    if [ "x$targetDir" = "xwebmail" ]; then
      targetDir=mail
    fi
    if [ "x$targetDir" = "xweb" ]; then
      targetDir='.'
    fi
    if [ "x$targetDir" = "xwebtheme" ]; then
      targetDir=theme
    fi
    # everything which is in a WEB-INF directory, goes to the WEB-INF directory!
    test=`echo "x$file" | grep "^xWEB-INF"`
    if [ "x$test" != "x" ]; then
      targetDir='.'
    fi
    # if you specified a style, that goes into the style directory
    test=`echo "x$file" | grep "^xstyle"`
    if [ "x$test" != "x" ]; then
      # some juggling here - the targetdir comes after the style,
      # and after the template too, if there is one
      test=`echo "x$file" | grep "^xstyle.template"`
      if [ "x$test" != "x" ]; then
          targetFile=`echo $file | sed "s/^style\/template\///"`
          if [ "x$targetDir" = "xtheme" ]; then
            targetDir=style/template
          else
            targetDir=style/template/$targetDir
          fi
      else
          targetFile=`echo $file | sed "s/^style\///"`
          targetDir=style/$targetDir
      fi
    fi

    # append the path to the target dir - split off just the file name with no
    # path
    targetSubDir=`dirname $targetFile`
    if [ "z$targetSubDir" != "z." ]; then
        targetDir="$targetDir/$targetSubDir"
    fi

    targetFile=`basename $targetFile`
    # if debug mode, out out some info
    if [ "x$debug" == "xtrue" ]; then
        echo "system: $system"
        echo "target dir: $targetDir"
        echo "target file: $targetFile"
    fi

}

# if you create a symlink called igwrm to this script, it can be used to delete
# the links individually. useful when you want to rename/delete a jsp
if [ "z$scriptName" == "zigwrm" ]; then
    system="$1"
    file="$2"
    if [ "z$system" == "z" ] || [ "z$file" == "z" ]; then
        echo "Useage: "
        echo "       $scriptName {system} {file}"
        echo
        echo " where:"
        echo "       system:   one of the project subprojects, such as addressbook,"
        echo "                     calendar, core, library, etc."
        echo "       file:     name of a source file in that directory."
        echo
        echo "See documentation at the start of this script for more information "
        echo "and examples."
        exit -1;
    fi
    getTarget $system $file

    thisSourceFile="$projectDir/$system/src/web/$file"
    link="$srcWeb/$targetDir/$targetFile"
    if [ "x$debug" == "xtrue" ]; then
        echo "would remove file:"
        echo "  $thisSourceFile"
        echo "would remove link:"
        echo "  $link"
        echo
    else
        rm -vi "$thisSourceFile"
        rm -v "$link"
    fi
else
    # we're setting up a new copy of the webdir - clear any existing one
    # only do this if the first param is "clear"
    if [ "x$debug" != "xtrue" ]; then
        if [ "z$1" == "zclear" ]; then
            echo "Removing previous web dir..."
            if test -e  "$srcWeb"; then
                rm -rf "$srcWeb"
            fi
        fi
        if test ! -e "$srcWeb"; then
            mkdir -p "$srcWeb"
        fi
    fi

    cd $projectDir/..
    cvslocal=`pwd`
    for webFile in `find ivatagroupware/*/src/web ivatagroupware/package/*/src/web ivatamasks/*/src/web -type f | grep -v CVS | grep -v ivatamasks/demo | grep -v testWeb`;
    do
        sourceDir=`echo $webFile | sed "s/\/src.*//"`
        system=`echo $sourceDir | sed "s/ivatagroupware\///"  | sed "s/package\///" | sed "s/ivatamasks\///" `
        file=`echo $webFile | sed "s/$system\/src\/web\///" | sed "s/ivatagroupware\///"  | sed "s/package\///" | sed "s/ivatamasks\///"`

        getTarget $system $file

        if [ "x$debug" == "xtrue" ]; then
            echo "web file: $webFile"
            if test ! -e "$srcWeb/$targetDir/$targetFile"; then
                echo "  link  $cvslocal/$webFile"
                echo "  to    $srcWeb/$targetDir/$targetFile"
            fi
            echo
        fi

        # only act if debug is not true
        if [ "x$debug" != "xtrue" ]; then
            if test ! -e "$srcWeb/$targetDir"; then
                mkdir -p "$srcWeb/$targetDir"
            fi
            # only create the link if it is not already there!
            if test ! -e "$srcWeb/$targetDir/$targetFile"; then
                ln -v "$cvslocal/$webFile" "$srcWeb/$targetDir/$targetFile"
            fi
        fi
    done
    # bind the hibernate and groovy files
    hibernateSourceDir="ivatagroupware/hibernate/target/xdoclet/hibernatedoclet"
    groovySourceDir="ivatagroupware"
    for hibernateFile in `find $hibernateSourceDir -name '*.xml'` `find "$groovySourceDir" -name '*.groovy' | grep -v classes | grep -v target`;
    do
        targetDir=`dirname $hibernateFile`
        targetDir=`echo $targetDir | sed "s/ivatagroupware\/hibernate\/target\/xdoclet\/hibernatedoclet[\/]*//"`
        targetDir=`echo $targetDir | sed "s/ivatagroupware\/[^\/]*\/src\/groovy[\/]*//"`
        targetDir=`echo $targetDir | sed "s/ivatagroupware\/package\/war\/src\/groovy[\/]*//"`
        targetFile=`basename $hibernateFile`
        if [ "x$debug" == "xtrue" ]; then
            echo "hibernate file: $hibernateFile"
            if test ! -e "$classesDir/$targetDir/$targetFile"; then
                echo "  link  $hibernateFile"
                echo "  to    $classesDir/$targetDir/$targetFile"
            fi
            echo
        fi

        # only act if debug is not true
        if [ "x$debug" != "xtrue" ]; then
            if test ! -e "$classesDir/$targetDir"; then
                mkdir -p "$classesDir/$targetDir"
            fi
            # only create the link if it is not already there!
            if test ! -e "$classesDir/$targetDir/$targetFile"; then
                ln -v "$hibernateFile" "$classesDir/$targetDir/$targetFile"
            fi
        fi
    done

    # copy conf files
    cd $projectDir/.. || die "Can't change to the project dir .. '$projectDir/..': $!";
    for confDir in `find ivatagroupware ivatamasks -name "conf" -type d`;
    do
        for confFile in `find "$confDir" -type f | grep -v CVS`;
        do
            targetFile=$confFile
            targetFile=`echo $targetFile | sed "s/.*src\/conf\///"`
            confTarget="$classesDir/$targetFile"
            confTargetDir=`dirname $confTarget`
            if test ! -e "$confTargetDir"; then
                mkdir -p "$confTargetDir"
            fi
            if test ! -e "$confTarget"; then
                if [ "x$debug" == "xtrue" ]; then
                    echo "Would copy conf file $confFile"
                else
                    cp -v "$confFile" "$confTarget"
                fi
            fi
        done
    done

    # go thro' all the web libraries - you must have run maven on the
    # ivatagroupware main project first :-)
    cd $projectDir || die "Can't change to the project dir '$projectDir': $!";
    libDir="$projectDir/package/war/target/ivatagroupware-war/WEB-INF/lib"
    if test -e "$libDir"; then
        libTargetDir="$srcWeb/WEB-INF/lib"
        if test ! -e "$libTargetDir"; then
            mkdir -p "$libTargetDir"
        fi
        for libSource in `find "$libDir" -type f | grep -v "ivatagroupware[-.a-zA-Z_0-9]*jar"  | grep -v "ivatamasks[-.a-zA-Z_0-9]*jar"` $projectDir/package/war/target/ivatagroupware-war/WEB-INF/lib/ivatagroupware-startdb*.jar;
        do
            jarFile=`basename "$libSource"`
            libTarget="$libTargetDir/$jarFile"
            if test ! -e "$libTarget"; then
                if [ "x$debug" == "xtrue" ]; then
                    echo "Would copy library $jarFile"
                else
                    cp -v "$libSource" "$libTarget"
                fi
            fi
        done
    else
        echo "No WEB-INF/lib found. Run maven first, then run $scriptName again to copy missing libraries."
    fi
fi

exit 0

