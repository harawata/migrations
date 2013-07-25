package org.apache.ibatis.migration.java_api_test.scripts_java;

import org.apache.ibatis.migration.BootstrapScript;

public class Bootstrap implements BootstrapScript {

  @Override
  public String getScript() {
    return "CREATE TABLE bootstrap_table (ID INTEGER NOT NULL, NAME VARCHAR(16));";
  }

}
