package io.tuliplogic.raytracer.ops.model.modules

import io.tuliplogic.raytracer.geometry.affine.ATModule
import io.tuliplogic.raytracer.geometry.affine.PointVec.Pt
import io.tuliplogic.raytracer.ops.model.data.{Camera, Ray}
import zio.macros.annotation.mockable
import zio.{UIO, ZIO}

@mockable
trait CameraModule {
  val cameraModule: CameraModule.Service[Any]
}

object CameraModule {

  trait Service[R] {
    def rayForPixel(camera: Camera, px: Int, py: Int): ZIO[R, Nothing, Ray]
  }

  trait Live extends CameraModule {
    val aTModule: ATModule.Service[Any]

    val cameraModule: CameraModule.Service[Any] = new Service[Any] {
      // Implementation: the canonical camera has the eye in Pt.origin, and the screen on the plane z = -1,
      // therefore after computing the coordinates of the point in the screen, we have to apply the _inverse of the camera transformation_
      // because the camera transformation is the transformation to be applied to thw world in order to produce the effect of moving/orienting the camera around
      // This transformation must be applied both to the point in the camera, and to the origin. Then the computation of the ray is trivial.
      override def rayForPixel(camera: Camera, px: Int, py: Int): ZIO[Any, Nothing, Ray] =
        for {
          xOffset   <- UIO((px + 0.5) * camera.pixelXSize)
          yOffset   <- UIO((py + 0.5) * camera.pixelYSize)
          //coordinates of the canvas point before the transformation
          origX     <- UIO(camera.halfWidth - xOffset)
          origY     <- UIO(camera.halfHeight - yOffset)
          //transform the coordinates by the inverse
          inverseTf <- aTModule.invert(camera.tf)
          pixel     <- aTModule.applyTf(inverseTf, Pt(origX, origY, -1))
          origin    <- aTModule.applyTf(inverseTf, Pt.origin)
          direction <- (pixel - origin).normalized.orDie
        } yield Ray(origin, direction)
    }
  }

  object > extends CameraModule.Service[CameraModule] {
    override def rayForPixel(camera: Camera, px: Int, py: Int): ZIO[CameraModule, Nothing, Ray] =
      ZIO.accessM(_.cameraModule.rayForPixel(camera, px, py))
  }
}
