<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <RESULT>
            <xsl:for-each select="*[local-name()='PlaceSearchResponse']/*[local-name()='result']">
                <xsl:text>&#10;</xsl:text>
                <RECORD>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?location'><xsl:value-of select="geometry/location/lat"/>, <xsl:value-of select="geometry/location/lng"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?type'><xsl:value-of select="type"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?place_id'><xsl:value-of select="place_id"/></ITEM>
                </RECORD>
            </xsl:for-each>  
        </RESULT>
    </xsl:template>
</xsl:stylesheet>
