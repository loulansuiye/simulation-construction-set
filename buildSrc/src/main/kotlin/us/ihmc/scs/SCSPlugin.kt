package us.ihmc.scs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.testing.Test

class SCSPlugin : Plugin<Project>
{
   var scsShowGui = false
   var scsVideo = false
   var runningOnCIServer = false
   val javaProperties = hashMapOf<String, String>()

   open class SCSExtension(val javaProperties: Map<String, String>) // in the future, add real fields?

   override fun apply(project: Project)
   {
      project.properties["scsVideo"].run { if (this != null) scsVideo = (this as String).toBoolean() }
      project.properties["scsShowGui"].run { if (this != null) scsShowGui = (this as String).toBoolean() }
      System.getenv("RUNNING_ON_CONTINUOUS_INTEGRATION_SERVER").run { if (this != null) runningOnCIServer = (this as String).toBoolean() }

      // defaults
      javaProperties["keep.scs.up"] = "false"
      javaProperties["run.multi.threaded"] = "true"
      javaProperties["use.perfect.sensors"] = "false"
      javaProperties["scs.dataBuffer.size"] = "8142"
      javaProperties["openh264.license"] = "accept"
      javaProperties["disable.joint.subsystem.publisher"] = "true"
      javaProperties["check.nothing.changed.in.simulation"] = "false"
      javaProperties["show.scs.windows"] = "false"
      javaProperties["create.scs.gui"] = "false"
      javaProperties["show.scs.yographics"] = "false"
      javaProperties["java.awt.headless"] = "true"
      javaProperties["create.videos"] = "false"
      if (scsShowGui)
      {
         javaProperties["show.scs.windows"] = "true"
         javaProperties["create.scs.gui"] = "true"
         javaProperties["java.awt.headless"] = "false"
         javaProperties["show.scs.yographics"] = "true"
      }
      if (scsVideo)
      {
         javaProperties["create.scs.gui"] = "true"
         javaProperties["java.awt.headless"] = "false"
         javaProperties["create.videos"] = "true"
         if (runningOnCIServer)
            javaProperties["create.videos.dir"] = "/opt/ihmc/bamboo-videos"
         else
            javaProperties["create.videos.dir"] = System.getProperty("user.home") + "/bamboo-videos"
      }

      project.extensions.create("scs", SCSExtension::class.java, javaProperties)

      for (allproject in project.allprojects)
      {
         allproject.tasks.withType(JavaExec::class.java) { javaExec ->
            // setup properties for all JavaExec tasks
            javaExec.systemProperties.putAll(javaProperties)
            allproject.logger.info("[scs] Passing JVM args ${javaExec.systemProperties} to $javaExec")
         }
         allproject.tasks.withType(Test::class.java) { test ->
            // setup properties for forked test jvms
            test.systemProperties.putAll(javaProperties)
            allproject.logger.info("[scs] Passing JVM args ${test.systemProperties} to $test")
         }
         allproject.tasks.withType(CreateStartScripts::class.java) { startScripts ->
            // setup properties for all start scripts (includes application plugin)
            val list = arrayListOf<String>()
            javaProperties.forEach {
               list.add("-D${it.key}=${it.value}")
            }
            startScripts.defaultJvmOpts = list
            allproject.logger.info("[scs] Passing JVM args $list to $startScripts")
         }
      }
   }
}