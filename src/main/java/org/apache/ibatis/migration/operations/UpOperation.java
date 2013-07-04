package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationsLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class UpOperation extends DatabaseOperation {
  private final Integer steps;

  public UpOperation() {
    super();
    this.steps = null;
  }

  public UpOperation(Integer steps) {
    super();
    this.steps = steps;
  }

  @Override
  public void operate(ConnectionProvider connectionProvider, MigrationsLoader migrationsLoader, PrintStream printStream, DatabaseOperationOption option) {
    try {
      Change lastChange = null;
      if (changelogExists(connectionProvider, option)) {
        lastChange = getLastAppliedChange(connectionProvider, option);
      }

      List<Change> migrations = migrationsLoader.getMigrations();
      int stepCount = 0;
      for (Change change : migrations) {
        if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
          printStream.println(horizontalLine("Applying: " + change.getFilename(), 80));
          ScriptRunner runner = getScriptRunner(connectionProvider, printStream, option);
          try {
            runner.runScript(migrationsLoader.getScriptReader(change, false));
          } finally {
            runner.closeConnection();
          }
          insertChangelog(change, connectionProvider, option);
          printStream.println();
          stepCount++;
          if (steps != null && stepCount > steps) {
            break;
          }
        }
      }
    } catch (Exception e) {
      throw new MigrationException("Error executing command.  Cause: " + e, e);
    }
  }
}
