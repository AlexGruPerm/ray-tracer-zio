package io.tuliplogic.raytracer.geometry.matrix

import cats.FlatMap
import cats.implicits._

import scala.reflect.ClassTag

/**
  * here we can tweak using Vector vs zio Chunk vs fs2 Chunk
  */
object Types {
  type L[A] = Vector[A]
  val L = Vector

  val vectorizable = Vectorizable[L]
  val fm           = FlatMap[L]
  type M   = Matrix[L]
  type Row = M
  type Col = M
  //TODO: Maybe see how we can make point and vector typesafe (wrap them in case class)
  val factory = new Matrix.MatrixFactory[L]()
}
