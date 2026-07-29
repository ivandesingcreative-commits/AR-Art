package com.example.model

enum class Guide3DType(val displayName: String, val description: String) {
    SPHERE("Esfera / Masa Base", "Primitiva esférica para volumen inicial y cabezas"),
    CUBE("Cubo / Prisma Rectangular", "Primitiva cúbica para bloques, cajas y planos rectos"),
    CYLINDER("Cilindro / Columna", "Guía cilíndrica para brazos, piernas y cuellos"),
    CONE("Cono / Pirámide", "Primitiva cónica para bases cónicas y bocetas"),
    HEAD_BUST("Cabeza / Bloque Anatómico", "Guía Loomis de proporciones para cabeza y rostro"),
    TORSO("Torso / Caja Torácica", "Estructura de volumen para pecho y cadera"),
    POT_VASE("Objeto Cóncavo / Revolución", "Simetría circular para vasijas y recipientes")
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
            return Mesh3D(vertices, edges, "Esfera Primitiva")
        }

        fun createCube(size: Float = 160f): Mesh3D {
            val h = size / 2f
            val vertices = listOf(
                Vector3D(-h, -h, -h), // 0
                Vector3D(h, -h, -h),  // 1
                Vector3D(h, h, -h),   // 2
                Vector3D(-h, h, -h),  // 3
                Vector3D(-h, -h, h),  // 4
                Vector3D(h, -h, h),   // 5
                Vector3D(h, h, h),    // 6
                Vector3D(-h, h, h)    // 7
            )
            val edges = listOf(
                // Bottom face
                Edge3D(0, 1), Edge3D(1, 2), Edge3D(2, 3), Edge3D(3, 0),
                // Top face
                Edge3D(4, 5), Edge3D(5, 6), Edge3D(6, 7), Edge3D(7, 4),
                // Pillars
                Edge3D(0, 4), Edge3D(1, 5), Edge3D(2, 6), Edge3D(3, 7)
            )
            return Mesh3D(vertices, edges, "Cubo Primitivo")
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

        fun createCone(radius: Float = 90f, height: Float = 200f, segments: Int = 10): Mesh3D {
            val vertices = mutableListOf<Vector3D>()
            val edges = mutableListOf<Edge3D>()

            // Apex
            vertices.add(Vector3D(0f, height / 2f, 0f))

            // Base circle
            val baseY = -height / 2f
            for (i in 0 until segments) {
                val angle = 2.0 * Math.PI * i / segments
                val x = (radius * Math.cos(angle)).toFloat()
                val z = (radius * Math.sin(angle)).toFloat()
                vertices.add(Vector3D(x, baseY, z))
            }

            // Edges from apex to base
            for (i in 1..segments) {
                val next = if (i == segments) 1 else i + 1
                edges.add(Edge3D(0, i))
                edges.add(Edge3D(i, next))
            }
            return Mesh3D(vertices, edges, "Cono Primitivo")
        }

        fun createHeadBust(scale: Float = 1.2f): Mesh3D {
            val baseSphere = createSphere(radius = 80f * scale, rings = 7, segments = 10)
            val vList = baseSphere.vertices.map { v ->
                Vector3D(v.x * 0.85f, v.y * 1.25f, v.z * 0.95f)
            }.toMutableList()

            val jawIdxStart = vList.size
            vList.add(Vector3D(-40f * scale, -100f * scale, 30f * scale))
            vList.add(Vector3D(0f * scale, -120f * scale, 50f * scale))
            vList.add(Vector3D(40f * scale, -100f * scale, 30f * scale))

            val eList = baseSphere.edges.toMutableList()
            eList.add(Edge3D(jawIdxStart, jawIdxStart + 1))
            eList.add(Edge3D(jawIdxStart + 1, jawIdxStart + 2))

            return Mesh3D(vList, eList, "Cabeza Anatómica")
        }
    }
}
