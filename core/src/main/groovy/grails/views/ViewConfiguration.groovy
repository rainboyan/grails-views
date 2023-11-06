package grails.views

import java.beans.PropertyDescriptor

import org.springframework.beans.BeanUtils

import grails.config.ConfigMap
import org.grails.config.CodeGenConfig

/**
 * Interface for view configurations
 *
 * @author Graeme Rocher
 * @since 1.0
 */
interface ViewConfiguration {

    /**
     *
     * @param compileStatic Weather compile statically
     */
    void setCompileStatic(boolean compileStatic)

    /**
     * @return Should compile statically
     */
    boolean isCompileStatic()

    /**
     *
     * @param allowResourceExpansion Whether to allow resource expansion
     */
    void setAllowResourceExpansion(boolean allowResourceExpansion)

    /**
     * @return Whether to allow resource expansion
     */
    boolean isAllowResourceExpansion()

    /**
     * @return Whether reloading is enabled
     */
    boolean isEnableReloading()

    /**
     *
     * @param prettyPrint Whether to pretty print
     */
    void setPrettyPrint(boolean prettyPrint)

    /**
     * @return Whether to pretty print
     */
    boolean isPrettyPrint()

    /**
     *
     * @param useAbsoluteLinks Whether to use absolute links
     */
    void setUseAbsoluteLinks(boolean useAbsoluteLinks)

    /**
     * @return Whether to use absolute links
     */
    boolean isUseAbsoluteLinks()

    /**
     *
     * @param packageName The package name
     */
    void setPackageName(String packageName)

    /**
     * @return The package name
     */
    String getPackageName()

    /**
     *
     * @param extension The file extension
     */
    void setExtension(String extension)

    /**
     * @return The file extension
     */
    String getExtension()

    /**
     * @return The template base class
     */
    Class<?> getBaseTemplateClass()

    /**
     *
     * @param cache Whether to cache
     */
    void setCache(boolean cache)

    /**
     * @return Whether to cache
     */
    boolean isCache()

    /**
     *
     * @param templatePath Path to the templates
     */
    void setTemplatePath(String templatePath)

    /**
     * @return Path to the templates
     */
    String getTemplatePath()

    /**
     *
     * @param packageImports The packages to automatically import
     */
    void setPackageImports(String[] packageImports)

    /**
     * @return The packages to automatically import
     */
    String[] getPackageImports()

    /**
     *
     * @param staticImports The static imports to automatically import
     */
    void setStaticImports(String[] staticImports)

    /**
     * @return The static imports to automatically import
     */
    String[] getStaticImports()

    /**
     * @return The name of the views module (example json or markup)
     */
    String getViewModuleName()

    /**
     *
     * @param encoding The default encoding to use to render views
     */
    void setEncoding(String encoding)

    /**
     * @return The default encoding to use to render views
     */
    String getEncoding()

    default void readConfiguration(File configFile) {
        if (configFile?.exists()) {
            def config = new CodeGenConfig()
            config.loadYml(configFile)
            readConfiguration(config)
        }
    }

    default void readConfiguration(ConfigMap config) {
        String moduleName = getViewModuleName()
        GroovyObject configObject = (GroovyObject)this
        if (config != null) {
            PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(GenericViewConfiguration)
            for (PropertyDescriptor desc in descriptors) {
                if (desc.writeMethod != null) {
                    String propertyName = desc.name
                    Object value
                    if (desc.propertyType == Class) {
                        String className = config.getProperty("grails.views.${moduleName}.$propertyName".toString(), String)
                        if (className) {
                            value = getClass().classLoader.loadClass(className)
                        }
                    } else {
                        value = config.getProperty("grails.views.${moduleName}.$propertyName", (Class) desc.propertyType)
                    }
                    if (value != null) {
                        configObject.setProperty(propertyName, value)
                    }
                }
            }
        }
    }

}