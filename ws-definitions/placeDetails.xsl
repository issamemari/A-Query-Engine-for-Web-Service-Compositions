<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <RESULT>
            <xsl:for-each select="*[local-name()='PlaceDetailsResponse']/*[local-name()='result']">
                <xsl:text>&#10;</xsl:text>
                <RECORD>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?place_id'><xsl:value-of select="place_id"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?name'><xsl:value-of select="name"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?type'><xsl:value-of select="type"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?phone_number'><xsl:value-of select="formatted_phone_number"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?full_address'><xsl:value-of select="formatted_address"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?rating'><xsl:value-of select="rating"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?website'><xsl:value-of select="website"/></ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> <ITEM ANGIE-VAR='?open'><xsl:value-of select="opening_hours/open_now"/></ITEM>
                </RECORD>
            </xsl:for-each>  
        </RESULT>
    </xsl:template>
</xsl:stylesheet>