package org.apache.ibatis.migration;

import java.math.BigDecimal;

public interface MigrationScript {
  BigDecimal getId();

  String getDescription();

  String getUpScript();

  String getDownScript();
}
