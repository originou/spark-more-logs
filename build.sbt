name := "spark-more-logs"

version := "0.1"

crossScalaVersions := Seq("2.11.8", "2.12.12")
//scalaVersion := "2.11.8"

val sparkLibDep: Seq[ModuleID] = Seq(
  "org.apache.spark" %% "spark-core" % "2.4.6",
  "org.apache.spark" %% "spark-sql" % "2.4.6",
)

val javaUtilsDep = Seq(
  "org.projectlombok" % "lombok" % "1.18.18",
)

libraryDependencies ++= sparkLibDep
libraryDependencies ++= javaUtilsDep
