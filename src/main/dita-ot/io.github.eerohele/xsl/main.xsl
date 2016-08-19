<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita2html="http://dita-ot.sourceforge.net/ns/200801/dita2html"
  exclude-result-prefixes="xs dita2html">

  <xsl:output method="xml" indent="yes"/>

  <xsl:variable name="css">
    <css>
      <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/metrics-graphics/2.6.0/metricsgraphics.min.css"/>
      <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,400italic,600,700" rel="stylesheet" type="text/css"/>
    </css>
  </xsl:variable>

  <xsl:variable name="javascript">
    <javascript>
      <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/0.0.1/prism.min.js"></script>
      <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js"></script>
      <script src="https://cdnjs.cloudflare.com/ajax/libs/metrics-graphics/2.6.0/metricsgraphics.min.js"></script>
      <script src="https://cdnjs.cloudflare.com/ajax/libs/platform/1.3.0/platform.min.js"></script>
      <script src="https://cdnjs.cloudflare.com/ajax/libs/classlist/2014.01.31/classList.min.js"></script>
      <script src="app.js"></script>
    </javascript>
  </xsl:variable>

  <xsl:template match="node()" mode="gen-user-head">
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <xsl:sequence select="$css/css/link"/>
  </xsl:template>

  <xsl:template match="*" mode="addFooterToHtmlBodyElement">
    <xsl:next-match/>
    <xsl:sequence select="$javascript/javascript/script"/>
  </xsl:template>

  <!-- sw-d -->

  <xsl:template match="*[contains(@class, ' sw-d/filepath ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setidaname"/>
      <xsl:apply-templates/>
    </code>
  </xsl:template>

  <!-- pr-d -->

  <xsl:template match="*[contains(@class, ' pr-d/codeblock ')]" name="topic.pr-d.codeblock">
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
    <xsl:call-template name="spec-title-nospace"/>

    <pre>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setscale"/>
      <xsl:call-template name="setidaname"/>

      <code>
        <xsl:apply-templates/>
      </code>
    </pre>

    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </xsl:template>

  <!-- task -->

  <xsl:template match="*[contains(@class, ' task/cmd ')]" name="topic.task.cmd">
    <xsl:choose>
      <xsl:when test="@href and @keyref">
        <xsl:apply-templates select="." mode="turning-to-link">
          <xsl:with-param name="keys" select="@keyref"/>
          <xsl:with-param name="type" select="'ph'"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:call-template name="commonattributes"/>
          <xsl:call-template name="setidaname"/>
          <xsl:apply-templates/>
        </p>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:call-template name="add-br-for-empty-cmd"/>
  </xsl:template>

</xsl:stylesheet>
