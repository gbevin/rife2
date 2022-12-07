/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * Interface describing a route that leads to a RIFE2 web execution {@link Element}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 2.0
 */
public interface Route {
    /**
     * The router used to create the route.
     *
     * @return this route's router
     * @since 2.0
     */
    Router router();

    /**
     * The request method that this route responds to.
     *
     * @return this route's request method; or
     * <code>null</code> if the route should respond to all methods
     * @since 2.0
     */
    RequestMethod method();

    /**
     * The URL path that this route responds to.
     *
     * @return this route's path
     * @since 2.0
     */
    String path();

    /**
     * The way this route is handling the path info section.
     *
     * @return this route's path info handling
     * @since 2.0
     */
    PathInfoHandling pathInfoHandling();

    /**
     * The default identifier that can be used when referring to an element of this route.
     *
     * @return this route's default element ID
     * @since 2.0
     */
    String defaultElementId();

    /**
     * Returns an execution {@link Element} instance to handle this route with.
     * Note that the lifecycle of the <code>Element</code> depends on the implementer
     * of this interface. Make sure you understand the implications of this lifecycle as
     * it might have an influence on performance or cause unintended side effects.
     * <p>
     * Each implementer of this interface is free to prepare the <code>Element</code>
     * instance as it best sees fit.
     *
     * @param context the web engine context for which to obtain an element instance for
     * @return an element instance to handle this route
     * @since 2.0
     */
    Element obtainElementInstance(Context context);

    /**
     * Finalize an element instance for this route.
     * <p>
     * Each implementer of this interface is free to finalize the <code>Element</code>
     * instance as it best sees fit.
     *
     * @param element the element to finalize
     * @param context the context in which the element should be finalized
     * @since 2.0
     */
    void finalizeElementInstance(Element element, Context context);
}