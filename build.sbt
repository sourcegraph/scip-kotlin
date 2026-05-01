import _root_.kotlin.Keys._
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

lazy val V = new {
  val kotlin          = "2.2.0"
  val protobuf        = "3.17.3"
  val protoc          = "3.17.3"
  val kotest          = "4.6.3"
  val kctfork         = "0.7.1"
  val scipJava        = "0.12.3"
  val semanticdbJavac = "0.12.3"
}

inThisBuild(
  List(
    organization := "com.sourcegraph",
    homepage     := Some(url("https://github.com/sourcegraph/scip-kotlin")),
    licenses     := List(
      "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "strum355",
        "Noah Santschi-Cooney",
        "noah@sourcegraph.com",
        url("https://github.com/strum355")
      ),
      Developer(
        "olafurpg",
        "Ólafur Páll Geirsson",
        "olafurpg@sourcegraph.com",
        url("https://github.com/olafurpg")
      )
    ),
    dynverSeparator  := "-",
    PB.protocVersion := V.protoc
  )
)

name             := "scip-kotlin-root"
publish / skip   := true
crossPaths       := false
autoScalaLibrary := false

lazy val kotlinc = project
  .in(file("semanticdb-kotlinc"))
  .enablePlugins(KotlinPlugin)
  .settings(
    name             := "semanticdb-kotlinc",
    moduleName       := "semanticdb-kotlinc",
    description      := "A kotlinc plugin to emit SemanticDB information",
    crossPaths       := false,
    autoScalaLibrary := false,
    kotlinVersion    := V.kotlin,
    kotlincJvmTarget := "1.8",
    kotlincOptions ++= Seq("-Xinline-classes", "-Xcontext-parameters"),

    // sbt-kotlin-plugin defaults to adding `kotlin-scripting-compiler-embeddable`
    // (and its transitive kotlin-stdlib) as a regular dependency. Mark them
    // Provided — kotlinc supplies them at runtime, and we don't want them
    // bundled into the fat-jar.
    kotlinRuntimeProvided := true,

    // kotlin-stdlib is supplied by kotlinc at runtime — keep on compile
    // classpath via Provided so the assembled fat-jar does not bundle it.
    libraryDependencies +=
      "org.jetbrains.kotlin" % "kotlin-stdlib" % V.kotlin % Provided,

    // protobuf java codegen — proto file lives at src/main/proto/...
    Compile / PB.protoSources := Seq((Compile / sourceDirectory).value / "proto"),
    Compile / PB.targets := Seq(
      PB.gens.java(V.protobuf) -> (Compile / sourceManaged).value
    ),
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % V.protobuf,

    // kotlin-compiler-embeddable is supplied by kotlinc at runtime
    libraryDependencies +=
      "org.jetbrains.kotlin" % "kotlin-compiler-embeddable" % V.kotlin % Provided,

    // ---- sbt-assembly fat-jar ---------------------------------------------
    // Mirrors scip-java's `fatjarPackageSettings`. Produces a shaded jar that
    // replaces the slim `packageBin` so `publishLocal` ships the shaded
    // artifact (the same artifact Gradle's shadowJar produced previously).
    assembly / assemblyShadeRules := Seq(
      // Relocate any IntelliJ classes the same way kotlin-compiler-embeddable
      // does internally. Do NOT rename `com.sourcegraph.**` — the
      // META-INF/services files reference those FQNs.
      ShadeRule
        .rename("com.intellij.**" -> "org.jetbrains.kotlin.com.intellij.@1")
        .inAll
    ),
    Compile / packageBin := assembly.value,
    // Strip every <dependency> from the POM — the fat-jar absorbs the
    // protobuf runtime, and the kotlin-* deps are Provided by kotlinc.
    pomPostProcess := { node =>
      new RuleTransformer(new RewriteRule {
        override def transform(n: XmlNode): XmlNodeSeq =
          if (n.label == "dependency") XmlNodeSeq.Empty else n
      }).transform(node).head
    },

    // tests
    libraryDependencies ++= Seq(
      "org.jetbrains.kotlin"  % "kotlin-compiler-embeddable" % V.kotlin                          % Test,
      "org.jetbrains.kotlin"  % "kotlin-test"                % V.kotlin                          % Test,
      "org.jetbrains.kotlin"  % "kotlin-test-junit5"         % V.kotlin                          % Test,
      "org.jetbrains.kotlin"  % "kotlin-reflect"             % V.kotlin                          % Test,
      "io.kotest"             % "kotest-assertions-core-jvm" % V.kotest                          % Test,
      "dev.zacsweers.kctfork" % "core"                       % V.kctfork                         % Test,
      "com.github.sbt.junit"  % "jupiter-interface"          % JupiterKeys.jupiterVersion.value  % Test
    ),

    Test / fork := true,
    Test / javaOptions += "-Xmx2g",

    // sbt-kotlin-plugin 3.1.6 inspects every jar on the kotlinc classpath and
    // moves any jar containing META-INF/services/org.jetbrains.kotlin.compiler.plugin.*
    // entries into the compiler-plugin classpath, removing it from the regular
    // classpath. kctfork ships such service files for its own internal use as a
    // KAPT/registrar shim, which makes its public API (com.tschuchort.compiletesting.*)
    // invisible to our test sources. Workaround: pre-extract kctfork to a
    // directory and add that directory to the test classpath — sbt-kotlin-plugin
    // only inspects .jar files, so directories pass through unmodified.
    Test / unmanagedJars += {
      val report  = update.value
      val files   = report.allFiles
      val jar     = files
        .find(_.getName == s"core-${V.kctfork}.jar")
        .getOrElse(sys.error(s"kctfork core-${V.kctfork}.jar not found in update report"))
      val dir     = target.value / s"kctfork-${V.kctfork}-extracted"
      val marker  = dir / ".extracted"
      if (!marker.exists()) {
        IO.delete(dir)
        IO.unzip(jar, dir)
        IO.touch(marker)
      }
      Attributed.blank(dir)
    }
  )

