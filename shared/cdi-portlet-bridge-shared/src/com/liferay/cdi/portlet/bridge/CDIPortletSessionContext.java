package com.liferay.cdi.portlet.bridge;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map.Entry;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.portlet.PortletSession;

import com.liferay.cdi.portlet.bridge.context.PortletSessionScoped;

/**
 * A simple {@link Context} implementation that stores CDI beans within the {@link PortletSession}.
 * 
 * @author Michael Scholz
 */
public class CDIPortletSessionContext implements Context, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String ATTRIBUTE_PREFIX = CDIPortletSessionContext.class.getName();

    @Override
    public Class<? extends Annotation> getScope() {
        return PortletSessionScoped.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
        if(!(contextual instanceof Bean<?>)) {
            return null;
        }

        final String attributeName = getAttributeName(contextual);

        final PortletSession portletSession = PortletRequestContainer.getCurrentPortletRequest().getPortletSession(true);

        CDIPortletSessionBean<T> beanEntry = (CDIPortletSessionBean<T>) portletSession.getAttribute(attributeName);
        if(beanEntry == null) {
            T instance = contextual.create(creationalContext);
            
            beanEntry = new CDIPortletSessionBean<T>(contextual, creationalContext, instance);
            portletSession.setAttribute(attributeName, beanEntry);
        }

        return beanEntry.instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(final Contextual<T> contextual) {
        if(!(contextual instanceof Bean<?>)) {
            return null;
        }

        final PortletSession portletSession = PortletRequestContainer.getCurrentPortletRequest().getPortletSession();
        if(portletSession == null) {
            return null;
        }
        
        CDIPortletSessionBean<T> beanEntry = (CDIPortletSessionBean<T>)portletSession.getAttribute(getAttributeName(contextual));
        
        return beanEntry != null ? beanEntry.instance : null;
    }

    @Override
    public boolean isActive() {
        return PortletRequestContainer.getCurrentPortletRequest() != null;
    }
    
    static void destroyPortletSessionBeans(PortletSession portletSession) {
        for(Entry<String, Object> entry : portletSession.getAttributeMap().entrySet()) {
            String attributeName = entry.getKey();
            if(attributeName != null && attributeName.startsWith(ATTRIBUTE_PREFIX + "$")) {
                CDIPortletSessionBean<?> beanEntry = (CDIPortletSessionBean<?>) entry.getValue();
                
                beanEntry.destroyBean();
            }
        }
    }

    private static <T> String getAttributeName(final Contextual<T> contextual) {
        final Bean<T> bean = (Bean<T>) contextual;
        final String beanClassName = bean.getBeanClass().getName();

        return ATTRIBUTE_PREFIX + "$" + beanClassName;
    }
    
    private static class CDIPortletSessionBean<T> implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Contextual<T> bean;
        public final CreationalContext<T> creationalContext;
        public final T instance;
        
        private CDIPortletSessionBean(Contextual<T> bean, CreationalContext<T> creationalContext, T instance) {
            this.bean = bean;
            this.creationalContext = creationalContext;
            this.instance = instance;
        }
        
        public void destroyBean() {
            bean.destroy(instance, creationalContext);
        }
    }
}
