package net.jumperz.net;

import java.util.*;
import java.io.*;
import net.jumperz.util.*;

public class MMultipartParameter
implements MAbstractParameter
{
private String name = "";
private String value = "";
private List headerList = new ArrayList();
private String filename = "";
// --------------------------------------------------------------------------------
public MMultipartParameter( String s )
throws IOException
{
parse( s );
}
// --------------------------------------------------------------------------------
public MMultipartParameter( String name, String value, int type )
throws IOException
{
//name = MStringUtil.replaceAll( name, "\"", "\\\"" );

	// name and value must be LATIN1 Strings
parse( "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value );
}
// --------------------------------------------------------------------------------
public MMultipartParameter( String name, String value )
throws IOException
{
//name = MStringUtil.replaceAll( name, "\"", "\\\"" );

	// name and value must be LATIN1 Strings
parse( "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value );
}
// --------------------------------------------------------------------------------
private void parse( String in )
throws IOException
{
int index = in.indexOf( "\r\n\r\n" );
if( index == -1 )
	{
	throw new IOException( "Invalid format. No double line break found." );
	}
String headers = in.substring( 0, index );
value = in.substring( index + 4 ); //4 = length( "\r\n\r\n" )

	//parse header
boolean contentDispositionFound = false;
String[] array = headers.split( "\r\n" );
for( int i = 0; i < array.length; ++i )
	{
	String header = array[ i ];
	headerList.add( header );
	if( header.indexOf( "Content-Disposition" ) == 0 )
		{
		if( contentDispositionFound == true )
			{
			throw new IOException( "Double Content-Disposition found." );
			}
		contentDispositionFound = true;
		name = MRegEx.getMatch( ";[ ]*name=\"([^\"]*)\"", header );
		filename = MRegEx.getMatch( ";[ ]*filename=\"([^\"]*)\"", header );
		}
	}
}
//-------------------------------------------------------------------------------------------
public final void addHeaderValue( String name, String value )
{
if( name != null && value != null )
	{
	headerList.add( name + ": " + value );
	}
}
// --------------------------------------------------------------------------------
public String getFileName()
{
return filename;
}
// --------------------------------------------------------------------------------
public final void addHeader( String header )
{
if( header != null )
	{
	headerList.add( header );
	}
}
// --------------------------------------------------------------------------------
public List getHeaderList()
{
return headerList;
}
// --------------------------------------------------------------------------------
public String toString()
{
StringBuffer buf = new StringBuffer( 1024 );
for( int i = 0; i < headerList.size(); ++i )
	{
	buf.append( headerList.get( i ) );
	buf.append( "\r\n" );
	}
buf.append( "\r\n" );
buf.append( value );

return buf.toString();
}
//-------------------------------------------------------------------------------------------
public final boolean headerExists( String name )
{
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		return true;
		}
	}
return false;
}
//-------------------------------------------------------------------------------------------
public final String getHeaderValue( String name )
{
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		String value = header.substring( name.length() + 1 ); // value = " www.jumperz.net";
		int pos = 0;
		while( value.charAt( pos ) == ' ' )
			{
			pos++;
			}
		return value.substring( pos );
		}
	}
return "";
}
// --------------------------------------------------------------------------------
public void setFilename( String s )
{
if( !hasFilename() )
	{
	return;
	}

for( int i = 0; i < headerList.size(); ++i )
	{
	String header = ( String )getHeaderList().get( i );
	String[] array = header.split( ";\\s*" );
	for( int k = 0; k < array.length; ++k )
		{
		if( array[ k ].matches( "^filename=.*" ) )
			{
			String magic = "_XX_MAGIC_XX__";
			String repl = "filename=\"" + s + "\"";
			header = header.replaceFirst( "filename=\"[^\"]*\"", magic );
			header = MStringUtil.replaceFirst( header, magic, repl );
			getHeaderList().remove( i );
			getHeaderList().add( i, header );		
			}
		}
	}
}
// --------------------------------------------------------------------------------
public boolean hasFilename()
{
for( int i = 0; i < headerList.size(); ++i )
	{
	String header = ( String )headerList.get( i );
	String[] array = header.split( ";\\s*" );
	for( int k = 0; k < array.length; ++k )
		{
		if( array[ k ].toLowerCase().indexOf( "filename=" ) == 0 )
			{
			return true;
			}
		}
	}
return false;
}
//--------------------------------------------------------------------------------
public void setName( String s )
{
name = s;
}
// --------------------------------------------------------------------------------
public void setValue( String s )
{
value = s;
}
// --------------------------------------------------------------------------------
public int getType()
{
return MULTIPART;
}
// --------------------------------------------------------------------------------
public String getValue()
{
return value;
}
// --------------------------------------------------------------------------------
public String getName()
{
return name;
}
// --------------------------------------------------------------------------------
}