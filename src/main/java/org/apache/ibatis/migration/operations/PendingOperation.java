package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationsLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class PendingOperation extends DatabaseOperation {

  @Override
  public void operate(ConnectionProvider connectionProvider, MigrationsLoader migrationsLoader, PrintStream printStream, DatabaseOperationOption option) {
    try {
      if (!changelogExists(connectionProvider, option)) {
        throw new MigrationException("Change log doesn't exist, no migrations applied.  Try running 'up' instead.");
      }
      List<Change> pending = getPendingChanges(connectionProvider, migrationsLoader, option);
      printStream.println("WARNING: Running pending migrations out of order can create unexpected results.");
      for (Change change : pending) {
        printStream.println(horizontalLine("Applying: " + change.getFilename(), 80));
        ScriptRunner runner = getScriptRunner(connectionProvider, printStream, option);
        try {
          runner.runScript(migrationsLoader.getScriptReader(change, false));
        } finally {
          runner.closeConnection();
        }
        insertChangelog(change, connectionProvider, option);
        printStream.println();
      }
    } catch (Exception e) {
      throw new MigrationException("Error executing command.  Cause: " + e, e);
    }
  }

  private List<Change> getPendingChanges(ConnectionProvider connectionProvider, MigrationsLoader migrationsLoader, DatabaseOperationOption option) {
    List<Change> pending = new ArrayList<Change>();
    List<Change> migrations = migrationsLoader.getMigrations();
    List<Change> changelog = getChangelog(connectionProvider, option);
    for (Change change : migrations) {
      int index = changelog.indexOf(change);
      if (index < 0) {
        pending.add(change);
      }
    }
    Collections.sort(pending);
    return pending;
  }
}
