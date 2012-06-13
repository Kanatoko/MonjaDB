package net.jumperz.util;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;

public class MScriptParser
extends DefaultHandler
{
private List list = new ArrayList();
boolean isScriptTag = false;
//--------------------------------------------------------------------------------
public void startElement( String uri, String localName, String qName, Attributes attributes )
{
if( qName.toLowerCase().equals( "script" ) )
	{
	isScriptTag = true;
	}
else
	{
	isScriptTag = false;	
	}
}
// --------------------------------------------------------------------------------
public void characters( char[] ch, int start, int len )
throws SAXException
{
if( isScriptTag )
	{
	String s = new String( ch, start, len );
	list.add( s );
	}
}
// --------------------------------------------------------------------------------
public List getScriptList()
{
return list;
}
// --------------------------------------------------------------------------------
}