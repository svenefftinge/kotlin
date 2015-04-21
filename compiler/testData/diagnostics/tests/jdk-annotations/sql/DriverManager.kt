// !CHECK_TYPE

import java.sql.DriverManager

fun getConnection(url: String?) {
  DriverManager.getConnection(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>url<!>)
  checkSubtype<java.sql.Connection>(DriverManager.getConnection(url!!))
}

fun getConnection(url: String?, props: java.util.Properties?) {
  DriverManager.getConnection(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>url<!>, props)
  checkSubtype<java.sql.Connection>(DriverManager.getConnection(url!!, props))
}

fun getConnection(url: String?, user: String?, password: String?) {
  DriverManager.getConnection(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>url<!>, user!!, password!!)
  DriverManager.getConnection(url!!, user, password<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
  DriverManager.getConnection(url<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>, user<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>, password)
  checkSubtype<java.sql.Connection>(DriverManager.getConnection(url<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>, user<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>, password<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>))
}

fun getDriver(url: String?) {
  DriverManager.getDriver(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>url<!>)
  checkSubtype<java.sql.Driver>(DriverManager.getDriver(url!!))
}

fun registerDriver(driver: java.sql.Driver?) {
   DriverManager.registerDriver(<!NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS!>driver<!>)
   DriverManager.registerDriver(driver!!)
}

fun getDrivers() {
   // todo fix to java.util.Enumeration<java.sql.Driver> bug in compiler fixed
   checkSubtype<java.util.Enumeration<*>>(DriverManager.getDrivers())
}