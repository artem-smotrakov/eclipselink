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
package org.eclipse.persistence.testing.tests.remote;

import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.mappings.*;
import org.eclipse.persistence.descriptors.RelationalDescriptor;

/**
 * This class was generated by the TopLink project class generator.
 * It stores the meta-data (descriptors) that define the TopLink mappings.
 * @see org.eclipse.persistence.sessions.factories.ProjectClassGenerator
 */
public class MasterSlaveProject extends org.eclipse.persistence.sessions.Project {

    public MasterSlaveProject() {
        setName("MasterSlave");
        applyLogin();

        addDescriptor(buildMasterDescriptor());
        addDescriptor(buildSlaveDescriptor());
    }

    public void applyLogin() {
        DatabaseLogin login = new DatabaseLogin();
        setLogin(login);
    }

    public RelationalDescriptor buildMasterDescriptor() {
        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(org.eclipse.persistence.testing.tests.remote.Master.class);
        descriptor.addTableName("MASTER");
        descriptor.addPrimaryKeyFieldName("MASTER.id");

        // Descriptor properties.
        descriptor.useSoftCacheWeakIdentityMap();
        descriptor.setIdentityMapSize(100);
        descriptor.useRemoteSoftCacheWeakIdentityMap();
        descriptor.setRemoteIdentityMapSize(100);

        // Query manager.
        descriptor.getQueryManager().checkDatabaseForDoesExist();

        // Event manager.

        // Mappings.
        DirectToFieldMapping primaryKeyMapping = new DirectToFieldMapping();
        primaryKeyMapping.setAttributeName("primaryKey");
        primaryKeyMapping.setFieldName("MASTER.id");
        descriptor.addMapping(primaryKeyMapping);

        OneToManyMapping slavesMapping = new OneToManyMapping();
        slavesMapping.setAttributeName("slaves");
        slavesMapping.setReferenceClass(org.eclipse.persistence.testing.tests.remote.Slave.class);
        slavesMapping.useBasicIndirection();
        slavesMapping.addTargetForeignKeyFieldName("SLAVE.mid", "MASTER.id");
        descriptor.addMapping(slavesMapping);

        return descriptor;
    }

    public RelationalDescriptor buildSlaveDescriptor() {
        RelationalDescriptor descriptor = new RelationalDescriptor();
        descriptor.setJavaClass(org.eclipse.persistence.testing.tests.remote.Slave.class);
        descriptor.addTableName("SLAVE");
        descriptor.addPrimaryKeyFieldName("SLAVE.id");

        // Descriptor properties.
        descriptor.useSoftCacheWeakIdentityMap();
        descriptor.setIdentityMapSize(100);
        descriptor.useRemoteSoftCacheWeakIdentityMap();
        descriptor.setRemoteIdentityMapSize(100);

        // Query manager.
        descriptor.getQueryManager().checkDatabaseForDoesExist();

        // Event manager.

        // Mappings.
        DirectToFieldMapping MyPrimaryKeyMapping = new DirectToFieldMapping();
        MyPrimaryKeyMapping.setAttributeName("primaryKey");
        MyPrimaryKeyMapping.setFieldName("SLAVE.id");
        descriptor.addMapping(MyPrimaryKeyMapping);

        OneToOneMapping masterMapping = new OneToOneMapping();
        masterMapping.setAttributeName("master");
        masterMapping.setReferenceClass(org.eclipse.persistence.testing.tests.remote.Master.class);
        masterMapping.dontUseIndirection();
        masterMapping.addForeignKeyFieldName("SLAVE.mid", "MASTER.id");
        descriptor.addMapping(masterMapping);

        return descriptor;
    }

}

