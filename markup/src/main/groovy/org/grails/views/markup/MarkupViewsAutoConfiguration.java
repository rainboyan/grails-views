package org.grails.views.markup;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import grails.plugin.markup.view.MarkupViewConfiguration;
import grails.plugin.markup.view.MarkupViewTemplateEngine;
import grails.plugin.markup.view.mvc.MarkupViewResolver;
import grails.views.mvc.GenericGroovyTemplateViewResolver;
import grails.views.resolve.PluginAwareTemplateResolver;

@AutoConfiguration
public class MarkupViewsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MarkupViewConfiguration markupViewConfiguration() {
        return new MarkupViewConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public MarkupViewTemplateEngine markupTemplateEngine(MarkupViewConfiguration componentViewConfiguration) {
        return new MarkupViewTemplateEngine(componentViewConfiguration, Thread.currentThread().getContextClassLoader());
    }

    @Bean
    @ConditionalOnMissingBean
    public MarkupViewResolver smartMarkupViewResolver(MarkupViewConfiguration componentViewConfiguration,
            MarkupViewTemplateEngine markupViewTemplateEngine) {
        PluginAwareTemplateResolver pluginAwareTemplateResolver = new PluginAwareTemplateResolver(componentViewConfiguration);
        MarkupViewResolver markupViewResolver = new MarkupViewResolver(markupViewTemplateEngine);
        markupViewResolver.setTemplateResolver(pluginAwareTemplateResolver);
        return markupViewResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public GenericGroovyTemplateViewResolver markupViewResolver(MarkupViewResolver smartMarkupViewResolver) {
        return new GenericGroovyTemplateViewResolver(smartMarkupViewResolver);
    }

}
