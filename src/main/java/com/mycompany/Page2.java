/*
 * This is free and unencumbered software released into the public domain.
 */
package com.mycompany;


import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;


/**
 * Example Page class for Fortressdemo1 Wicket sample project.  It contains security control functions to demonstrate ANSI RBAC security concepts.
 *
 * @author Shawn McKinney
 * @version $Rev$
 */
public class Page2 extends MyBasePage
{
    private static final Logger LOG = Logger.getLogger( Page2.class.getName() );

    public Page2()
    {
        add( new Page2Form( "pageForm" ) );
        setChildPage( ChildPage.PAGE2 );
    }

    public class Page2Form extends Form
    {
        private static final String PAGE2 = "com.mycompany.Page2";

        public Page2Form( String id )
        {
            super( id );

            add( new Label( "label2", "This is Page2.  If you see it, ROLE_TEST_SUPER or ROLE_TEST2 is activated within " +
                "" + "your session" ) );
            final String szBtn1 = GlobalUtils.BTN_PAGE_2 + "." + GlobalUtils.BUTTON1;
            add( new SecureIndicatingAjaxButton( this, szBtn1, PAGE2, GlobalUtils.BUTTON1 )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form form )
                {
                    info( szBtn1 );
                    if(checkAccess())
                        target.appendJavaScript(";alert('" + szBtn1 + "');");
                }

                @Override
                public void onError( AjaxRequestTarget target, Form form )
                {
                    LOG.error( "submit failed: " + szBtn1 );
                }

                @Override
                protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                {
                    super.updateAjaxAttributes( attributes );
                    AjaxCallListener ajaxCallListener = new AjaxCallListener()
                    {
                        @Override
                        public CharSequence getFailureHandler( Component component )
                        {
                            return GlobalUtils.WINDOW_LOCATION_REPLACE_DEMO_HOME_HTML;
                        }
                    };
                    attributes.getAjaxCallListeners().add( ajaxCallListener );
                }
            } );
            final String szBtn2 = GlobalUtils.BTN_PAGE_2 + "." + GlobalUtils.BUTTON2;
            add( new SecureIndicatingAjaxButton( this, szBtn2, PAGE2, GlobalUtils.BUTTON2 )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form form )
                {
                    info( szBtn2 );
                    if(checkAccess())
                        target.appendJavaScript(";alert('" + szBtn2 + "');");
                }

                @Override
                public void onError( AjaxRequestTarget target, Form form )
                {
                    LOG.error( "submit failed: " + szBtn2 );
                }

                @Override
                protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                {
                    super.updateAjaxAttributes( attributes );
                    AjaxCallListener ajaxCallListener = new AjaxCallListener()
                    {
                        @Override
                        public CharSequence getFailureHandler( Component component )
                        {
                            return GlobalUtils.WINDOW_LOCATION_REPLACE_DEMO_HOME_HTML;
                        }
                    };
                    attributes.getAjaxCallListeners().add( ajaxCallListener );
                }
            } );
            final String szBtn3 = GlobalUtils.BTN_PAGE_2+ "." + GlobalUtils.BUTTON3;
            add( new SecureIndicatingAjaxButton( this, szBtn3, PAGE2, GlobalUtils.BUTTON3 )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form form )
                {
                    info( szBtn3 );
                    if(checkAccess())
                        target.appendJavaScript(";alert('" + szBtn3 + "');");
                }

                @Override
                public void onError( AjaxRequestTarget target, Form form )
                {
                    LOG.error( "submit failed: " + szBtn3 );
                }

                @Override
                protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                {
                    super.updateAjaxAttributes( attributes );
                    AjaxCallListener ajaxCallListener = new AjaxCallListener()
                    {
                        @Override
                        public CharSequence getFailureHandler( Component component )
                        {
                            return GlobalUtils.WINDOW_LOCATION_REPLACE_DEMO_HOME_HTML;
                        }
                    };
                    attributes.getAjaxCallListeners().add( ajaxCallListener );
                }
            } );
        }
    }
}
