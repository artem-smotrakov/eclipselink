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
package org.eclipse.persistence.testing.tests.nchar;

import org.eclipse.persistence.tools.schemaframework.*;

/**
 * This class was generated by the TopLink table creator generator.
 * It stores the meta-data (tables) that define the database schema.
 * @see org.eclipse.persistence.sessions.factories.TableCreatorClassGenerator
 */
public class CharNcharTableCreator extends TableCreator {

    public CharNcharTableCreator() {
        setName("CharNchar");

        addTableDefinition(buildCHARNCHARTable());
    }

    public TableDefinition buildCHARNCHARTable() {
        TableDefinition table = new TableDefinition();
        table.setName("CHARNCHAR");

        FieldDefinition fieldID = new FieldDefinition();
        fieldID.setName("ID");
        fieldID.setTypeName("NUMBER");
        fieldID.setSize(20);
        fieldID.setSubSize(0);
        fieldID.setIsPrimaryKey(true);
        fieldID.setIsIdentity(false);
        fieldID.setUnique(false);
        fieldID.setShouldAllowNull(false);
        table.addField(fieldID);

        FieldDefinition fieldCH = new FieldDefinition();
        fieldCH.setName("CH");
        fieldCH.setTypeName("CHAR");
        fieldCH.setSize(1);
        fieldCH.setSubSize(0);
        fieldCH.setIsPrimaryKey(false);
        fieldCH.setIsIdentity(false);
        fieldCH.setUnique(false);
        fieldCH.setShouldAllowNull(true);
        table.addField(fieldCH);

        FieldDefinition fieldNCH = new FieldDefinition();
        fieldNCH.setName("NCH");
        fieldNCH.setTypeName("NCHAR");
        fieldNCH.setSize(1);
        fieldNCH.setSubSize(0);
        fieldNCH.setIsPrimaryKey(false);
        fieldNCH.setIsIdentity(false);
        fieldNCH.setUnique(false);
        fieldNCH.setShouldAllowNull(true);
        table.addField(fieldNCH);

        FieldDefinition fieldSTR = new FieldDefinition();
        fieldSTR.setName("STR");
        fieldSTR.setTypeName("VARCHAR2");
        fieldSTR.setSize(20);
        fieldSTR.setSubSize(0);
        fieldSTR.setIsPrimaryKey(false);
        fieldSTR.setIsIdentity(false);
        fieldSTR.setUnique(false);
        fieldSTR.setShouldAllowNull(true);
        table.addField(fieldSTR);

        FieldDefinition fieldNSTR = new FieldDefinition();
        fieldNSTR.setName("NSTR");
        fieldNSTR.setTypeName("NVARCHAR2");
        fieldNSTR.setSize(20);
        fieldNSTR.setSubSize(0);
        fieldNSTR.setIsPrimaryKey(false);
        fieldNSTR.setIsIdentity(false);
        fieldNSTR.setUnique(false);
        fieldNSTR.setShouldAllowNull(true);
        table.addField(fieldNSTR);

        FieldDefinition fieldCLB = new FieldDefinition();
        fieldCLB.setName("CLB");
        fieldCLB.setTypeName("CLOB");
        fieldCLB.setSize(0);
        fieldCLB.setSubSize(0);
        fieldCLB.setIsPrimaryKey(false);
        fieldCLB.setIsIdentity(false);
        fieldCLB.setUnique(false);
        fieldCLB.setShouldAllowNull(true);
        table.addField(fieldCLB);

        FieldDefinition fieldNCLB = new FieldDefinition();
        fieldNCLB.setName("NCLB");
        fieldNCLB.setTypeName("NCLOB");
        fieldNCLB.setSize(0);
        fieldNCLB.setSubSize(0);
        fieldNCLB.setIsPrimaryKey(false);
        fieldNCLB.setIsIdentity(false);
        fieldNCLB.setUnique(false);
        fieldNCLB.setShouldAllowNull(true);
        table.addField(fieldNCLB);

        FieldDefinition fieldCLB2 = new FieldDefinition();
        fieldCLB2.setName("CLB2");
        fieldCLB2.setTypeName("CLOB");
        fieldCLB2.setSize(0);
        fieldCLB2.setSubSize(0);
        fieldCLB2.setIsPrimaryKey(false);
        fieldCLB2.setIsIdentity(false);
        fieldCLB2.setUnique(false);
        fieldCLB2.setShouldAllowNull(true);
        table.addField(fieldCLB2);

        FieldDefinition fieldNCLB2 = new FieldDefinition();
        fieldNCLB2.setName("NCLB2");
        fieldNCLB2.setTypeName("NCLOB");
        fieldNCLB2.setSize(0);
        fieldNCLB2.setSubSize(0);
        fieldNCLB2.setIsPrimaryKey(false);
        fieldNCLB2.setIsIdentity(false);
        fieldNCLB2.setUnique(false);
        fieldNCLB2.setShouldAllowNull(true);
        table.addField(fieldNCLB2);

        return table;
    }

}
