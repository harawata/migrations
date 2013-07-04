package org.apache.ibatis.migration;

import java.io.Reader;
import java.util.List;

public interface MigrationsLoader {

  List<Change> getMigrations();

  Reader getScriptReader(Change change, boolean undo);

  Reader getBootstrapReader();

}
