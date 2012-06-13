package net.jumperz.app.MBitDog;

import java.io.*;
import net.jumperz.util.*;

public class MLineLogger
extends MAbstractLogger
{
private int lineCount;
//--------------------------------------------------------------------------------
public final void logImpl( String line )
throws IOException
{
if( stream == null )
	{
	rotateStream();
	}

if( lineCount == rotate )
	{
	rotateLogFile();
	}

stream.write( line.getBytes( MCharset.CS_ISO_8859_1 ) );
stream.write( 0x0A );

++lineCount;
}
//--------------------------------------------------------------------------------
protected final void reset()
{
lineCount = 0;
}
//--------------------------------------------------------------------------------
}