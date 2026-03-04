package com.rhsoft.ioc;

import java.util.Arrays;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class BasicModule extends AbstractModule {

    @Override
    protected void configure() {

        System.getenv().entrySet().stream().forEach(entry -> {
             bind(String.class)
                .annotatedWith(Names.named("env." + entry.getKey()))
                .toInstance(entry.getValue());
        });

        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(final TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
                typeEncounter.register(new InjectionListener<I>() {
                    @Override
                    public void afterInjection(Object o) {
                        Arrays.stream(o.getClass().getMethods())
                                .filter(m -> m.getDeclaredAnnotationsByType(AfterInjection.class).length > 0)
                                .forEach(m -> {
                                    try {
                                        m.setAccessible(true);
                                        m.invoke(o);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                });
            }
        });
    }

}