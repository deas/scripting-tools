/*
 * Andreas Steffan - http://www.contentreich.de
 *
 * Created on Sep 30, 2011
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.contentreich.scripting;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.Resource;


public class ApplicationContextWrapper implements ApplicationContextAware, ApplicationContext {
    private ApplicationContext context;
    
	@Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

	@Override
    public boolean containsBean(String arg0) {
        return context.containsBean(arg0);
    }

	@Override
    public boolean containsBeanDefinition(String arg0) {
        return context.containsBeanDefinition(arg0);
    }

	@Override
    public String[] getAliases(String arg0) throws NoSuchBeanDefinitionException {
        return context.getAliases(arg0);
    }
    
	@Override
    public Object getBean(String arg0) throws BeansException {
        return context.getBean(arg0);
    }

	@Override
    public int getBeanDefinitionCount() {
        return context.getBeanDefinitionCount();
    }

	@Override
    public String[] getBeanDefinitionNames() {
        return context.getBeanDefinitionNames();
    }

	@Override
    public String[] getBeanNamesForType(Class arg0, boolean arg1, boolean arg2) {
        return context.getBeanNamesForType(arg0, arg1, arg2);
    }

	@Override
    public String[] getBeanNamesForType(Class arg0) {
        return context.getBeanNamesForType(arg0);
    }

	@Override
    public String getDisplayName() {
        return context.getDisplayName();
    }

	@Override
    public String getMessage(MessageSourceResolvable arg0, Locale arg1) throws NoSuchMessageException {
        return context.getMessage(arg0, arg1);
    }

	@Override
    public String getMessage(String arg0, Object[] arg1, Locale arg2) throws NoSuchMessageException {
        return context.getMessage(arg0, arg1, arg2);
    }

    public String getMessage(String arg0, Object[] arg1, String arg2, Locale arg3) {
        return context.getMessage(arg0, arg1, arg2, arg3);
    }

	@Override
    public ApplicationContext getParent() {
        return context.getParent();
    }

	@Override
    public BeanFactory getParentBeanFactory() {
        return context.getParentBeanFactory();
    }

	@Override
    public Resource getResource(String arg0) {
        return context.getResource(arg0);
    }

	@Override
    public Resource[] getResources(String arg0) throws IOException {
        return context.getResources(arg0);
    }

	@Override
    public long getStartupDate() {
        return context.getStartupDate();
    }

	@Override
    public Class getType(String arg0) throws NoSuchBeanDefinitionException {
        return context.getType(arg0);
    }

	@Override
    public boolean isSingleton(String arg0) throws NoSuchBeanDefinitionException {
        return context.isSingleton(arg0);
    }

	@Override
    public void publishEvent(ApplicationEvent arg0) {
        context.publishEvent(arg0);
    }

	@Override
	public String getId() {
		return context.getId();
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory()
			throws IllegalStateException {
		return context.getAutowireCapableBeanFactory();
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type)
			throws BeansException {
		return context.getBeansOfType(type);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type,
			boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {
		return context.getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(
			Class<? extends Annotation> annotationType) throws BeansException {
		return context.getBeansWithAnnotation(annotationType);
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName,
			Class<A> annotationType) {
		return context.findAnnotationOnBean(beanName, annotationType);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType)
			throws BeansException {
		return context.getBean(name, requiredType);
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return context.getBean(requiredType);
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		return context.getBean(name, args);
	}

	@Override
	public boolean isPrototype(String name)
			throws NoSuchBeanDefinitionException {
		return context.isPrototype(name);
	}

	@Override
	public boolean isTypeMatch(String name, Class targetType)
			throws NoSuchBeanDefinitionException {
		return context.isTypeMatch(name, targetType);
	}

	@Override
	public boolean containsLocalBean(String name) {
		return context.containsLocalBean(name);
	}

	@Override
	public ClassLoader getClassLoader() {
		return context.getClassLoader();
	}

	ApplicationContext getApplicationContext() {
		return context;
	}
}
