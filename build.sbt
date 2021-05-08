// sbt-git-series -- Copyright 2018-2021 -- Justin Patterson
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

sbtPlugin := true

name := "sbt-git-series"
organization := "org.scalawag.sbt"

scalaVersion := "2.12.13"

scalacOptions ++= Seq("-feature", "-deprecation")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// Publishing configuration

Test / publishArtifact := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

homepage := Some(url("https://github.com/scalawag/sbt-git-series"))
startYear := Some(2018)
licenses += "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
scmInfo := Some(ScmInfo(
  url("https://github.com/scalawag/sbt-git-series"),
  "scm:git:git://github.com/scalawag/sbt-git-series.git"
))
developers := List(
  Developer("justinp", "Justin Patterson", "justin@scalawag.org", url("https://github.com/justinp"))
)
credentials += Credentials("GnuPG Key ID", "gpg", "439444E02ED9335F91C538455283F6A358FB8629", "ignored")

