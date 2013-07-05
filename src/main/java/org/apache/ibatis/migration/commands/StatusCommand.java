package org.apache.ibatis.migration.commands;

import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.operations.StatusOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class StatusCommand extends BaseCommand {
  private int applied;

  private int pending;

  private List<Change> changes;

  public StatusCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    StatusOperation operation = new StatusOperation();
    operation.operate(getConnectionProvider(), getMigrationsLoader(), getDatabaseOperationOption(), printStream);
    applied = operation.getAppliedCount();
    pending = operation.getPendingCount();
    changes = operation.getCurrentStatus();
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
