package org.grails.views.json;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import grails.plugin.json.view.JsonViewConfiguration;
import grails.plugin.json.view.JsonViewTemplateEngine;
import grails.plugin.json.view.api.jsonapi.DefaultJsonApiIdRenderer;
import grails.plugin.json.view.mvc.JsonViewResolver;
import grails.views.mvc.GenericGroovyTemplateViewResolver;
import grails.views.resolve.PluginAwareTemplateResolver;

@AutoConfiguration
public class JsonViewsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DefaultJsonApiIdRenderer jsonApiIdRenderStrategy() {
        return new DefaultJsonApiIdRenderer();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonViewConfiguration jsonViewConfiguration() {
        return new JsonViewConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonViewTemplateEngine jsonTemplateEngine(JsonViewConfiguration jsonViewConfiguration) {
        return new JsonViewTemplateEngine(jsonViewConfiguration, Thread.currentThread().getContextClassLoader());
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonViewResolver jsonSmartViewResolver(JsonViewConfiguration jsonViewConfiguration,
            JsonViewTemplateEngine jsonTemplateEngine) {
        PluginAwareTemplateResolver pluginAwareTemplateResolver = new PluginAwareTemplateResolver(jsonViewConfiguration);
        JsonViewResolver smartViewResolver = new JsonViewResolver(jsonTemplateEngine);
        smartViewResolver.setTemplateResolver(pluginAwareTemplateResolver);
        return smartViewResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public GenericGroovyTemplateViewResolver jsonViewResolver(JsonViewResolver jsonSmartViewResolver) {
        return new GenericGroovyTemplateViewResolver(jsonSmartViewResolver);
    }

}
