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

ThisBuild / versionScheme := Some("semver-spec")
