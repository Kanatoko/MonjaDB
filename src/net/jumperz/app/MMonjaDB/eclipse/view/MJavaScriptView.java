/*
 * Created on Mar 13, 2012
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.jumperz.app.MMonjaDB.eclipse.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDB.eclipse.dialog.MActionDialog;
import net.jumperz.app.MMonjaDB.eclipse.dialog.MPromptDialog;
import net.jumperz.app.MMonjaDBCore.MInputView;
import net.jumperz.app.MMonjaDBCore.MOutputView;
import net.jumperz.app.MMonjaDBCore.action.mj.*;
import net.jumperz.app.MMonjaDBCore.event.MEvent;
import net.jumperz.app.MMonjaDBCore.event.MEventManager;
import net.jumperz.mongo.MMongoUtil;
import net.jumperz.util.MSystemUtil;

import java.util.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;
import com.mongodb.*;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;

public class MJavaScriptView
extends MAbstractView
implements MOutputView, MInputView
{
private Text text1, text2;
private Object _id;
private Action executeAction;
private Action clearAction;
private SashForm sashForm;
private long initializedTime;
//--------------------------------------------------------------------------------
public MJavaScriptView()
{
eventManager.register2( this );
}
//--------------------------------------------------------------------------------
public void init2()
{
parent.setLayout( new FormLayout() );

sashForm = new SashForm( parent, SWT.SMOOTH | SWT.VERTICAL);

FormData fd_sashForm1 = new FormData();
fd_sashForm1.top = new FormAttachment( 0, 1 );
fd_sashForm1.left = new FormAttachment( 0, 1 );
fd_sashForm1.right = new FormAttachment( 100, -1 );
fd_sashForm1.bottom = new FormAttachment( 100, -1 );
sashForm.setLayoutData(fd_sashForm1);

text1 = new Text( sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI );
text1.addControlListener(new ControlAdapter() {
	public void controlResized(ControlEvent e) {
	onSashResize();
	}
});
text1.addModifyListener(new ModifyListener() {
	public void modifyText(ModifyEvent e) {
	executeAction.setEnabled( dataManager.isConnected() && text1.getText().length() > 0 );	
	}
});
FormData fd_text = new FormData();
fd_text.top = new FormAttachment(0, 1);
fd_text.left = new FormAttachment(0, 1);
fd_text.bottom = new FormAttachment(100, -1);
fd_text.right = new FormAttachment(100, -1);
text1.setLayoutData(fd_text);

text2 = new Text( sashForm, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI );
FormData fd_text2 = new FormData();
fd_text2.top = new FormAttachment(0, 1);
fd_text2.left = new FormAttachment(0, 1);
fd_text2.bottom = new FormAttachment(100, -1);
fd_text2.right = new FormAttachment(100, -1);
text2.setLayoutData(fd_text2);

executeAction = new Action(){ public void run(){//-----------
onExecute();
}};//-----------
executeAction.setToolTipText( "Execute JavaScript 'eval()' on MongoDB Server" );
executeAction.setText( "Execute" );
initAction( executeAction, "database_go.png", null );
executeAction.setEnabled( false );

clearAction = new Action(){ public void run(){//-----------
text1.setText( "" );
text2.setText( "" );
}};//-----------
clearAction.setToolTipText( "Clear" );
clearAction.setText( "Clear" );
initAction( clearAction, "bullet_delete.png", null );
clearAction.setEnabled( true );

if( prop.containsKey( JAVASCRIPT_COMPOSITE_WEIGHT ) )
	{
	
	( new Thread(){ public void run() {
		MSystemUtil.sleep( 0 );
		shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//----
		sashForm.setWeights( prop.getIntArrayProperty( JAVASCRIPT_COMPOSITE_WEIGHT ) );
		}});//----
	}} ).start();

	}
else
	{
	sashForm.setWeights( new int[]{ 70, 30 } );
	}
initializedTime = System.currentTimeMillis();
}
//--------------------------------------------------------------------------------
private void onSashResize()
{
if( System.currentTimeMillis() >= initializedTime + 3000 )
	{
	prop.setProperty( JAVASCRIPT_COMPOSITE_WEIGHT, sashForm.getWeights() );
	}
}
//--------------------------------------------------------------------------------
private void onExecute()
{
Object o = dataManager.getDB().eval( text1.getText(), null );
if( o != null && o instanceof BasicDBObject )
	{
	text2.setText( MMongoUtil.toJson( dataManager.getDB(), o ) );
	}
else
	{
	text2.setText( o + "" );
	}
}
//--------------------------------------------------------------------------------
public void dispose()
{
eventManager.removeObserver2( this );
super.dispose();
}
//--------------------------------------------------------------------------------
public void setFocus()
{
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
}
//--------------------------------------------------------------------------------
}
