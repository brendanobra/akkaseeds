
name := """akkaseed"""

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

resolvers += "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"

val sprayVersion = "1.3.3"

val sprayTestKitVersion = "1.3.3"

val akkaVersion = "2.3.12"

val stormPathVersion = "1.0.RC8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.specs2" %% "specs2-core" % "2.3.11" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion  % "test",
  "com.novocode" % "junit-interface" % "0.7" % "test->default",
  "commons-io" % "commons-io" % "2.4"

)

resolvers += Resolver.jcenterRepo

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

javacOptions += "-Xmx1G"

testOptions in Test <+= (target in Test) map {
  t => Tests.Argument(TestFrameworks.ScalaTest, "junitxml(directory=\"%s\")" format (t / "../shippable/testresults"))
}

testOptions in Test += Tests.Argument("-oSD")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

fork in run := true

Revolver.settings

fork in Revolver.reStart := true

javaOptions in Revolver.reStart += "-Xmx512m"

javaOptions in Revolver.reStart += s"-Dtag=notag"

javaOptions in Revolver.reStart += s"-Dversion=noversion"

javaOptions in Revolver.reStart +=s"-Drole=seed2"


s3credentials := file(".s3credentials")

publishMavenStyle := false


mainClass in(Compile, packageBin) := Some("org.obrafamily.akkaseed.Main")


test in assembly :=false

import sbtassembly.MergeStrategy
import sbtassembly.MergeStrategy._


val aopMerge: MergeStrategy = new MergeStrategy {
  val name = "aopMerge"
  import scala.xml._
  import scala.xml.dtd._

  def apply(tempDir: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] = {
    val dt = DocType("aspectj", PublicID("-//AspectJ//DTD//EN", "http://www.eclipse.org/aspectj/dtd/aspectj.dtd"), Nil)
    val file = MergeStrategy.createMergeTarget(tempDir, path)
    val xmls: Seq[Elem] = files.map(XML.loadFile)
    val aspectsChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "aspects" \ "_")
    val weaverChildren: Seq[Node] = xmls.flatMap(_ \\ "aspectj" \ "weaver" \ "_")
    val options: String = xmls.map(x => (x \\ "aspectj" \ "weaver" \ "@options").text).mkString(" ").trim
    val weaverAttr = if (options.isEmpty) Null else new UnprefixedAttribute("options", options, Null)
    val aspects = new Elem(null, "aspects", Null, TopScope, false, aspectsChildren: _*)
    val weaver = new Elem(null, "weaver", weaverAttr, TopScope, false, weaverChildren: _*)
    val aspectj = new Elem(null, "aspectj", Null, TopScope, false, aspects, weaver)
    XML.save(file.toString, aspectj, "UTF-8", xmlDecl = false, dt)
    IO.append(file, IO.Newline.getBytes(IO.defaultCharset))
    Right(Seq(file -> path))
  }
}

// Use defaultMergeStrategy with a case for aop.xml
// I like this better than the inline version mentioned in assembly's README
val customMergeStrategy: String => MergeStrategy = {
  case PathList("META-INF", "aop.xml") =>
    aopMerge
  case s =>
    defaultMergeStrategy(s)
}

// Use the customMergeStrategy in your settings
assemblyMergeStrategy in assembly := customMergeStrategy

/*
unmanagedSourceDirectories in MultiJvm <<= Seq(baseDirectory(_ / "src/test")).join
*/
