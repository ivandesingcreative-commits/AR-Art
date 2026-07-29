package com.example.util

import android.content.Context
import android.net.Uri
import com.example.model.Edge3D
import com.example.model.Mesh3D
import com.example.model.Vector3D
import java.io.BufferedReader
import java.io.InputStreamReader

object Obj3DFileParser {

    fun parseObjFile(context: Context, uri: Uri, fileName: String): Mesh3D? {
        val vertices = mutableListOf<Vector3D>()
        val edges = mutableSetOf<Pair<Int, Int>>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null) {
                val trimmed = line.trim()
                if (trimmed.startsWith("v ")) {
                    val parts = trimmed.split("\\s+".toRegex())
                    if (parts.size >= 4) {
                        val x = parts[1].toFloatOrNull() ?: 0f
                        val y = parts[2].toFloatOrNull() ?: 0f
                        val z = parts[3].toFloatOrNull() ?: 0f
                        // Scale up if model is unit length
                        vertices.add(Vector3D(x, y, z))
                    }
                } else if (trimmed.startsWith("f ")) {
                    val parts = trimmed.split("\\s+".toRegex())
                    val faceIndices = mutableListOf<Int>()
                    for (i in 1 until parts.size) {
                        val vertexPart = parts[i].split("/")[0]
                        val idx = vertexPart.toIntOrNull()
                        if (idx != null) {
                            // 1-indexed in .obj format
                            val finalIdx = if (idx > 0) idx - 1 else vertices.size + idx
                            if (finalIdx in vertices.indices) {
                                faceIndices.add(finalIdx)
                            }
                        }
                    }

                    for (i in faceIndices.indices) {
                        val start = faceIndices[i]
                        val end = faceIndices[(i + 1) % faceIndices.size]
                        val pair = if (start < end) start to end else end to start
                        edges.add(pair)
                    }
                }
                line = reader.readLine()
            }
            reader.close()

            if (vertices.isEmpty()) return null

            // Normalize and scale mesh to display viewport standard size (~150 units)
            val minX = vertices.minOf { it.x }
            val maxX = vertices.maxOf { it.x }
            val minY = vertices.minOf { it.y }
            val maxY = vertices.maxOf { it.y }
            val minZ = vertices.minOf { it.z }
            val maxZ = vertices.maxOf { it.z }

            val centerX = (minX + maxX) / 2f
            val centerY = (minY + maxY) / 2f
            val centerZ = (minZ + maxZ) / 2f

            val extent = maxOf(maxX - minX, maxY - minY, maxZ - minZ).coerceAtLeast(0.001f)
            val scaleFactor = 200f / extent

            val normalizedVertices = vertices.map { v ->
                Vector3D(
                    (v.x - centerX) * scaleFactor,
                    (v.y - centerY) * scaleFactor,
                    (v.z - centerZ) * scaleFactor
                )
            }

            val edgeList = edges.map { Edge3D(it.first, it.second) }

            return Mesh3D(
                vertices = normalizedVertices,
                edges = edgeList,
                name = fileName.ifBlank { "Modelo OBJ Importado" }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
