package net.jumperz.io;

import java.io.*;

public interface MLineReader
{
public static final int CR	= 0;
public static final int LF	= 1;
public static final int CRLF	= 2;
public static final int NULL	= 3;
public static final int UNKNOWN	= 4;
// --------------------------------------------------------------------------------
public String readLine() throws IOException;
public void setInputStream( InputStream in ) throws IOException;
public int getLastDelimiter();
public int getLastDelimiterSize();
public String getLastDelimiterString();
// --------------------------------------------------------------------------------
}