package net.jumperz.io;

import java.io.*;
import java.util.Random;

	//test: MSystemUtilTest
public class MSlowInputStream
extends InputStream
{
private InputStream in;
private Random random = new Random();
//--------------------------------------------------------------------------------
public MSlowInputStream( InputStream in )
{
this.in = in;
}
//--------------------------------------------------------------------------------
public int read()
throws IOException
{
return in.read();
}
//--------------------------------------------------------------------------------
public int read( byte[] b )
throws IOException
{
return read( b, 0, b.length );
}
//--------------------------------------------------------------------------------
public int read( byte[] b, int off, int len )
throws IOException
{
return in.read( b, off, random.nextInt( len + 1 ) );
}
//--------------------------------------------------------------------------------
}