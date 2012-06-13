package net.jumperz.app.MBitDog;

import java.io.*;
import java.util.regex.*;
import java.text.*;
import net.jumperz.util.*;
import java.util.*;

public abstract class MAbstractLogger
{
protected String fileName;
protected Pattern pattern;
protected boolean negationFlag = false;
protected int rotate;
protected boolean eat;
protected String suffix;
protected String command;
protected int bufsize;
protected boolean buffering = true;
protected boolean timestamp = false;

private boolean closed = false;

protected String currentLogFileName;
protected OutputStream stream;
int suffixIndex;

public abstract void logImpl( String line ) throws IOException;
protected abstract void reset();
//--------------------------------------------------------------------------------
public void cleanUp( boolean isNormalShutdown )
{
synchronized( this )
	{
	if( closed )
		{
		return;
		}
	else
		{
		closed = true;
		}
	}
try
	{
	if( stream != null )
		{
		stream.flush();
		stream.close();
		}
	if( isNormalShutdown )
		{
		execCommand();	
		}
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
}
// --------------------------------------------------------------------------------
public void log( String line )
throws IOException
{
if( timestamp )
	{
	String dateStr = new Date().toString();
	StringBuffer buf = new StringBuffer( line.length() + dateStr.length() + 10 );
	buf.append( dateStr );
	buf.append( " " );
	buf.append( line );
	logImpl( buf.toString() );
	}
else
	{
	logImpl( line );
	}
}
//--------------------------------------------------------------------------------
private void execCommand()
throws IOException
{
if( !command.equals( "none" ) 
 && !command.equals( "" )
  )
	{
	Runtime.getRuntime().exec( command + " " + currentLogFileName );
	}
}
//--------------------------------------------------------------------------------
protected void rotateLogFile()
throws IOException
{
stream.close();
execCommand();
rotateStream();

suffixIndex++;
reset();
}
//--------------------------------------------------------------------------------
protected void rotateStream()
throws IOException
{
DecimalFormat format = new DecimalFormat( suffix );

File dir = new File( fileName ).getParentFile();
if( !dir.exists() )
	{
	MSystemUtil.createDir( dir.getAbsolutePath() );
	}
String[] fileList = dir.list();
List l = new ArrayList();
for( int i = 0; i < fileList.length; ++i )
	{
	l.add( fileList[ i ] );
	}

	// determine the filename
for( ;;++suffixIndex )
	{
	String suffixStr = format.format( suffixIndex );
	currentLogFileName = fileName + "." + suffixStr;
	File file = new File( currentLogFileName );
	String shortFileName = file.getName();

	boolean exists = false;
	int len = shortFileName.length();
	for( int i = 0; i < l.size(); ++i )
		{
		String _str = ( String )l.get( i );
		if( _str.length() >= len )
			{
			if( _str.substring( 0, len ).equalsIgnoreCase( shortFileName ) )
				{
				exists = true;
				l.remove( _str );
				break;
				}
			}
		}

	if( exists == false )
		{
		break;
		}
	}

	//open stream
stream = new FileOutputStream( currentLogFileName, true );
if( buffering )
	{
	stream = new BufferedOutputStream( stream, bufsize );
	}
}
//--------------------------------------------------------------------------------
public final boolean match( String line )
{
Matcher matcher = pattern.matcher( line );
boolean match = matcher.find();
return ( match != negationFlag );
}
//--------------------------------------------------------------------------------
public final void setPattern( String patternStr, boolean ignoreCase )
{
if( ignoreCase )
	{
	pattern = Pattern.compile( patternStr, Pattern.CASE_INSENSITIVE );
	}
else
	{
	pattern = Pattern.compile( patternStr );
	}
}
//--------------------------------------------------------------------------------
public String getCommand() {
	return command;
}

public boolean isEater() {
	return eat;
}

public String getFileName() {
	return fileName;
}

public int getRotate() {
	return rotate;
}

public boolean isNegationFlag() {
	return negationFlag;
}

public String getSuffix() {
	return suffix;
}

public void setCommand(String string) {
	command = string;
}

public void setEat(boolean b) {
	eat = b;
}

public void setFileName(String string) {
	fileName = string;
}

public void setRotate(int i) {
	rotate = i;
}

public void setNegationFlag(boolean b) {
	negationFlag = b;
}

public void setSuffix(String string) {
	suffix = string;
}

public boolean isBuffering()
{

return buffering;
}

public void setBuffering( boolean buffering )
{

this.buffering = buffering;
}

public int getBufsize()
{

return bufsize;
}

public void setBufsize( int bufsize )
{

this.bufsize = bufsize;
}

public void setTimestamp( boolean timestamp )
{

this.timestamp = timestamp;
}

public boolean getTimestamp()
{
return timestamp;
}

}