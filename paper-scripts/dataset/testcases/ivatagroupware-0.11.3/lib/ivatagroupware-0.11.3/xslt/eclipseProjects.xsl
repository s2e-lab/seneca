<?xml version="1.0"?>

<!--
////////////////////////////////////////////////////////////////////////////////
// $Id: eclipseProjects.xsl,v 1.3 2005/04/09 17:19:15 colinmacleod Exp $
//
// Eclipse Stylesheet
// This stylesheet is used to replace all the project jars with their apropriate
// paths within eclipse. It also adds in the xdoclet source - hence the name.
//
// since: ivata op 0.9 (2004-01-31)
// Author: Colin MacLeod <colin.macleod@ivata.com>
// $Revision: 1.3 $
//
////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001 - 2005 ivata limited.
// All rights reserved.
// =========================================================
// ivata groupware may be redistributed under the GNU General Public
// License as published by the Free Software Foundation;
// version 2 of the License.
//
// These programs are free software; you can redistribute them and/or
// modify them under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 2 of the License.
//
// These programs are distributed in the hope that they will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// See the GNU General Public License in the file LICENSE.txt for more
// details.
//
// If you would like a copy of the GNU General Public License write to
//
// Free Software Foundation, Inc.
// 59 Temple Place - Suite 330
// Boston, MA 02111-1307, USA.
//
//
// To arrange commercial support and licensing, contact ivata at
//                  http://www.ivata.com/contact.jsp
//
////////////////////////////////////////////////////////////////////////////////
//
// $Log: eclipseProjects.xsl,v $
// Revision 1.3  2005/04/09 17:19:15  colinmacleod
// Changed copyright text to GPL v2 explicitly.
//
// Revision 1.2  2005/03/16 12:41:57  colinmacleod
// Fixed CVS paths.
//
// Revision 1.1.1.1  2005/03/10 17:50:12  colinmacleod
// Restructured ivata op around Hibernate/PicoContainer.
// Renamed ivata groupware.
//
// Revision 1.7  2004/12/31 19:21:08  colinmacleod
// Added comments and conversion of ivata masks project files.
//
// revision 1.6 2004/10/07 colinmacleod
// Streamlined and generalized subprojects.
//
// revision 1.5 2004/07/13 colinmacleod
// Moved project to POJOs from EJBs.
// Applied PicoContainer to services layer (replacing session EJBs).
// Applied Hibernate to persistence layer (replacing entity EJBs).
//
// revision 1.4 2004/03/27 colinmacleod
// XSLT now creates project dependencies in eclipse files.
//
// revision 1.3 2004/03/26 colinmacleod
// Split off EJB specifics so we can exclude CVS directories from all generated eclipse .classpath files.
//
// revision 1.2 2004/03/03 colinmacleod
// Restructured projects around ejb, jar and war artifacts.
//
// revision 1.1 2004/01/31 colinmacleod
// First version - corrects eclipse classpath
//
////////////////////////////////////////////////////////////////////////////////
-->
<xsl:stylesheet version="1.1"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="currentVersion"/>
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="classpath">
    <xsl:copy>
      <classpathentry excluding="**/CVS/" kind="src" path="src/test"/>
      <xsl:apply-templates select="*"/>
      <classpathentry kind="var" path="MAVEN_REPO/junit/jars/junit-3.8.1.jar"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="classpathentry[@kind='src' and (@path='src/java' or @path='src\java')]">
    <classpathentry excluding="**/CVS/" kind="src" path="src/java"/>
  </xsl:template>
  <xsl:variable name="ivatagroupware">MAVEN_REPO/ivatagroupware/jars/ivatagroupware-</xsl:variable>
  <xsl:variable name="ivatagroupwareDOS">MAVEN_REPO\ivatagroupware\jars\ivatagroupware-</xsl:variable>
  <xsl:template match="classpathentry[@kind='var' and starts-with(@path, $ivatagroupware)]">
    <xsl:variable name="afterOp"><xsl:value-of select="substring-after(@path, $ivatagroupware)"/></xsl:variable>
    <xsl:variable name="beforeDash"><xsl:value-of select="substring-before($afterOp, '-')"/></xsl:variable>
    <classpathentry kind="src" path="/{$beforeDash}"/>
  </xsl:template>
  <xsl:template match="classpathentry[@kind='var' and starts-with(@path, $ivatagroupwareDOS)]">
    <xsl:variable name="afterOpDOS"><xsl:value-of select="substring-after(@path, $ivatagroupwareDOS)"/></xsl:variable>
    <xsl:variable name="beforeDashDOS"><xsl:value-of select="substring-before($afterOpDOS, '-')"/></xsl:variable>
    <classpathentry kind="src" path="/{$beforeDashDOS}"/>
  </xsl:template>
  <xsl:template match="*|@*|comment()|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|comment()|text()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>

