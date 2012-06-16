package net.jumperz.gui;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import org.eclipse.swt.widgets.*;
import net.jumperz.util.*;

public class MSwtUtil
{
//--------------------------------------------------------------------------------
public static void removeMenuItems( Menu menu )
{
MenuItem[] items = menu.getItems();
for( int i = 0; i < items.length; ++i )
	{
	items[ i ].dispose();
	}
}
// --------------------------------------------------------------------------------
public static void addListenerToMenuItems( Menu menu, Listener listener )
{
MenuItem[] itemArray = menu.getItems();
for( int i = 0; i < itemArray.length; ++i )
	{
	itemArray[ i ].addListener( SWT.Selection, listener );
	}
}
//--------------------------------------------------------------------------------
public static void setTreeColumnWidthToProperties( String prefix, Tree tree, MProperties prop )
{
TreeColumn[] columns = tree.getColumns();
for( int i = 0; i < columns.length -1; ++i )
	{
	prop.setProperty( prefix + ".treeColumnWidth" + Integer.toString( i ), columns[ i ].getWidth() );
	}
}
// --------------------------------------------------------------------------------
public static void getTreeColumnWidthFromProperties( String prefix, Tree tree, MProperties prop, int[] defaultWidth )
{
TreeColumn[] columns = tree.getColumns();
if( columns.length > defaultWidth.length )
	{
	getTreeColumnWidthFromProperties(  prefix, tree, prop, 20 );
	}
else
	{
	for( int i = 0; i < columns.length; ++i )
		{
		columns[ i ].setWidth( prop.getIntProperty( prefix + ".treeColumnWidth" + Integer.toString( i ), defaultWidth[ i ] ) );
		}
	}
}
//--------------------------------------------------------------------------------
public static void getTreeColumnWidthFromProperties( String prefix, Tree tree, MProperties prop, int defaultWidth )
{
try
	{
	TreeColumn[] columns = tree.getColumns();
	for( int i = 0; i < columns.length; ++i )
		{
		columns[ i ].setWidth( prop.getIntProperty( prefix + ".treeColumnWidth" + Integer.toString( i ), defaultWidth ) );
		}
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}

// --------------------------------------------------------------------------------
public static void setTableColumnWidthToProperties( String prefix, Table table, MProperties prop )
{
TableColumn[] columns = table.getColumns();
for( int i = 0; i < columns.length -1; ++i )
	{
	prop.setProperty( prefix + ".columnWidth" + Integer.toString( i ), columns[ i ].getWidth() );
	}
}
//--------------------------------------------------------------------------------
public static void getTableColumnWidthFromProperties2( String prefix, Table table, MProperties prop )
{
TableColumn[] columns = table.getColumns();
if( columns.length > 0 )
	{
	getTableColumnWidthFromProperties( prefix, table, prop, table.getBounds().width / columns.length );
	}
else
	{
	getTableColumnWidthFromProperties( prefix, table, prop, 20 );
	}
}
// --------------------------------------------------------------------------------
public static void getTableColumnWidthFromProperties( String prefix, Table table, MProperties prop )
{
getTableColumnWidthFromProperties( prefix, table, prop, 20 );
}
// --------------------------------------------------------------------------------
public static void getTableColumnWidthFromProperties( String prefix, Table table, MProperties prop, int[] defaultWidth )
{
TableColumn[] columns = table.getColumns();
if( columns.length > defaultWidth.length )
	{
	getTableColumnWidthFromProperties(  prefix, table, prop, 20 );
	}
else
	{
	for( int i = 0; i < columns.length; ++i )
		{
		columns[ i ].setWidth( prop.getIntProperty( prefix + ".columnWidth" + Integer.toString( i ), defaultWidth[ i ] ) );
		}
	}
}
//--------------------------------------------------------------------------------
public static void getTableColumnWidthFromProperties( String prefix, Table table, MProperties prop, int defaultWidth )
{
try
	{
	TableColumn[] columns = table.getColumns();
	for( int i = 0; i < columns.length; ++i )
		{
		columns[ i ].setWidth( prop.getIntProperty( prefix + ".columnWidth" + Integer.toString( i ), defaultWidth ) );
		}
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
// --------------------------------------------------------------------------------
public static void addListenerToTreeColumns2( Tree tree, Listener listener )
{
TreeColumn[] columns = tree.getColumns();
for( int i = 0; i < columns.length; ++i )
	{
	columns[ i ].addListener( SWT.Resize, listener );
	columns[ i ].addListener( SWT.Selection, listener );
	}
}
// --------------------------------------------------------------------------------
public static void addListenerToTableColumns2( Table table, Listener listener )
{
TableColumn[] columns = table.getColumns();
for( int i = 0; i < columns.length; ++i )
	{
	columns[ i ].addListener( SWT.Resize, listener );
	columns[ i ].addListener( SWT.Selection, listener );
	}
}
// --------------------------------------------------------------------------------
public static void addListenerToTableColumns( Table table, Listener listener )
{
TableColumn[] columns = table.getColumns();
for( int i = 0; i < columns.length; ++i )
	{
	columns[ i ].addListener( SWT.Resize, listener );
	}
}
// --------------------------------------------------------------------------------
public static java.util.List getTreeColumns( Tree tree )
{
return Arrays.asList( tree.getColumns() );
}
// --------------------------------------------------------------------------------
public static java.util.List getTableColumns( Table table )
{
return Arrays.asList( table.getColumns() );
}
// --------------------------------------------------------------------------------
public static void copyToClipboard( String s )
{
Display display = Display.findDisplay( Thread.currentThread() );
Clipboard clipboard = new Clipboard( display );
TextTransfer textTransfer = TextTransfer.getInstance();
clipboard.setContents(new Object[]{ s }, new Transfer[]{ textTransfer } );
clipboard.dispose();
/*
 	Clipboard clipboard = new Clipboard(display);
		String textData = "Hello World";
		String rtfData = "{\\rtf1\\b\\i Hello World}";
		TextTransfer textTransfer = TextTransfer.getInstance();
		RTFTransfer rtfTransfer = RTFTransfer.getInstance();
		clipboard.setContents(new Object[]{textData, rtfData}, new Transfer[]{textTransfer, rtfTransfer});
		clipboard.dispose();
*/
}
// --------------------------------------------------------------------------------
}