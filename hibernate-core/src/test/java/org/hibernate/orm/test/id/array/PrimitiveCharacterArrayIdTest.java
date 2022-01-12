/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.id.array;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.Query;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:p.krauzowicz@visiona.pl">Piotr Krauzowicz</a>
 * @author Gail Badner
 */
@DomainModel(
		annotatedClasses = PrimitiveCharacterArrayIdTest.DemoEntity.class
)
@SessionFactory
public class PrimitiveCharacterArrayIdTest {

	@BeforeEach
	public void prepare(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					for ( int i = 0; i < 3; i++ ) {
						DemoEntity entity = new DemoEntity();
						entity.id = new char[] {
								(char) ( i + 'a' ),
								(char) ( i + 'b' ),
								(char) ( i + 'c' ),
								(char) ( i + 'd' )
						};
						entity.name = "Simple name " + i;
						session.persist( entity );
					}
				}
		);
	}

	@AfterEach
	public void cleanup(SessionFactoryScope scope) {
		scope.inTransaction(
				session ->
						session.createQuery( "delete from PrimitiveCharacterArrayIdTest$DemoEntity" ).executeUpdate()
		);
	}

	/**
	 * Removes two records from database.
	 */
	@Test
	@TestForIssue(jiraKey = "HHH-8999")
	public void testMultipleDeletions(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Query query = session.createQuery( "SELECT s FROM PrimitiveCharacterArrayIdTest$DemoEntity s" );
					List results = query.list();
					session.delete( results.get( 0 ) );
					session.delete( results.get( 1 ) );
				}
		);

		scope.inTransaction(
				session -> {
					Query query = session.createQuery( "SELECT s FROM PrimitiveCharacterArrayIdTest$DemoEntity s" );
					assertEquals( 1, query.list().size() );
				}
		);
	}

	/**
	 * Updates two records from database.
	 */
	@Test
	@TestForIssue(jiraKey = "HHH-8999")
	public void testMultipleUpdates(SessionFactoryScope scope) {
		final String lastResultName = scope.fromTransaction(
				session -> {
					Query query = session.createQuery( "SELECT s FROM PrimitiveCharacterArrayIdTest$DemoEntity s" );
					List<DemoEntity> results = (List<DemoEntity>) query.list();
					results.get( 0 ).name = "Different 0";
					results.get( 1 ).name = "Different 1";
					return results.get( 0 ).name;
				}
		);

		scope.inTransaction(
				session -> {
					Query query = session.createQuery( "SELECT s FROM PrimitiveCharacterArrayIdTest$DemoEntity s" );
					List<DemoEntity> results = (List<DemoEntity>) query.list();
					final Set<String> names = new HashSet<String>();
					for ( DemoEntity entity : results ) {
						names.add( entity.name );
					}
					assertTrue( names.contains( "Different 0" ) );
					assertTrue( names.contains( "Different 1" ) );
					assertTrue( names.contains( lastResultName ) );
				}
		);
	}

	@Entity
	@Table(name = "DemoEntity")
	public static class DemoEntity {
		@Id
		public char[] id;
		public String name;
	}
}
