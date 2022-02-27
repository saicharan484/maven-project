package sortpom.wrapper;

import org.jdom.Element;
import sortpom.parameter.DependencySortOrder;
import sortpom.parameter.PluginParameters;
import sortpom.wrapper.content.*;

import static sortpom.wrapper.ElementUtil.*;

/**
 * Create wrappers around xml elements. The wrappers help to sort the XML element among themselves.
 */
public class ElementWrapperCreator {
    private DependencySortOrder sortDependencies;
    private DependencySortOrder sortDependencyExclusions;
    private DependencySortOrder sortPlugins;
    private boolean sortProperties;
    private boolean sortModules;
    private boolean sortExecutions;

    private final ElementSortOrderMap elementNameSortOrderMap;


    ElementWrapperCreator(ElementSortOrderMap elementNameSortOrderMap) {
        this.elementNameSortOrderMap = elementNameSortOrderMap;
    }

    public void setup(PluginParameters pluginParameters) {
        this.sortDependencies = pluginParameters.sortDependencies;
        this.sortDependencyExclusions = pluginParameters.sortDependencyExclusions;
        this.sortPlugins = pluginParameters.sortPlugins;
        this.sortProperties = pluginParameters.sortProperties;
        this.sortModules = pluginParameters.sortModules;
        this.sortExecutions = pluginParameters.sortExecutions;
    }

    Wrapper<Element> createWrapper(Element element) {
        boolean sortedBySortOrderFile = elementNameSortOrderMap.containsElement(element);
        if (sortedBySortOrderFile) {
            if (isDependencyElement(element)) {
                DependencySortedWrapper dependencySortedWrapper = new DependencySortedWrapper(element, elementNameSortOrderMap.getSortOrder(element));
                dependencySortedWrapper.setSortOrder(sortDependencies);
                return dependencySortedWrapper;
            }
            if (isExclusionElement(element)) {
                ExclusionSortedWrapper exclusionSortedWrapper = new ExclusionSortedWrapper(element, elementNameSortOrderMap.getSortOrder(element));
                exclusionSortedWrapper.setSortOrder(sortDependencyExclusions);
                return exclusionSortedWrapper;
            }
            if (isPluginElement(element)) {
                PluginSortedWrapper pluginSortedWrapper = new PluginSortedWrapper(element, elementNameSortOrderMap.getSortOrder(element));
                pluginSortedWrapper.setSortOrder(sortPlugins);
                return pluginSortedWrapper;
            }
            if (isModuleElement(element)) {
                return new ModuleSortedWrapper(element, elementNameSortOrderMap.getSortOrder(element));
            }
            if (isExecutionElement(element)) {
                return new ExecutionSortedWrapper(element, elementNameSortOrderMap.getSortOrder(element));
            }
            return new SortedWrapper(element, elementNameSortOrderMap.getSortOrder(element));
        }
        if (isPropertyElement(element)) {
            return new AlphabeticalSortedWrapper(element);
        }
        return new UnsortedWrapper<>(element);
    }

    private boolean isDependencyElement(final Element element) {
        if (sortDependencies.isNoSorting()) {
            return false;
        }
        return isElementName(element, "dependency") && isElementParentName(element, "dependencies");
    }

    private boolean isExclusionElement(final Element element) {
        if (sortDependencyExclusions.isNoSorting()) {
            return false;
        }
        return isElementName(element, "exclusion") && isElementParentName(element, "exclusions");
    }

    private boolean isPluginElement(final Element element) {
        if (sortPlugins.isNoSorting()) {
            return false;
        }
        return isElementName(element, "plugin") &&
                (isElementParentName(element, "plugins") || isElementParentName(element, "reportPlugins"));
    }

    private boolean isModuleElement(final Element element) {
        if (!sortModules) {
            return false;
        }
        return isElementName(element, "module") && isElementParentName(element, "modules");
    }

    private boolean isExecutionElement(final Element element) {
        if (!sortExecutions) {
            return false;
        }
        return isElementName(element, "execution") && isElementParentName(element, "executions");
    }

    private boolean isPropertyElement(final Element element) {
        if (!sortProperties) {
            return false;
        }
        String deepName = getDeepName(element);
        boolean inTheRightPlace = deepName.startsWith("/project/properties/")
                || deepName.startsWith("/project/profiles/profile/properties/");
        return inTheRightPlace && isElementParentName(element, "properties");
    }

}
