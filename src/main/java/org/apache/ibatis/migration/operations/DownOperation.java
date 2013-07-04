package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationsLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class DownOperation extends DatabaseOperation {
  private Integer steps;

  public DownOperation(Integer steps) {
    super();
    this.steps = steps;
  }

  @Override
  public void operate(ConnectionProvider connectionProvider, MigrationsLoader migrationsLoader, PrintStream printStream, DatabaseOperationOption option) {
    try {
      Change lastChange = getLastAppliedChange(connectionProvider, option);
      if (lastChange == null) {
        printStream.println("Changelog exist, but no migration found.");
      } else {
        List<Change> migrations = migrationsLoader.getMigrations();
        Collections.reverse(migrations);
        int stepCount = 0;
        for (Change change : migrations) {
          if (change.getId().equals(lastChange.getId())) {
            printStream.println(horizontalLine("Undoing: " + change.getFilename(), 80));
            ScriptRunner runner = getScriptRunner(connectionProvider, printStream, option);
            try {
              runner.runScript(migrationsLoader.getScriptReader(change, true));
            } finally {
              runner.closeConnection();
            }
            if (changelogExists(connectionProvider, option)) {
              deleteChange(connectionProvider, change, option);
            } else {
              printStream.println("Changelog doesn't exist. No further migrations will be undone (normal for the last migration).");
            }
            printStream.println();
            stepCount++;
            if (steps == null || stepCount > steps) {
              break;
            }
            lastChange = getLastAppliedChange(connectionProvider, option);
          }
        }
      }
    } catch (Exception e) {
      throw new MigrationException("Error undoing last migration.  Cause: " + e, e);
    }
  }

  protected void deleteChange(ConnectionProvider connectionProvider, Change change, DatabaseOperationOption option) {
    SqlRunner runner = getSqlRunner(connectionProvider);
    try {
      runner.delete("delete from " + option.getChangelogTable() + " where id = ?", change.getId());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      runner.closeConnection();
    }
  }
}
