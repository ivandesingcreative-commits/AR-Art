package com.example.model

enum class Guide3DType(val displayName: String, val description: String) {
    HEAD_BUST("Busto / Cabeza", "Guía de proporciones para rostro, ojos y nariz"),
    SPHERE("Esfera / Masa Base", "Masa esférica para figuras redondas y cabezas"),
    CYLINDER("Cilindro / Extremidad", "Guía para brazos, piernas y cuellos de arcilla"),
    ANIMAL_FORM("Cuerpo de Animal", "Masa anatómica para cuadrúpedos y criaturas"),
    TORSO("Torso / Pecho", "Proporciones de torso humano o personaje"),
    POT_VASE("Vasija / Cuenco", "Guía de simetría circular para objetos cóncavos")
}

data class Vector3D(val x: Float, val y: Float, val z: Float)
data class Edge3D(val start: Int, val end: Int)

data class Mesh3D(
    val vertices: List<Vector3D>,
    val edges: List<Edge3D>,
    val name: String
) {
    companion object {
        fun createSphere(radius: Float = 100f, rings: Int = 8, segments: Int = 12): Mesh3D {
            val vertices = mutableListOf<Vector3D>()
            val edges = mutableListOf<Edge3D>()

            for (i in 0..rings) {
                val phi = Math.PI * i / rings
                for (j in 0 until segments) {
                    val theta = 2.0 * Math.PI * j / segments
                    val x = (radius * Math.sin(phi) * Math.cos(theta)).toFloat()
                    val y = (radius * Math.cos(phi)).toFloat()
                    val z = (radius * Math.sin(phi) * Math.sin(theta)).toFloat()
                    vertices.add(Vector3D(x, y, z))
                }
            }

            for (i in 0 until rings) {
                for (j in 0 until segments) {
                    val current = i * segments + j
                    val nextInRing = i * segments + (j + 1) % segments
                    val nextInNextRing = (i + 1) * segments + j

                    edges.add(Edge3D(current, nextInRing))
                    if (i < rings) {
                        edges.add(Edge3D(current, nextInNextRing))
                    }
                }
            }
            return Mesh3D(vertices, edges, "Esfera de Arcilla")
        }

        fun createCylinder(radius: Float = 70f, height: Float = 200f, segments: Int = 12): Mesh3D {
            val vertices = mutableListOf<Vector3D>()
            val edges = mutableListOf<Edge3D>()

            val halfH = height / 2f
            for (level in listOf(-halfH, 0f, halfH)) {
                for (i in 0 until segments) {
                    val angle = 2.0 * Math.PI * i / segments
                    val x = (radius * Math.cos(angle)).toFloat()
                    val z = (radius * Math.sin(angle)).toFloat()
                    vertices.add(Vector3D(x, level, z))
                }
            }

            // Edges for 3 rings
            for (ring in 0..2) {
                for (i in 0 until segments) {
                    val current = ring * segments + i
                    val next = ring * segments + (i + 1) % segments
                    edges.add(Edge3D(current, next))
                    if (ring < 2) {
                        edges.add(Edge3D(current, current + segments))
                    }
                }
            }
            return Mesh3D(vertices, edges, "Cilindro Base")
        }

        fun createHeadBust(scale: Float = 1.2f): Mesh3D {
            val baseSphere = createSphere(radius = 80f * scale, rings = 7, segments = 10)
            val vList = baseSphere.vertices.map { v ->
                // Stretch vertically for head skull, flatten sides
                Vector3D(v.x * 0.85f, v.y * 1.25f, v.z * 0.95f)
            }.toMutableList()

            // Add jaw line points
            val jawIdxStart = vList.size
            vList.add(Vector3D(-40f * scale, -100f * scale, 30f * scale))
            vList.add(Vector3D(0f * scale, -120f * scale, 50f * scale))
            vList.add(Vector3D(40f * scale, -100f * scale, 30f * scale))

            val eList = baseSphere.edges.toMutableList()
            eList.add(Edge3D(jawIdxStart, jawIdxStart + 1))
            eList.add(Edge3D(jawIdxStart + 1, jawIdxStart + 2))

            return Mesh3D(vList, eList, "Busto de Cabeza")
        }

        fun createAnimalBody(): Mesh3D {
            val vertices = mutableListOf<Vector3D>()
            val edges = mutableListOf<Edge3D>()

            // Chest sphere
            val chestMesh = createSphere(radius = 60f, rings = 5, segments = 8)
            val chestOffset = chestMesh.vertices.map { Vector3D(it.x - 50f, it.y, it.z) }
            vertices.addAll(chestOffset)

            // Hip sphere
            val hipOffset = chestMesh.vertices.map { Vector3D(it.x + 60f, it.y * 0.85f, it.z * 0.85f) }
            val hipStart = vertices.size
            vertices.addAll(hipOffset)

            // Edges between chest and hip (spine)
            edges.addAll(chestMesh.edges)
            edges.addAll(chestMesh.edges.map { Edge3D(it.start + hipStart, it.end + hipStart) })
            edges.add(Edge3D(0, hipStart))
            edges.add(Edge3D(4, hipStart + 4))

            return Mesh3D(vertices, edges, "Cuerpo de Criatura")
        }
    }
}
