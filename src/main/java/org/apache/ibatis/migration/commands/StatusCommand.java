package org.apache.ibatis.migration.commands;

import java.util.List;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.operations.StatusOperation;
import org.apache.ibatis.migration.options.SelectedOptions;

public final class StatusCommand extends BaseCommand {
  private StatusOperation operation;

  public StatusCommand(SelectedOptions options) {
    super(options);
  }

  public void execute(String... params) {
    operation = new StatusOperation().operate(getConnectionProvider(), getMigrationsLoader(), getDatabaseOperationOption(), printStream);
  }

  public int getAppliedCount() {
    return operation.getAppliedCount();
  }

  public int getPendingCount() {
    return operation.getPendingCount();
  }

  public List<Change> getCurrentStatus() {
    return operation.getCurrentStatus();
  }
}
