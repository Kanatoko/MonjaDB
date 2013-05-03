package net.jumperz.io.multicore.example;

import net.jumperz.io.multicore.MParser;

public class MGrep
implements MParser
{
private String pattern;
//--------------------------------------------------------------------------------
public MGrep()
{
pattern = System.getProperty( "grep" );
}
//--------------------------------------------------------------------------------
public String parse( String s )
{
if( s.indexOf( pattern ) > -1 )
	{
	return s;
	}
else
	{
	return null;
	}
}
//--------------------------------------------------------------------------------
}