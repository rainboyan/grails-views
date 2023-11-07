package org.grails.views.component;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import grails.plugin.component.view.ComponentViewConfiguration;
import grails.plugin.component.view.ComponentViewTemplateEngine;
import grails.plugin.component.view.mvc.ComponentViewResolver;
import grails.views.mvc.GenericGroovyTemplateViewResolver;
import grails.views.resolve.PluginAwareTemplateResolver;

@AutoConfiguration
public class ComponentViewsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ComponentViewConfiguration componentViewConfiguration() {
        return new ComponentViewConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public ComponentViewTemplateEngine componentTemplateEngine(ComponentViewConfiguration componentViewConfiguration) {
        return new ComponentViewTemplateEngine(componentViewConfiguration, Thread.currentThread().getContextClassLoader());
    }

    @Bean
    @ConditionalOnMissingBean
    public ComponentViewResolver smartComponentViewResolver(ComponentViewConfiguration componentViewConfiguration,
            ComponentViewTemplateEngine componentViewTemplateEngine) {
        PluginAwareTemplateResolver pluginAwareTemplateResolver = new PluginAwareTemplateResolver(componentViewConfiguration);
        ComponentViewResolver componentViewResolver = new ComponentViewResolver(componentViewTemplateEngine);
        componentViewResolver.setTemplateResolver(pluginAwareTemplateResolver);
        return componentViewResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public GenericGroovyTemplateViewResolver componentViewResolver(ComponentViewResolver smartComponentViewResolver) {
        return new GenericGroovyTemplateViewResolver(smartComponentViewResolver);
    }

}
