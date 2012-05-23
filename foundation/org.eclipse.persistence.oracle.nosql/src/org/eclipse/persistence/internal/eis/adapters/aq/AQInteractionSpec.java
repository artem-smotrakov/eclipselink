/*******************************************************************************
 * Copyright (c) 1998, 2012 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.eis.adapters.aq;

import javax.resource.cci.*;

/**
 * Interaction spec for AQ JCA adapter.
 * Abstract type to enqueue and dequeue sub-types.
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public abstract class AQInteractionSpec implements InteractionSpec {
    protected String queue;
    protected String schema;

    /**
     * Default constructor.
     */
    public AQInteractionSpec() {
    }

    /**
     * Return the queue name.
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Set the queue name.
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * Set the queue schema.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Return the queue schema.
     */
    public String getSchema() {
        return schema;
    }

    public String toString() {
        return getClass().getName() + "(" + getQueue() + ")";
    }
}
