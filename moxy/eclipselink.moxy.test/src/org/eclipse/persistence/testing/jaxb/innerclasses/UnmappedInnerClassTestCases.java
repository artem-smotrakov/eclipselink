/*******************************************************************************
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Blaise Doughan - 2.3.3 - initial implementation
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.innerclasses;

import org.eclipse.persistence.testing.jaxb.JAXBWithJSONTestCases;

public class UnmappedInnerClassTestCases extends JAXBWithJSONTestCases {

    private static final String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/innerclasses/unmappedinner.xml";
    private static final String JSON_RESOURCE = "org/eclipse/persistence/testing/jaxb/innerclasses/unmappedinner.json";
    
    public UnmappedInnerClassTestCases(String name) throws Exception {
        super(name);
        setClasses(new Class[] {UnmappedInnerClassRoot.class});
        setControlDocument(XML_RESOURCE);
        setControlJSON(JSON_RESOURCE);
    }

    @Override
    protected UnmappedInnerClassRoot getControlObject() {
        UnmappedInnerClassRoot root = new UnmappedInnerClassRoot();
        root.name = "FOO";
        return root;
    }

}
