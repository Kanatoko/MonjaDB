package net.jumperz.app.MBitDog;

import java.io.*;
import net.jumperz.util.*;

public class MSizeLogger
extends MAbstractLogger
{
private int size;
//--------------------------------------------------------------------------------
public final void logImpl( String line )
throws IOException
{
if( stream == null )
	{
	rotateStream();
	}

int nextSize = size + line.length() + 1;
if( nextSize > rotate )
	{
	rotateLogFile();
	}

stream.write( line.getBytes( MCharset.CS_ISO_8859_1 ) );
stream.write( 0x0A );

size += line.length() + 1;
}
//--------------------------------------------------------------------------------
protected final void reset()
{
size = 0;
}
//--------------------------------------------------------------------------------
}