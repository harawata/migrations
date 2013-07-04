package org.apache.ibatis.migration.operations;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.ConnectionProvider;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationsLoader;
import org.apache.ibatis.migration.options.DatabaseOperationOption;

public final class VersionOperation extends DatabaseOperation {
  private BigDecimal version;

  public VersionOperation(BigDecimal version) {
    super();
    this.version = version;
    if (version == null) {
      throw new IllegalArgumentException("The version must be null.");
    }
  }

  @Override
  public void operate(ConnectionProvider connectionProvider, MigrationsLoader migrationsLoader, PrintStream printStream, DatabaseOperationOption option) {
    ensureVersionExists(migrationsLoader);
    Change change = getLastAppliedChange(connectionProvider, option);
    if (change == null || version.compareTo(change.getId()) > 0) {
      printStream.println("Upgrading to: " + version);
      UpOperation up = new UpOperation(1);
      while (!version.equals(change.getId())) {
        up.operate(connectionProvider, migrationsLoader, printStream, option);
        change = getLastAppliedChange(connectionProvider, option);
      }
    } else if (version.compareTo(change.getId()) < 0) {
      printStream.println("Downgrading to: " + version);
      DownOperation down = new DownOperation(1);
      while (!version.equals(change.getId())) {
        down.operate(connectionProvider, migrationsLoader, printStream, option);
        change = getLastAppliedChange(connectionProvider, option);
      }
    } else {
      printStream.println("Already at version: " + version);
    }
    printStream.println();
  }

  private void ensureVersionExists(MigrationsLoader migrationsLoader) {
    List<Change> migrations = migrationsLoader.getMigrations();
    if (!migrations.contains(new Change(version))) {
      throw new MigrationException("A migration for the specified version number does not exist.");
    }
  }
}
