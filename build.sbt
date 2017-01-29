import java.io.{FileInputStream, FileOutputStream, FileWriter}
import java.nio.file.{Files, Paths}

name := "server-push-benchmark"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "args4j" % "args4j" % "2.33",
  "org.apache.commons" % "commons-lang3" % "3.5",
  "com.typesafe" % "config" % "1.3.1",
  "org.asynchttpclient" % "async-http-client" % "2.0.27",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.6"
)

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName in assembly := "benchmark.jar"

mainClass in assembly := Some("io.tailrec.research.serverpush.BenchmarkRunner")

assembly ~= (file => {
  println("Out file : " + file)
  new FileOutputStream(new File("benchmark.jar"))
    .getChannel.transferFrom(new FileInputStream(file).getChannel, 0, Long.MaxValue)
  file
})

