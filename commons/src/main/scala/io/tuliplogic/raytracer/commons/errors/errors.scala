package io.tuliplogic.raytracer.commons.errors

sealed trait RayTracerError extends Exception
case class ATError(override val getMessage: String) extends RayTracerError

/**
  * Errors for matrix handling
  */
sealed trait AlgebraicError extends RayTracerError
object AlgebraicError {
  case class MatrixDimError(override val getMessage: String) extends AlgebraicError
  case class MatrixNotInvertible(override val getMessage: String)  extends AlgebraicError
  case class VectorNonNormalizable(override val getMessage: String) extends AlgebraicError

  case class IndexExceedMatrixDimension(row: Int, col: Int, m: Int, n: Int) extends AlgebraicError {
    override def getMessage = s"Attempted to access index ($row, $col) in a matrix $m x $n"
  }

  case class MatrixConstructionError(override val getMessage: String) extends AlgebraicError
}

/**
  * Errors for Canvas handling
  */
sealed trait CanvasError extends RayTracerError
object CanvasError {
  case class IndexExceedCanvasDimension(x: Int, y: Int, width: Int, height: Int) extends AlgebraicError {
    override def getMessage = s"Attempted to access index ($x, $y) in a canvas $width x $height"
  }
}

sealed trait BusinessError extends RayTracerError
object BusinessError {
  case class GenericError(override val getMessage: String) extends BusinessError
}

sealed trait IOError extends RayTracerError
object IOError {
  case class CanvasRenderingError(override val getMessage: String, override val getCause: Throwable) extends IOError
  case class HttpError(override val getMessage: String) extends IOError
  case class DrawingRepoError(override val getMessage: String) extends IOError
}
