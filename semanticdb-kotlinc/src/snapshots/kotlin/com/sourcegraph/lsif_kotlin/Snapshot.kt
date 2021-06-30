package com.sourcegraph.lsif_kotlin

import com.sourcegraph.lsif_java.SemanticdbPrinters
import com.sourcegraph.lsif_java.commands.CommentSyntax
import com.sourcegraph.semanticdb_javac.Semanticdb.TextDocument
import com.sourcegraph.semanticdb_javac.Semanticdb.TextDocuments
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.relativeTo

fun main() {
    val snapshotDir = Path(System.getProperty("snapshotDir"))
    val sourceDir = Path(System.getProperty("sourceDir"))
    val sourceroot = Path(System.getProperty("sourceroot"))
    val targetroot = Path(System.getProperty("targetroot"))
    val files = SemanticdbFile.fromDirectory(sourceDir, sourceroot, targetroot)

    files.forEach {
        val snapshot = SemanticdbPrinters.printTextDocument(it.textDocument, CommentSyntax.default())
        val relativeToSourceDir = it.javaPath.relativeTo(it.sourceDir)
        val expectedOutPath = snapshotDir.resolve(relativeToSourceDir)
        Files.createDirectories(expectedOutPath.parent)
        Files.write(expectedOutPath, snapshot.toByteArray(StandardCharsets.UTF_8))
    }
}

// because its not shipped as part of lsif_java jar...
class SemanticdbFile(val sourceDir: Path, sourceroot: Path, relativePath: Path, targetroot: Path) {
    companion object {
        fun fromDirectory(sourceDir: Path, sourceroot: Path, targetroot: Path): Sequence<SemanticdbFile> =
            sourceDir.toFile().walkTopDown().mapNotNull {
                if (it.isDirectory) return@mapNotNull null
                SemanticdbFile(sourceDir, sourceroot, it.toPath().relativeTo(sourceroot), targetroot)
            }
    }

    val javaPath: Path = sourceroot.resolve(relativePath)

    private val semanticdbPath: Path = targetroot
        .resolve("META-INF")
        .resolve("semanticdb")
        .resolve("$relativePath.semanticdb")

    val textDocument: TextDocument = run {
        val docs = TextDocuments.parseFrom(Files.readAllBytes(semanticdbPath))
        if (docs.documentsCount == 0)
            TextDocument.newBuilder().build()
        else
            docs.getDocuments(0)
    }
}