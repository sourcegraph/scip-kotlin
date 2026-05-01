addSbtPlugin("org.jetbrains.scala" % "sbt-kotlin-plugin"     % "3.1.6")
addSbtPlugin("com.thesamet"        % "sbt-protoc"            % "1.0.6")
addSbtPlugin("com.eed3si9n"        % "sbt-assembly"          % "2.3.1")
addSbtPlugin("com.github.sbt"      % "sbt-ci-release"        % "1.11.1")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"          % "2.5.5")
addSbtPlugin("com.github.sbt.junit" % "sbt-jupiter-interface" % "0.15.1")

libraryDependencies +=
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.20"
