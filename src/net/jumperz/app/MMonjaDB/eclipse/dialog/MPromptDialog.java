package net.jumperz.app.MMonjaDB.eclipse.dialog;

import net.jumperz.app.MMonjaDB.eclipse.*;
import net.jumperz.app.MMonjaDBCore.MConstants;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.action.MActionManager;
import net.jumperz.util.MProperties;
import net.jumperz.util.MStringUtil;
import java.util.Set;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;

public class MPromptDialog
extends Dialog
implements MConstants
{
private Text text;
private Set dataSet;
private String title;
private String labelText;
private String defaultValue = "";
//--------------------------------------------------------------------------------
public MPromptDialog( Shell parentShell, Set s, String _title, String _labelText )
{
super( parentShell );
title = _title;
labelText = _labelText;
dataSet = s;
}
//--------------------------------------------------------------------------------
public MPromptDialog( Shell parentShell, Set s, String _title, String _labelText, String defaultValue )
{
super( parentShell );
title = _title;
labelText = _labelText;
dataSet = s;
this.defaultValue = defaultValue;
}
//--------------------------------------------------------------------------------
protected void configureShell( Shell newShell )
{
super.configureShell(newShell);
newShell.setText( title );
//Image image = MUtil.getImage( newShell.getDisplay(), "server_lightning.png" );
//newShell.setImage( image );
}
//--------------------------------------------------------------------------------
protected void okPressed()
{
dataSet.add( text.getText() );
setReturnCode(OK);
close();
}
//--------------------------------------------------------------------------------
protected Control createDialogArea( Composite parent )
{
Composite composite = (Composite)super.createDialogArea( parent );
composite.setLayout( new FormLayout() );

Group group1 = new Group( composite, SWT.SHADOW_ETCHED_OUT );
FormData d7 = new FormData();
d7.top = new FormAttachment( 0, 6 );
d7.left = new FormAttachment( 0, 3 );
d7.right = new FormAttachment( 100, -3 );
d7.bottom = new FormAttachment( 100, -3 );
group1.setLayoutData( d7 );
group1.setLayout( new FormLayout() );

Label passwdLabel = new Label( group1, SWT.NONE );
passwdLabel.setText( labelText );
FormData fd_passwdLabel = new FormData();
fd_passwdLabel.top = new FormAttachment( 0, 15 );
fd_passwdLabel.left = new FormAttachment( 0, 8 );
passwdLabel.setLayoutData( fd_passwdLabel );

text = new Text( group1, SWT.BORDER );
FormData d1 = new FormData();
d1.top = new FormAttachment(passwdLabel, -2, SWT.TOP);
d1.left = new FormAttachment( passwdLabel, 10, SWT.RIGHT );
d1.right = new FormAttachment( passwdLabel, 200, SWT.RIGHT );
//d1.bottom = new FormAttachment( 100, -5 );
text.setLayoutData( d1 );
text.setText( defaultValue );

Composite composite_1 = new Composite(group1, SWT.NONE);
composite_1.setLayout(new FormLayout());
FormData fd_composite_1 = new FormData();
fd_composite_1.right = new FormAttachment(text, 20, SWT.RIGHT);
fd_composite_1.bottom = new FormAttachment(0, 50);
fd_composite_1.top = new FormAttachment(0, 3);
fd_composite_1.left = new FormAttachment(text, 14);
composite_1.setLayoutData(fd_composite_1);

return composite;
}
}
