package io.tuliplogic.raytracer.ops.programs

import io.tuliplogic.raytracer.commons.errors.RayTracerError
import io.tuliplogic.raytracer.geometry.affine.PointVec.{Pt, Vec}
import io.tuliplogic.raytracer.geometry.affine.aTModule.ATModule
import io.tuliplogic.raytracer.ops.model.data.{Camera, Canvas, World}
import io.tuliplogic.raytracer.ops.model.modules.rasteringModule
import io.tuliplogic.raytracer.ops.model.modules.rasteringModule.RasteringModule
import zio.ZIO
import zio.clock.Clock
import zio.stream.Sink
import zio.duration._

object RaytracingProgram {

  private def drawOnCanvasWithCamera(world: World, camera: Camera, canvas: Canvas): ZIO[RasteringModule, RayTracerError, Unit] =
    rasteringModule.raster(world, camera).mapM(cp => canvas.update(cp)).run(Sink.drain)

  def drawOnCanvas(world: World, viewFrom: Pt, viewTo: Pt, upDirection: Vec, visualAngleRad: Double, hRes: Int, vRes: Int):
    ZIO[RasteringModule with ATModule with Clock, RayTracerError, Canvas] = for {
    camera <- Camera.make(viewFrom, viewTo, upDirection, visualAngleRad, hRes, vRes)
    canvas <- Canvas.create(hRes, vRes)
    _      <- ZIO.effectTotal(println(s"created canvas, now proceeding with drawing"))

    to     <- drawOnCanvasWithCamera(world, camera, canvas).timeout(90.seconds)
    _      <- ZIO.effectTotal(println(s"Timed out: ${to.isEmpty}"))
  } yield canvas

}
