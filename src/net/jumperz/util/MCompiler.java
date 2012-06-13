package net.jumperz.util;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class MCompiler
{
public static final String CLASSNAME = "_CLASSNAME_";
// --------------------------------------------------------------------------------
public static boolean available()
{
String javaHome = System.getProperty( "java.home" );
File tools = new File( javaHome + "/../lib/tools.jar" );
if( !tools.exists() )
	{
	return false;
	}
return true;
}
//--------------------------------------------------------------------------------
public Class compile( String code )
throws MCompilationException
{
return compile( new String[]{}, code );
}
//--------------------------------------------------------------------------------
public Class compile( String[] args, String code )
throws MCompilationException
{
File file = null;
File classFile = null;
try
	{
	file = File.createTempFile( "tmp", ".java" );
	String srcFileName = file.getAbsolutePath();
	String className = srcFileName.replaceFirst( "\\.java", "" );
	className = className.replaceFirst( ".*/", "" );
	MStringUtil.saveStringToFile( code.replaceFirst( CLASSNAME, className ), srcFileName );
	String[] args2 = new String[ args.length + 1 ];
	for( int i = 0; i < args.length; ++i )
		{
		args2[ i ] = args[ i ];
		}
	args2[ args.length ] = srcFileName;
	int result = compile( args2 );
	if( result != 0 )
		{
		throw new MCompilationException( "compile error" );
		}
	String classFileName = srcFileName.replaceFirst( "\\.java", "\\.class" );
	classFile = new File( classFileName );
	String tmpDirName = file.getParentFile().getAbsolutePath();
	URL dirUrl = new URL( "file:" + tmpDirName + "/" );
	URLClassLoader cl = new URLClassLoader( new URL[]{ dirUrl } );
	return cl.loadClass( className );
	}
catch( Exception e )
	{
	throw new MCompilationException( e.getMessage() );
	}
finally
	{
	//file.delete();
	//classFile.delete();
	}
}
// --------------------------------------------------------------------------------
public int compile( String[] args )
throws MCompilationException
{
try
	{
	String javaHome = System.getProperty( "java.home" );
	File tools = new File( javaHome + "/../lib/tools.jar" );
	String toolsFileName = tools.getCanonicalPath();

	URL toolsURL = new URL( "file:" + toolsFileName );
	URLClassLoader cl = new URLClassLoader( new URL[]{ toolsURL } );
	Class clazz = cl.loadClass( "com.sun.tools.javac.Main" );
	Method m = clazz.getDeclaredMethod( "compile", new Class[]{ String[].class } );

	//long start = System.currentTimeMillis();
	Object result = m.invoke( null, new Object[]{ args } );
	//System.out.println( System.currentTimeMillis() - start );

	return ( ( Integer )result ).intValue();
	}
catch( Exception e )
	{
	throw new MCompilationException( e.getMessage() );
	}
}
// --------------------------------------------------------------------------------
}