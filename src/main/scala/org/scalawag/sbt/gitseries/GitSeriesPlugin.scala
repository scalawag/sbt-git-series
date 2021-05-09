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

package org.scalawag.sbt.gitseries

import com.typesafe.sbt.SbtGit.git.{gitCurrentBranch, gitCurrentTags}
import com.typesafe.sbt.SbtGit.{git, useJGit}
import sbt.{AutoPlugin, plugins}
import sbt.Keys.version
import sbt._


object GitSeriesPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {
    val jenkinsMode = SettingKey[Boolean]("jenkinsMode", "modify version derivation to account for Jenkins' git usage")
    val travisMode = SettingKey[Boolean]("travisMode", "modify version derivation to account for Travis CI's git usage")
  }

  private val seriesBranchRegex = "series/(\\d+\\.\\d+)".r
  private val topicBranchRegex = "topic/(\\d+\\.\\d+(\\.\\d+)?)/([a-zA-Z0-9_-]+)".r
  private val releaseRefRegex = "release/(\\d+\\.\\d+\\.\\d+)".r
  private val prereleaseTagRegex = "prerelease/(\\d+\\.\\d+\\.\\d+)-(\\d+)".r

  import autoImport._

  override def projectSettings = Seq[Def.Setting[_]](
    useJGit,
    jenkinsMode := false,
    travisMode := false,
    ThisBuild / version := {
      val s = Keys.sLog.value

      val currentTags = gitCurrentTags.value
      val currentBranch = gitCurrentBranch.value

      val eligibleBranches =
        if ( jenkinsMode.value )
          // Jenkins checks out its tags and creates a branch with the same name, so we need to ignore any branches that
          // have a coexistent similarly-named tag.
          Iterable(currentBranch).filterNot(currentTags.contains)
        else if ( travisMode.value && sys.env.get("TRAVIS_BRANCH") != sys.env.get("TRAVIS_TAG") )
          // Travis always checks out a detached head for branches, but we can tell what it was trying to do by looking
          // at $TRAVIS_BRANCH and $TRAVIS_TAG. If they're different, we can use that branch name as if it were checked
          // out because Travis thinks it's building a branch.
          sys.env.get("TRAVIS_BRANCH").toIterable
        else
          Iterable(currentBranch)

      s.info(s"currentTags: ${currentTags.mkString(" ")}")

      val warning = if (eligibleBranches.isEmpty) s" (ignored because of tag $currentBranch)" else ""

      s.info(s"currentBranch: $currentBranch$warning")

      val seriesBranchVersion = eligibleBranches.collectFirst {
        case seriesBranchRegex(series) => s"$series-SNAPSHOT"
      }

      val releaseBranchVersion = eligibleBranches.collectFirst {
        case releaseRefRegex(release) => s"$release-SNAPSHOT"
      }

      val topicBranchVersion = eligibleBranches.collectFirst {
        case topicBranchRegex(target, topic) => s"$target-$topic-SNAPSHOT"
      }

      val prereleaseTagVersion = currentTags.collectFirst {
        case prereleaseTagRegex(version, pre) => s"$version-pre.$pre"
      }

      val releaseTagVersion = currentTags.collectFirst {
        case releaseRefRegex(tag) => tag
      }

      // These environment variables are only defined when building in Jenkins.

      val prVersion =
        if ( jenkinsMode.value)
          for {
            prId <- sys.env.get("CHANGE_ID")
            seriesBranchRegex(series) <- sys.env.get("CHANGE_TARGET")
          } yield {
            s"$series-PR-$prId-SNAPSHOT"
          }
        else
          None

      val result = git.makeVersion(Seq(
        seriesBranchVersion,
        releaseBranchVersion,
        topicBranchVersion,
        prVersion,
        prereleaseTagVersion,
        releaseTagVersion)
      ).getOrElse("0.0-SNAPSHOT")

      s.info(s"version set to: $result")

      result
    }
  )
}
