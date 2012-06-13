package net.jumperz.io;

import java.io.*;
import java.util.*;
import net.jumperz.util.*;

public class MSimpleLineReader
implements MLineReader
{
private InputStream is;
private BufferedReader reader;
// --------------------------------------------------------------------------------
public String readLine()
throws IOException
{
return reader.readLine();
}
// --------------------------------------------------------------------------------
public void setInputStream( InputStream in )
throws IOException
{
is = in;
reader = new BufferedReader( new InputStreamReader( is, MCharset.CS_ISO_8859_1 ) );
}
// --------------------------------------------------------------------------------
public int getLastDelimiterSize()
{
return 1;
}
// --------------------------------------------------------------------------------
public int getLastDelimiter()
{
return UNKNOWN;
}
// --------------------------------------------------------------------------------
public String getLastDelimiterString()
{
return "\n";
}
// --------------------------------------------------------------------------------
}