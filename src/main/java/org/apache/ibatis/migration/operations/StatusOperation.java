package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationsLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class StatusOperation extends DatabaseOperation {
  private int applied;

  private int pending;

  private List<Change> changes;

  @Override
  public void operate(ConnectionProvider connectionProvider, MigrationsLoader migrationsLoader, PrintStream printStream, DatabaseOperationOption option) {
    printStream.println("ID             Applied At          Description");
    printStream.println(horizontalLine("", 80));
    List<Change> merged = new ArrayList<Change>();
    List<Change> migrations = migrationsLoader.getMigrations();
    if (changelogExists(connectionProvider, option)) {
      List<Change> changelog = getChangelog(connectionProvider, option);
      for (Change change : migrations) {
        int index = changelog.indexOf(change);
        if (index > -1) {
          merged.add(changelog.get(index));
          applied++;
        } else {
          merged.add(change);
          pending++;
        }
      }
      Collections.sort(merged);
    } else {
      merged.addAll(migrations);
      pending = migrations.size();
    }
    for (Change change : merged) {
      printStream.println(change);
    }
    printStream.println();
  }

  public int getAppliedCount() {
    return applied;
  }

  public int getPendingCount() {
    return pending;
  }

  public List<Change> getCurrentStatus() {
    return changes;
  }
}
