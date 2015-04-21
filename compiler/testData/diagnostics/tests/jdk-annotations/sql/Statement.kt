// !CHECK_TYPE

fun executeQuery(statement: java.sql.Statement, cmd: String?) {
  statement.executeQuery(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>cmd<!>)
  checkSubtype<java.sql.ResultSet>(statement.executeQuery(cmd!!))
}

fun executeQuery(statement: java.sql.PreparedStatement) {
  checkSubtype<java.sql.ResultSet>(statement.executeQuery())
}

fun executeUpdate(statement: java.sql.Statement, cmd: String?) {
  statement.executeUpdate(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>cmd<!>)
  statement.executeUpdate(cmd!!)
}