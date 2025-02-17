package com.vladmihalcea.book.hpjp.jdbc.transaction.locking.advisory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Vlad Mihalcea
 */
public class PostgreSQLSessionTryAdvisoryLocksTest extends AbstractPostgreSQLAdvisoryLocksTest {

	@Override
	protected int acquireLock(Connection connection, int logIndex, int workerId) {
		LOGGER.info( "Worker {} writes to log {}", workerId, logIndex );
		try(PreparedStatement statement =
				connection.prepareStatement("select pg_try_advisory_lock(?)")) {
			boolean lockAcquired;
			statement.setInt( 1, logIndex );

			do {
				ResultSet resultSet = statement.executeQuery();
				resultSet.next();
				lockAcquired = resultSet.getBoolean( 1 );
				LOGGER.info(
						"Worker {} has {} acquired an advisory lock on log {}",
						workerId,
						lockAcquired ? "" : "not",
						logIndex
				);
				logIndex = randomLogIndex();
				LOGGER.info("Trying with log {}", logIndex);
			}
			while ( !lockAcquired );
		}
		catch (SQLException e) {
			LOGGER.error( "Worker {} failed with this message: {}", workerId, e.getMessage() );
		}
		return logIndex;
	}

	@Override
	protected void releaseLock(Connection connection, int logIndex, int workerId) {
		try(PreparedStatement statement =
					connection.prepareStatement("select pg_advisory_unlock(?)")) {
			statement.setInt( 1, logIndex );
			statement.executeQuery();
		}
		catch (SQLException e) {
			LOGGER.error( "Worker {} failed with this message: {}", workerId, e.getMessage() );
		}
	}
}
