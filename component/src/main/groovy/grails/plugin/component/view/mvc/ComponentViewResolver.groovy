package grails.plugin.component.view.mvc

import groovy.transform.CompileStatic
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired

import grails.core.support.proxy.ProxyHandler
import grails.plugin.component.view.ComponentViewConfiguration
import grails.plugin.component.view.ComponentViewTemplate
import grails.plugin.component.view.ComponentViewTemplateEngine
import grails.plugin.component.view.renderer.ComponentViewHtmlRenderer
import grails.rest.render.RendererRegistry
import grails.views.mvc.SmartViewResolver
import grails.web.mime.MimeType

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class ComponentViewResolver extends SmartViewResolver implements InitializingBean {

    public static final String COMPONENT_VIEW_SUFFIX = ".${ComponentViewTemplate.EXTENSION}"


    @Autowired(required = false)
    ProxyHandler proxyHandler

    @Autowired(required = false)
    RendererRegistry rendererRegistry

    ComponentViewConfiguration viewConfiguration

    ComponentViewResolver(ComponentViewConfiguration configuration) {
        this(new ComponentViewTemplateEngine(configuration), ".$configuration.extension", MimeType.HTML.name)
    }

    ComponentViewResolver(ComponentViewTemplateEngine templateEngine) {
        this(templateEngine, COMPONENT_VIEW_SUFFIX, MimeType.HTML.name)
    }

    ComponentViewResolver(ComponentViewTemplateEngine templateEngine, String suffix, String contentType) {
        super(templateEngine, suffix, contentType)
        viewConfiguration = (ComponentViewConfiguration)templateEngine.viewConfiguration
    }

    @Override
    void afterPropertiesSet() {
        if(rendererRegistry != null) {
            def defaultHtmlRenderer = rendererRegistry.findRenderer(MimeType.HTML, Object.class)
            viewConfiguration.mimeTypes.each { String mimeTypeString ->
                MimeType mimeType = new MimeType(mimeTypeString, "html")
                rendererRegistry.addDefaultRenderer(
                    new ComponentViewHtmlRenderer<Object>(Object.class, mimeType, this , proxyHandler, rendererRegistry, defaultHtmlRenderer)
                )
            }
        }
    }

}
