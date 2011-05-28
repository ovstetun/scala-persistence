import sbt._

class ScalaQueryProject(info: ProjectInfo) extends DefaultProject(info) {
	
  val scalaQuery = "org.scalaquery" %% "scalaquery" % "0.9.4" withSources()

  val specs2 = "org.specs2" %% "specs2" % "1.3" % "test" withSources()
  val h2 = "com.h2database" % "h2" % "1.3.153" % "test"
  
  val snapshots = ScalaToolsSnapshots
  
  def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
  override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
}
