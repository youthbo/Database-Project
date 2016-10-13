package se.plushogskolan.database.repository.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Mapper<T> {
     T map(ResultSet result) throws SQLException;
}
