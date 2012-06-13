package net.jumperz.util;

import net.jumperz.ext.org.jrobin.core.*;
import net.jumperz.ext.org.jrobin.graph.*;
import java.io.*;
import java.awt.Color;
import java.awt.Font;

public class MRrdUtil
{
private static final Color c000000 = new Color( 0x00, 0x00, 0x00 );
private static final Color c222222 = new Color( 0x22, 0x22, 0x22 );
private static final Color c00FF00 = new Color( 0x00, 0xFF, 0x00 );
private static final Color c113311 = new Color( 0x11, 0x33, 0x11 );
private static final Color c777777 = new Color( 0x77, 0x77, 0x77 );
private static final Color c333333 = new Color( 0x33, 0x33, 0x33 );
private static final int IMAGE_HEIGHT = 100;
private static final int IMAGE_WIDTH = 350;
// --------------------------------------------------------------------------------
public static void setColor1( RrdGraphDef gd, String id )
throws IOException, RrdException
{
//gd.area( id, c113311, null );
gd.line( id, c000000, null, 1 );

gd.setTitleFontColor( c00FF00 );
gd.setMinorGridY( true );
//gd.setGridY( true );
gd.setDefaultFontColor( c00FF00 );
gd.setAntiAliasing( false );
gd.setBackColor( c000000 );
gd.setCanvasColor( c000000 );
gd.setMinorGridColor( c000000 );
gd.setMajorGridColor( c000000 );
gd.setShowSignature( false );
gd.setImageBorder( null, 0 );
gd.setAxisColor( c000000 );
gd.setFrameColor( c000000 );
}
// --------------------------------------------------------------------------------
public static void setColor2( RrdGraphDef gd, String id )
throws IOException, RrdException
{
gd.setMinorGridColor( c333333 );
gd.setMajorGridColor( c777777 );
gd.setAxisColor( c333333 );
gd.setFrameColor( c333333 );
}
// --------------------------------------------------------------------------------
public static void setColor3( RrdGraphDef gd, String id )
throws IOException, RrdException
{
//gd.area( id, c113311, null );
gd.line( id, c00FF00, null, 1 );

/*
Font verdana = Font.decode( "Verdana-BOLD-11" );
gd.setTitleFont( verdana );
gd.setTitleFontColor( c00FF00 );
gd.setMinorGridY( true );
gd.setGridY( true );
gd.setDefaultFontColor( c00FF00 );
gd.setAntiAliasing( false );
gd.setBackColor( c000000 );
gd.setCanvasColor( c000000 );
gd.setMinorGridColor( c555555 );
gd.setMajorGridColor( c777777 );
gd.setShowSignature( false );
gd.setImageBorder( null, 0 );
gd.setAxisColor( c555555 );
gd.setFrameColor( c555555 );
*/
}
// --------------------------------------------------------------------------------
public static void setColor4( RrdGraphDef gd, String id )
throws IOException, RrdException
{
gd.area( id, c113311, null );
gd.line( id, c00FF00, null, 1 );
}
//--------------------------------------------------------------------------------
public static RrdDb createStandardRrdFile( String rrdFileName, String dataSourceName )
throws IOException
{
return createStandardRrdFile( "GAUGE", rrdFileName, dataSourceName );
}
// --------------------------------------------------------------------------------
public static RrdDb createStandardRrdFile( String type, String rrdFileName, String dataSourceName )
throws IOException
{
try
	{
	RrdDef rrdDef = new RrdDef( rrdFileName );
	rrdDef.setStartTime( ( System.currentTimeMillis() ) / 1000  ); // now
	rrdDef.addDatasource( dataSourceName, type.toUpperCase(), 600, 0, Double.NaN );
	rrdDef.addArchive( "AVERAGE", 0.5, 1, 600 );
	rrdDef.addArchive( "AVERAGE", 0.5, 6, 700 );
	rrdDef.addArchive( "AVERAGE", 0.5, 24, 775 );
	rrdDef.addArchive( "AVERAGE", 0.5, 288, 797 );
	rrdDef.addArchive( "MAX", 0.5, 1, 600 );
	rrdDef.addArchive( "MAX", 0.5, 6, 700 );
	rrdDef.addArchive( "MAX", 0.5, 24, 775 );
	rrdDef.addArchive( "MAX", 0.5, 288, 797 );
	RrdDb rrdDb = new RrdDb( rrdDef );
	return rrdDb;
	}
catch( RrdException e )
	{
	throw new IOException( e.getMessage() );
	}
}
//--------------------------------------------------------------------------------
}