package com.rhsoft.ioc;

import com.google.inject.Guice;
import com.microsoft.azure.functions.spi.inject.FunctionInstanceInjector;

public class FunctionGuiceFactory implements FunctionInstanceInjector {
    @Override
    public <T> T getInstance(Class<T> functionClass) throws Exception {
        return Guice.createInjector(new BasicModule()).getInstance(functionClass);
    }
}
