import _root_.kotlin.Keys._
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq}
import scala.xml.transform.{RewriteRule, RuleTransformer}

lazy val V = new {
  val kotlin   = "2.2.0"
  val protobuf = "3.17.3"
  val protoc   = "3.17.3"
  val kotest   = "4.6.3"
  val kctfork  = "0.7.1"
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
    moduleName       := "semanticdb-kotlinc",
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
