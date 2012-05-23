/*******************************************************************************
 * Copyright (c) 2006, 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.eclipse.persistence.jpa.jpql;

/**
 * This visitor traverses the JPQL parsed tree and gathers the possible proposals at a given position.
 * <p>
 * Example:
 * <pre><code> // Have the external form of an IQuery
 * {@link org.eclipse.persistence.jpa.jpql.spi.IQuery IQuery} query = ...
 *
 * // Create a JPQLQueryContext
 * {@link JPQLQueryContext} context = new JPQLQueryContext();
 * context.{@link JPQLQueryContext#setQuery(org.eclipse.persistence.jpa.jpql.spi.IQuery) setQuery(query)};
 *
 * // Create a map of the positions within the parsed tree
 * {@link org.eclipse.persistence.jpa.jpql.parser.QueryPosition QueryPosition} queryPosition = context.getJPQLExpression().buildPosition(query.getExpression(), position);
 *
 * // Create the visitor and visit the parsed tree
 * ContentAssistVisitor visitor = new ContentAssistVisitor(context);
 * visitor.{@link #prepare(org.eclipse.persistence.jpa.jpql.parser.QueryPosition) prepare(queryPosition)};
 * queryPosition.getExpression().accept(visitor);
 *
 * // Retrieve the proposals
 * {@link org.eclipse.persistence.jpa.jpql.ContentAssistProposals ContentAssistProposals} proposals = visitor.getProposals();
 *
 * // Only required if the visitor is cached
 * visitor.dispose();
 *
 * // Only required if the context is cached
 * context.dispose();
 * </code></pre>
 * <p>
 * Provisional API: This interface is part of an interim API that is still under development and
 * expected to change significantly before reaching stability. It is available at this early stage
 * to solicit feedback from pioneering adopters on the understanding that any code that uses this
 * API will almost certainly be broken (repeatedly) as the API evolves.
 *
 * @version 2.4
 * @since 2.3
 * @author Pascal Filion
 */
public class DefaultContentAssistVisitor extends AbstractContentAssistVisitor {

	/**
	 * Creates a new <code>DefaultContentAssistVisitor</code>.
	 *
	 * @param queryContext The context used to query information about the query
	 * @exception NullPointerException The {@link JPQLQueryContext} cannot be <code>null</code>
	 */
	public DefaultContentAssistVisitor(JPQLQueryContext queryContext) {
		super(queryContext);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isJoinFetchIdentifiable() {
		return false;
	}
}