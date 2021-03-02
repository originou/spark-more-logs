name := "spark-more-logs"

version := "0.1"

crossScalaVersions := Seq("2.11.8", "2.12.12")
//scalaVersion := "2.11.8"

// Because I use Lombok on java code, java source code should be compile first
Compile / compileOrder := CompileOrder.JavaThenScala
Test / compileOrder := CompileOrder.JavaThenScala

val sparkLibDep: Seq[ModuleID] = Seq(
  "org.apache.spark" %% "spark-core" % "2.4.6",
  "org.apache.spark" %% "spark-sql" % "2.4.6",
)

val javaUtilsDep = Seq(
  "org.projectlombok" % "lombok" % "1.18.18",
)

val testUnitDep = Seq(
  "org.scalactic" %% "scalactic" % "3.2.5",
  "org.scalatest" %% "scalatest" % "3.2.5" % Test,
  "org.scalatestplus" %% "mockito-3-4" % "3.2.5.0" % Test,
)

libraryDependencies ++= sparkLibDep
libraryDependencies ++= javaUtilsDep

libraryDependencies ++= testUnitDep

resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"
addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.12")
