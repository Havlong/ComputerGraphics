package ru.pnzgu.computergraphics

infix fun List<List<Float>>.dot(other: List<List<Float>>): List<List<Float>> {
    if (this[0].size != other.size)
        return listOf()
    return List(size) { i ->
        List(other[0].size) { j ->
            var result = 0F
            for (t in other.indices)
                result += this[i][t] * other[t][j]
            result
        }
    }
}