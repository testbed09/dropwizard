package io.dropwizard.sslreload;

import io.dropwizard.Bundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.SslReload;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Bundle that gathers all the ssl connectors and registers an admin task that will
 *  refresh ssl configuration on request */
public class SslReloadBundle implements Bundle {
    private static final Logger LOGGER = LoggerFactory.getLogger(SslReloadBundle.class);

    private final SslReloadTask reloadTask = new SslReloadTask();

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Environment environment) {
        environment.getApplicationContext().addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                final Set<SslReload> reloaders = new HashSet<>();
                reloaders.addAll(getReloaders(environment.getApplicationContext()));
                reloaders.addAll(getReloaders(environment.getAdminContext()));

                LOGGER.info("{} ssl reloaders registered", reloaders.size());
                reloadTask.setReloaders(reloaders);
            }
        });

        environment.admin().addTask(reloadTask);
    }

    private Collection<SslReload> getReloaders(MutableServletContextHandler handler) {
        return handler.getServer().getBeans(SslReload.class);
    }
}

