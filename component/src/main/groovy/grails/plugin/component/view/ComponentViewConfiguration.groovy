package grails.plugin.component.view

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.support.GrailsApplicationAware
import grails.util.BuildSettings
import grails.util.Environment
import grails.util.Metadata
import grails.views.GenericViewConfiguration
import grails.views.ViewConfiguration
import grails.views.ViewsEnvironment
import grails.web.mime.MimeType
import groovy.text.markup.TemplateConfiguration
import groovy.transform.CompileStatic
import org.springframework.beans.BeanUtils
import org.springframework.boot.context.properties.ConfigurationProperties

import java.beans.PropertyDescriptor

import org.grails.core.artefact.DomainClassArtefactHandler

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
@ConfigurationProperties('grails.views.component')
class ComponentViewConfiguration extends TemplateConfiguration implements ViewConfiguration, GrailsApplicationAware {

    public static final String MODULE_NAME = "component"

    /**
     * The encoding to use
     */
    String encoding = "UTF-8"

    /**
     * Whether to pretty print
     */
    boolean prettyPrint = false

    /**
     * Whether to use absolute links
     */
    boolean useAbsoluteLinks = false

    /**
     * Whether to enable reloading
     */
    boolean enableReloading = ViewsEnvironment.isDevelopmentMode()

    /**
     * The package name to use
     */
    String packageName = Metadata.getCurrent().getApplicationName() ?: ""

    /**
     * Whether to compile templates statically
     */
    boolean compileStatic = true

    /**
     * The file extension of the templates
     */
    String extension

    /**
     * Whether the cache templates
     */
    boolean cache = !Environment.isDevelopmentMode()

    /**
     * Whether resource expansion is allowed
     */
    boolean allowResourceExpansion = true

    /**
     * The path to the templates
     */
    String templatePath = findTemplatePath()

    /**
     * The default package imports
     */
    String[] packageImports = ['groovy.transform'] as String[]

    /**
     * The default static imports
     */
    String[] staticImports = ["org.springframework.http.HttpStatus", "org.springframework.http.HttpMethod", "grails.web.http.HttpHeaders"] as String[]

    List<String> mimeTypes = [MimeType.HTML.name, MimeType.XHTML.name]

    ComponentViewConfiguration() {
        setExtension(ComponentViewTemplate.EXTENSION)
        setBaseTemplateClass(ComponentViewTemplate)
        setCacheTemplates( !ViewsEnvironment.isDevelopmentMode() )
        setAutoEscape(true)
        setPrettyPrint( ViewsEnvironment.isDevelopmentMode() )
    }

    @Override
    void setPrettyPrint(boolean prettyPrint) {
        setAutoIndent(true)
        setAutoNewLine(true)
    }

    @Override
    void setEncoding(String encoding) {
        setDeclarationEncoding(encoding)
    }

    @Override
    boolean isCache() {
        return isCacheTemplates()
    }

    @Override
    void setCache(boolean cache) {
        setCacheTemplates(cache)
    }

    @Override
    String getViewModuleName() {
        MODULE_NAME
    }

    PropertyDescriptor[] findViewConfigPropertyDescriptor() {
        List<PropertyDescriptor> allDescriptors = []
        allDescriptors.addAll(BeanUtils.getPropertyDescriptors(GenericViewConfiguration))
        allDescriptors.addAll(BeanUtils.getPropertyDescriptors(TemplateConfiguration))
        return allDescriptors as PropertyDescriptor[]
    }

    @Override
    void setGrailsApplication(GrailsApplication grailsApplication) {
        if(grailsApplication != null) {
            def domainArtefacts = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)
            setPackageImports(
                    findUniquePackages(domainArtefacts)
            )
        }
    }

    static String[] findUniquePackages(GrailsClass[] grailsClasses) {
        Set packages = []
        for (GrailsClass cls : grailsClasses) {
            packages << cls.packageName
        }
        packages as String[]
    }

    static String findTemplatePath() {
        File viewDir = new File(BuildSettings.GRAILS_APP_DIR, "views")
        return viewDir
    }

}
