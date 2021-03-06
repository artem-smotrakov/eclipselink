/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     bdoughan - December 4/2009 - 2.1 - Initial implementation
package org.eclipse.persistence.testing.oxm.mappings.lexicalhandler;

public class Address {

    private String street;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public boolean equals(Object object) {
        try {
            Address test = (Address) object;
            if(null == street) {
                return null == test.getStreet();
            } else {
                return street.equals(test.getStreet());
            }
        } catch(ClassCastException e) {
            return false;
        }
    }

}
