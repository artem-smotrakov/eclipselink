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
 *     Oracle - initial API and implementation
 *
 ******************************************************************************/
package org.eclipse.persistence.jpa.tests.jpql;

import java.util.Date;
import org.eclipse.persistence.jpa.jpql.AbstractJPQLQueryHelper;
import org.eclipse.persistence.jpa.jpql.spi.IEntity;
import org.eclipse.persistence.jpa.jpql.spi.IQuery;
import org.eclipse.persistence.jpa.jpql.spi.IType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version 2.4
 * @since 2.4
 * @author Pascal Filion
 */
@SuppressWarnings("nls")
public final class DefaultJPQLQueryHelperTest2_1 extends AbstractJPQLQueryHelperTest {

	protected IEntity entity(String entityName) throws Exception {
		IEntity entity = getPersistenceUnit().getEntityNamed(entityName);
		assertNotNull("The entity " + entityName + " could not be found", entity);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IQuery namedQuery(String entityName, String queryName) throws Exception {
		IEntity entity = entity(entityName);
		IQuery namedQuery = entity.getNamedQuery(queryName);
		assertNotNull("The named query " + queryName + " could not be found on " + entityName, namedQuery);
		return namedQuery;
	}

	@Test
	public void test_ResultType_Treat_1() throws Exception {

		// SELECT TREAT(TREAT(p.project LargeProject).parent AS LargeProject).endDate
		// FROM Product p
		IQuery namedQuery = namedQuery("Product", "product.treat");

		AbstractJPQLQueryHelper helper = buildQueryHelper(namedQuery);
		IType type = helper.getResultType();

		assertNotNull(
			"The type of TREAT(TREAT(p.project LargeProject).parent AS LargeProject).endDate should have been found",
			type
		);

		assertEquals(
			"The wrong type for TREAT(TREAT(p.project LargeProject).parent AS LargeProject).endDate was retrieved",
			getType(namedQuery, Date.class),
			type
		);
	}
}