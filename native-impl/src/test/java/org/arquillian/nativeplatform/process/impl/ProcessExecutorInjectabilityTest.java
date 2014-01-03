/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.nativeplatform.process.impl;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.arquillian.nativeplatform.ArquillianNativePlatformExtension;
import org.arquillian.nativeplatform.process.ProcessExecutor;
import org.arquillian.nativeplatform.process.ProcessExecutorFactory;
import org.arquillian.nativeplatform.process.enricher.ProcessExecutorResourceProvider;
import org.jboss.arquillian.config.descriptor.impl.ArquillianDescriptorImpl;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.impl.TestInstanceEnricher;
import org.jboss.arquillian.test.impl.enricher.resource.ArquillianResourceTestEnricher;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests that {@link ProcessExecutor} in injectable into test class as {@link ArquillianResource}.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessExecutorInjectabilityTest extends AbstractTestTestBase {

    @Mock
    private ServiceLoader serviceLoader;

    @Inject
    Instance<Injector> injector;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ArquillianNativePlatformExtension.class);
        extensions.add(TestInstanceEnricher.class);
        extensions.add(ArquillianResourceTestEnricher.class);
        extensions.add(ProcessExecutorResourceProvider.class);
        extensions.add(ProcessExecutorCreator.class);
    }

    @org.junit.Before
    public void setMocks() {

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        enricher = injector.get().inject(enricher);

        ResourceProvider provider = new ProcessExecutorResourceProvider();
        provider = injector.get().inject(provider);

        Mockito.when(serviceLoader.all(TestEnricher.class)).thenReturn(Arrays.asList(enricher));
        Mockito.when(serviceLoader.all(ResourceProvider.class)).thenReturn(Arrays.asList(provider));

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);

        Mockito.when(serviceLoader.onlyOne(ProcessExecutorFactory.class))
            .thenReturn(new DefaultProcessExecutionFactory());
        Mockito.when(serviceLoader.onlyOne(ProcessExecutorFactory.class, DefaultProcessExecutionFactory.class))
            .thenReturn(new DefaultProcessExecutionFactory());
    }

    @Test
    public void testProcessExecutorIsInjectable() throws Exception {

        fire(new ArquillianDescriptorImpl("arquillian.xml"));

        ProcessExecutorInjectTest instance = new ProcessExecutorInjectTest();

        getManager().getContext(ClassContext.class).activate(ProcessExecutorInjectTest.class);
        Method method = ProcessExecutorInjectTest.class.getMethod("testMe");
        getManager().getContext(TestContext.class).activate(instance);

        fire(new BeforeSuite());
        fire(new BeforeClass(ProcessExecutorInjectTest.class));
        fire(new Before(instance, method));

        assertThat(instance.getExecutor(), notNullValue());

        fire(new After(instance, method));
        fire(new AfterClass(ProcessExecutorInjectTest.class));
        fire(new AfterSuite());
    }

    private static class ProcessExecutorInjectTest {

        @ArquillianResource
        ProcessExecutor executor;

        @Test
        public void testMe() {

        }

        public ProcessExecutor getExecutor() {
            return executor;
        }
    }
}
