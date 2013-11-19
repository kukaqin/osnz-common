package nz.net.osnz.common.jsresource;

/**
 * An application resource has a scope in which it will operate
 */
public enum ResourceScope {

    /**
     * Ext Scope contains resources specific to ExtJS
     */
    Ext("ext"),

    /**
     * Angular scope contains resources specific to AngularJS
     */
    Angular("angular"),

    /**
     * Global scope is used when application resources should be available to anyone
     */
    Global("global"),

    /**
     * Session scope contains resources specific to a user
     */
    Session("session"),

    /**
     * State to cope with unparseable scope identifier
     */
    Unknown("unkown");

    private final String label;

    ResourceScope(String l) {
        this.label = l;
    }

    public String getLabel() {
        return this.label;
    }

}