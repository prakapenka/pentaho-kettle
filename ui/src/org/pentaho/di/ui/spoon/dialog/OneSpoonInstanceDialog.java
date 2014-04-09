package org.pentaho.di.ui.spoon.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;

public class OneSpoonInstanceDialog extends Dialog {

  private static Class<?> PKG = OneSpoonInstanceDialog.class;
  private static Class<?> PKG2 = BaseMessages.class;

  private String title, message1, message2, message3, message4;
  private Shell parent;
  private PropsUI props = PropsUI.getInstance();
  private Display display;

  // since we may implement separate workspaces in future milestone, see PDI-3748
  private boolean I_WILL_NOT_START_ANOTHER_INSTANCE_AND_WILL_WAIT_FOR_SEPARATE_WORKSPACES_IMPLEMENTATION = true;

  public OneSpoonInstanceDialog( Shell parent ) {
    super( parent, SWT.NONE );
    title = BaseMessages.getString( PKG, "OneSpoonInstanceDialog.Dialog.Title" );
    message1 = BaseMessages.getString( PKG, "OneSpoonInstanceDialog.Dialog.Message1" );
    message2 = BaseMessages.getString( PKG, "OneSpoonInstanceDialog.Dialog.Message2" );
    message3 = BaseMessages.getString( PKG, "OneSpoonInstanceDialog.Dialog.Message3" );
    message4 = BaseMessages.getString( PKG, "OneSpoonInstanceDialog.Dialog.Message4" );
    // assume there is only one
    this.parent = parent;
  }

  public boolean isBreakExecution() {
    return I_WILL_NOT_START_ANOTHER_INSTANCE_AND_WILL_WAIT_FOR_SEPARATE_WORKSPACES_IMPLEMENTATION;
  }

  public String open() {
    Display display = parent.getDisplay();
    Shell shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.NONE );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 10;
    formLayout.marginHeight = 5;
    shell.setLayout( formLayout );
    shell.setText( title );

    StyledText mainCause = new StyledText( shell, SWT.MULTI | SWT.READ_ONLY );
    mainCause.setText( message1 );
    props.setLook( mainCause );
    FormData fd = new FormData();
    fd.top = new FormAttachment( 5, Const.MARGIN );
    mainCause.setLayoutData( fd );

    StyledText description = new StyledText( shell, SWT.MULTI | SWT.READ_ONLY );
    description.setText( message2 );
    props.setLook( description );
    fd = new FormData();
    fd.top = new FormAttachment( mainCause, 10 );
    description.setLayoutData( fd );

    StyledText ourOpinion = new StyledText( shell, SWT.MULTI | SWT.READ_ONLY );
    ourOpinion.setText( message3 );
    props.setLook( ourOpinion );
    StyleRange style = new StyleRange();
    style.start = 0;
    style.length = ourOpinion.getText().length();
    style.fontStyle = SWT.BOLD;
    ourOpinion.setStyleRange( style );
    fd = new FormData();
    fd.top = new FormAttachment( description, 10 );
    ourOpinion.setLayoutData( fd );

    StyledText question = new StyledText( shell, SWT.MULTI | SWT.READ_ONLY );
    question.setText( message4 );
    props.setLook( ourOpinion );
    fd = new FormData();
    fd.top = new FormAttachment( ourOpinion, 10 );
    question.setLayoutData( fd );

    populateButtons( shell, question );

    WindowProperty winprop = props.getScreen( shell.getText() );
    if ( winprop != null ) {
      winprop.setShell( shell );
    } else {
      Point p = getMax( mainCause.getText() );
      shell.setSize( p.x + 100, p.y + 150 );
    }

    shell.pack();
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return "hello world";
  }

  private void populateButtons( Shell shell, Composite top ) {
    Button wOK, wNo;
    FormData fdYes, fdNo;
    Listener lsYes, lsNo;
    int width = 0;
    int margin = Const.MARGIN;

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG2, "System.Button.Yes" ) );
    wOK.pack( true );
    Rectangle rOK = wOK.getBounds();

    wNo = new Button( shell, SWT.PUSH );
    wNo.setText( BaseMessages.getString( PKG2, "System.Button.No" ) );
    wNo.pack( true );
    Rectangle rNext = wNo.getBounds();

    width = ( rOK.width > rNext.width ? rOK.width : rNext.width );
    width += margin;

    fdYes = new FormData();
    fdYes.left = new FormAttachment( 50, -width );
    fdYes.right = new FormAttachment( 50, -( margin / 2 ) );
    fdYes.bottom = new FormAttachment( 100, 0 );
    fdYes.top = new FormAttachment( top, 20 );
    wOK.setLayoutData( fdYes );
    fdNo = new FormData();
    fdNo.left = new FormAttachment( 50, margin / 2 );
    fdNo.right = new FormAttachment( 50, width );
    fdNo.bottom = new FormAttachment( 100, 0 );
    fdNo.top = new FormAttachment( top, 20 );
    wNo.setLayoutData( fdNo );

    // Add listeners
    lsNo = new Listener() {
      public void handleEvent( Event e ) {
        I_WILL_NOT_START_ANOTHER_INSTANCE_AND_WILL_WAIT_FOR_SEPARATE_WORKSPACES_IMPLEMENTATION = true;
        close();
      }
    };
    lsYes = new Listener() {
      public void handleEvent( Event e ) {
        I_WILL_NOT_START_ANOTHER_INSTANCE_AND_WILL_WAIT_FOR_SEPARATE_WORKSPACES_IMPLEMENTATION = false;
        close();
      }
    };
    wOK.addListener( SWT.Selection, lsYes );
    wNo.addListener( SWT.Selection, lsNo );
  }

  private Point getMax( String str ) {
    Image img = new Image( display, 1, 1 );
    GC gc = new GC( img );
    Point p = gc.textExtent( str, SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
    gc.dispose();
    img.dispose();
    return p;
  }

  private void close() {
    parent.dispose();
  }
}
