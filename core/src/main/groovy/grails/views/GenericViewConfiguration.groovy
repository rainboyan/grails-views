package grails.views

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.support.GrailsApplicationAware
import grails.util.Environment
import grails.util.Metadata
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler

/**
 * Default configuration
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
trait GenericViewConfiguration implements ViewConfiguration, GrailsApplicationAware {

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
     * The template base class
     */
    private Class baseTemplateClass
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
    String templatePath = ViewsEnvironment.findTemplatePath()

    /**
     * The default package imports
     */
    String[] packageImports = ['groovy.transform'] as String[]
    /**
     * The default static imports
     */
    String[] staticImports = ["org.springframework.http.HttpStatus", "org.springframework.http.HttpMethod", "grails.web.http.HttpHeaders"] as String[]

    void setBaseTemplateClass(Class<?> baseTemplateClass) {
        this.baseTemplateClass = baseTemplateClass
    }

    @Override
    Class<?> getBaseTemplateClass() {
        this.baseTemplateClass
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

    String[] findUniquePackages(GrailsClass[] grailsClasses) {
        Set packages = []
        for (GrailsClass cls : grailsClasses) {
            packages << cls.packageName
        }
        packages as String[]
    }

}
