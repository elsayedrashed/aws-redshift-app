name := "aws-redshift-app"

version := "0.1"

scalaVersion := "2.11.8"

val sparkVersion = "2.4.3"
val ScalaTestVersion = "3.0.8"
val RedshiftVersion ="1.2.10.1009"
val AwsVersion = "1.11.118"
val HadoopVersion = "3.1.1"
val JacksonVersion = "2.9.6"

resolvers ++= Seq(
  "apache-snapshots" at "http://repository.apache.org/snapshots/",
  "redshift" at "https://s3.amazonaws.com/redshift-maven-repository/release",
  "jitpack" at "https://jitpack.io"
)

// Hadoop
libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % HadoopVersion,
  "org.apache.hadoop" % "hadoop-aws" % HadoopVersion
)

// Apache Spark
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided" withSources(),
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided" withSources()
)

// Jackson
libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion
)

// AWS
libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk"  % AwsVersion % "provided",
  "com.amazonaws" % "aws-java-sdk-core" % AwsVersion % "provided",
  "com.amazonaws" % "aws-java-sdk-sts" % AwsVersion % "provided",
  "com.amazonaws" % "aws-java-sdk-redshift" % AwsVersion % "provided"
)

// Redshift
libraryDependencies ++= Seq(
  "com.github.databricks" %% "spark-redshift" % "master-SNAPSHOT",
  "com.amazon.redshift" % "redshift-jdbc42" % RedshiftVersion
)

// Test Framework
logBuffered in Test := false
libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % ScalaTestVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
)
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest,
  "-y", "org.scalatest.FunSuite",
  "-y", "org.scalatest.FunSpec",
  "-y", "org.scalatest.PropSpec",
  "-y", "org.scalatest.FlatSpec",
  "-y", "org.scalatest.FeatureSpec"
)

// Assembly
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x => MergeStrategy.first