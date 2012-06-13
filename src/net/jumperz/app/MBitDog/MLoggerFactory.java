package net.jumperz.app.MBitDog;

import java.io.*;
import java.util.*;
import net.jumperz.util.*;

public class MLoggerFactory
{
public static final int DEFAULT_BUFSIZE = 8192;
//--------------------------------------------------------------------------------
public static List load( String configFileName )
throws IOException
{
/*
<logger>
fileName=work/bitdog/access
pattern=[^:]{1,}:[0-9]
condition=match
case_sensitive=yes
type=sizen
rotate=104857600
eat=yes
suffix=0000
command=/bin/gzip
buffering=true //option
buffer_size=0 //option
timestamp=no //option
</logger>
*/
BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( configFileName ), MCharset.CS_ISO_8859_1 ) );

try
	{
	String line = null;
	String fileName = null;
	String patternStr = null;
	String conditionStr = null;
	String caseSensitiveFlagStr = null;
	String type = null;
	String rotateStr = null;
	String eatFlagStr = null;
	String suffix = null;
	String command = null;
	String bufferingStr = "true";
	String timestampStr = "false";
	boolean negationFlag;
	boolean caseSensitiveFlag;
	boolean eatFlag = false;
	int rotate = 0;
	int bufsize = DEFAULT_BUFSIZE;
	boolean buffering = true;
	boolean timestamp = false;
	
	List loggerList = new ArrayList();
	
	for( int lineNumber = 1; ; ++lineNumber )
		{
		line = reader.readLine();
		if( line == null )
			{
			break;
			}
		if( line.equalsIgnoreCase( "<logger>" ) )
			{
			if( fileName			!= null
			 || patternStr			!= null
			 || conditionStr		!= null
			 || caseSensitiveFlagStr	!= null
			 || type			!= null
			 || rotateStr			!= null
			 || eatFlagStr			!= null
			 || suffix			!= null
			 || command			!= null
			 || rotate			!= 0
			  )
				{
				throwParseError( configFileName, lineNumber );
				}
			}
		else if( line.equalsIgnoreCase( "</logger>" ) )
			{
			if( fileName			== null
			 || patternStr			== null
			 || conditionStr		== null
			 || caseSensitiveFlagStr	== null
			 || type			== null
			 || rotateStr			== null
			 || eatFlagStr			== null
			 || suffix			== null
			 || command			== null
			 || rotate			== 0
			  )
				{
				throwParseError( configFileName, lineNumber );
				}
	
				// start construction
			MAbstractLogger logger = null;
			if( type.equalsIgnoreCase( "size" ) )
				{
				logger = new MSizeLogger();
				}
			else if( type.equalsIgnoreCase( "line" ) )
				{
				logger = new MLineLogger();
				}
			else if( type.equalsIgnoreCase( "null" ) )
				{
				logger = new MNullLogger();
				}
			else
				{
				throwParseError( configFileName, lineNumber );
				}
			
			eatFlag = MStringUtil.meansTrue( eatFlagStr );
			caseSensitiveFlag = MStringUtil.meansTrue ( caseSensitiveFlagStr );
			negationFlag = ( conditionStr.equalsIgnoreCase( "NOT MATCH" ) );
			buffering = MStringUtil.meansTrue( bufferingStr );
			timestamp = MStringUtil.meansTrue( timestampStr );
			
			logger.setFileName( fileName );
			logger.setPattern( patternStr, !caseSensitiveFlag );
			logger.setNegationFlag( negationFlag );
			logger.setRotate( rotate );
			logger.setCommand( command );
			logger.setEat( eatFlag );
			logger.setSuffix( suffix );
			logger.setBuffering( buffering );
			logger.setBufsize( bufsize );
			logger.setTimestamp( timestamp );
			
			loggerList.add( logger );
			
			fileName		= null;
			patternStr		= null;
			conditionStr		= null;
			caseSensitiveFlagStr	= null;
			type			= null;
			rotateStr		= null;
			eatFlagStr		= null;
			suffix			= null;
			command			= null;
			bufferingStr		= "true";
			timestampStr		= "false";
			rotate			= 0;
			}
		else if( line.indexOf( "fileName" ) == 0 )
			{
			if( fileName != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			fileName = getParam( line, "fileName=" );
			}
		else if( line.indexOf( "pattern" ) == 0 )
			{
			if( patternStr != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			patternStr = getParam( line, "pattern=" );
			}
		else if( line.indexOf( "condition" ) == 0 )
			{
			if( conditionStr != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			conditionStr = getParam( line, "condition=" );
			}
		else if( line.indexOf( "case_sensitive=" ) == 0 )
			{
			if( caseSensitiveFlagStr != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			caseSensitiveFlagStr = getParam( line, "case_sensitive=" );
			}
		else if( line.indexOf( "type=" ) == 0 )
			{
			if( type != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			type = getParam( line, "type=" );
			}
		else if( line.indexOf( "rotate=" ) == 0 )
			{
			if( rotate != 0 )
				{
				throwParseError( configFileName, lineNumber );
				}
			rotateStr = getParam( line, "rotate=" );
			rotate = Integer.parseInt( rotateStr );
			/*
			if( rotate < 1 )
				{
				throwParseError( configFileName, lineNumber );
				}
			*/
			}
		else if( line.indexOf( "command=" ) == 0 )
			{
			if( command != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			command = getParam( line, "command=" );
			}
		else if( line.indexOf( "eat=" ) == 0 )
			{
			if( eatFlagStr != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			eatFlagStr = getParam( line, "eat=" );
			}
		else if( line.indexOf( "suffix=" ) == 0 )
			{
			if( suffix != null )
				{
				throwParseError( configFileName, lineNumber );
				}
			suffix = getParam( line, "suffix=" );
			/*
			if( !suffix.matches( "^0{1,}$" ) )
				{
				throwParseError( configFileName, lineNumber );			
				}
			*/
			}
		else if( line.indexOf( "buffering=" ) == 0 )
			{
			bufferingStr = getParam( line, "buffering=" );
			}	
		else if( line.indexOf( "timestamp=" ) == 0 )
			{
			timestampStr = getParam( line, "timestamp=" );
			}	
		else if( line.indexOf( "buffer_size=" ) == 0 )
			{
			bufsize = Integer.parseInt( getParam( line, "buffer_size=" ) );
			if( bufsize < 1 )
				{
				bufsize = DEFAULT_BUFSIZE;
				}
			}	
		}
	
	return loggerList;
	}
finally
	{
	reader.close();
	}
}
//--------------------------------------------------------------------------------------
private static String getParam( String line, String paramName )
{
return line.substring( paramName.length() );
}
//--------------------------------------------------------------------------------------
private static void throwParseError( String configFileName, int lineNumber )
throws IOException
{
throw new IOException( "Parse error. " + configFileName + " line: " + lineNumber );
}
//--------------------------------------------------------------------------------
}
