package com.liferay.cdi.portlet.bridge;

import static com.liferay.cdi.portlet.bridge.CDIPortletSessionContext.handleCDIPortletSessionBeanRemoved;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

@WebListener
public class CDIPortletSessionBeanDestroyListener implements HttpSessionAttributeListener {
    
    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {}

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        handleCDIPortletSessionBeanRemoved(event);
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        handleCDIPortletSessionBeanRemoved(event);
    }
}