lazy val snapshotsRunner = project
  .in(file("snapshots-runner"))
  .enablePlugins(KotlinPlugin)
  .settings(
    publish / skip   := true,
    kotlinVersion    := V.kotlin,
    kotlincJvmTarget := "1.8",
    kotlinLib("stdlib"),

    // Pulls in com.sourcegraph.scip_java.ScipJava (the published scip-java CLI)
    // which Snapshot.kt invokes via ScipJava.main.
    libraryDependencies +=
      "com.sourcegraph" % "scip-java_2.13" % V.scipJava,

    // ScipJava.main calls runAndExitIfNonZero, which calls System.exit on
    // failure — fork so it cannot kill the sbt JVM. Lets the `snapshots` task
    // below invoke `runMain` instead of constructing a ForkRun by hand.
    Compile / run / fork := true
  )

// Regenerates the golden SemanticDB/SCIP snapshots checked into
// semanticdb-kotlinc/minimized/src/generatedSnapshots/resources/. Defined at
// the top level so it is in scope for the `minimized` project below.
lazy val snapshots = taskKey[Unit](
  "Run the SCIP snapshot generator over the minimized project"
)

// `minimized` mirrors the (still-present) Gradle build at
// semanticdb-kotlinc/minimized/build.gradle.kts. It compiles a small set of
// Kotlin and Java fixtures with the assembled `kotlinc` plugin attached to
// kotlinc/javac, producing *.semanticdb files under target/semanticdb-targetroot/
// which are then converted to SCIP and rendered as the human-readable golden
// snapshots by the `snapshots` task.
lazy val minimized = project
  .in(file("semanticdb-kotlinc/minimized"))
  .enablePlugins(KotlinPlugin)
  .settings(
    publish / skip   := true,
    crossPaths       := false,
    autoScalaLibrary := false,
    kotlinVersion    := V.kotlin,
    kotlincJvmTarget := "1.8",
    kotlinLib("stdlib"),

    // Loads the javac semanticdb plugin via -Xplugin:semanticdb. Only needed
    // at compile time — its classes don't appear on the runtime classpath.
    libraryDependencies +=
      "com.sourcegraph" % "semanticdb-javac" % V.semanticdbJavac % Provided,

    // Force javac to fork. Two reasons:
    //   1. JDK 9+ strongly encapsulates jdk.compiler internals; semanticdb-javac
    //      reflectively touches them and needs --add-exports flags. With a
    //      forked javac we can pass `-J--add-exports=...` (mirrors scip-java).
    //   2. sbt's in-process javac receives `vf://` virtual-file URIs from the
    //      MappedFileConverter, which semanticdb-javac cannot resolve via
    //      java.nio.file.Path.of. Forked javac is invoked with absolute file
    //      paths instead, so the plugin sees real paths.
    // Setting javaHome to Some(<current JVM home>) flips
    // ZincUtil.compilers/JavaTools.directOrFork from direct → fork.
    javaHome := Some(file(System.getProperty("java.home"))),
    Compile / javacOptions ++= Seq(
      "-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
      "-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
      "-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
      "-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
    ),

    // Attach the assembled kotlinc fat-jar to the compile classpath.
    // sbt-kotlin-plugin's AnalyzingKotlinCompiler partitions the classpath:
    // any jar containing META-INF/services/org.jetbrains.kotlin.compiler.plugin*
    // entries (which our fat-jar does, for both CommandLineProcessor and
    // CompilerPluginRegistrar) is moved into args.pluginClasspaths and removed
    // from the regular classpath. So no `-Xplugin=<path>` is needed and we
    // don't have to predict the assembled jar's filename. The .value reference
    // also gives us the right task ordering — assembly runs before compile.
    Compile / unmanagedJars +=
      Attributed.blank((kotlinc / Compile / packageBin).value),
    Compile / kotlincPluginOptions ++= {
      val srcRoot = (ThisBuild / baseDirectory).value.getAbsolutePath
      val tgtRoot = (target.value / "semanticdb-targetroot").getAbsolutePath
      Seq(
        s"plugin:semanticdb-kotlinc:sourceroot=$srcRoot",
        s"plugin:semanticdb-kotlinc:targetroot=$tgtRoot"
      )
    },

    // The semanticdb javac plugin parses its own argument string, so
    // `-Xplugin:semanticdb -sourceroot:<...> -targetroot:<...>` MUST be passed
    // as a single javac argument (matches the existing Gradle behavior).
    Compile / javacOptions += {
      val srcRoot = (ThisBuild / baseDirectory).value
      val tgtRoot = target.value / "semanticdb-targetroot"
      s"-Xplugin:semanticdb -sourceroot:${srcRoot.getAbsolutePath} " +
        s"-targetroot:${tgtRoot.getAbsolutePath}"
    },

    // ----- snapshots regeneration task -----
    // Runs snapshotsRunner's SnapshotKt in the snapshotsRunner JVM (forked —
    // ScipJava.main calls System.exit). Snapshot.kt reads sourceroot,
    // targetroot, snapshotDir from argv.
    snapshots := Def.taskDyn {
      val _       = (Compile / compile).value
      val srcRoot = (ThisBuild / baseDirectory).value.getAbsolutePath
      val tgtRoot = (target.value / "semanticdb-targetroot").getAbsolutePath
      val snapDir = (baseDirectory.value / "src" / "generatedSnapshots" / "resources").getAbsolutePath
      (snapshotsRunner / Compile / runMain)
        .toTask(s" com.sourcegraph.scip_kotlin.SnapshotKt $srcRoot $tgtRoot $snapDir")
    }.value
  )
