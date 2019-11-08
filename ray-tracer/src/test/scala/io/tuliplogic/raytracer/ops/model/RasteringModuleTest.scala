package io.tuliplogic.raytracer.ops.model

import io.tuliplogic.raytracer.commons.errors.RayTracerError
import io.tuliplogic.raytracer.geometry.affine.PointVec.{Pt, Vec}
import io.tuliplogic.raytracer.ops.model.data.Scene.PointLight
import io.tuliplogic.raytracer.ops.model.data.{Camera, Color, ColoredPixel, Pixel, Ray, World}
import io.tuliplogic.raytracer.ops.model.modules.{CameraModule, RasteringModule, WorldModule}
import io.tuliplogic.raytracer.ops.model.modules.RasteringModule.ChunkRasteringModule
import zio.test.Assertion._
import zio.test.mock.Expectation._
import zio.test._
import zio.{UIO, ZIO}

object RasteringModuleTest extends DefaultRunnableSpec(
  suite("ChunkRasteringModule") {
    testM("raster should rely on cameraModule and world module") {
      val camera = Camera.makeUnsafe(Pt.origin, Pt(0, 0, -1), Vec.uy, math.Pi / 3, 2, 2)
      val world = World(PointLight(Pt(5, 5, 5), Color.white), List())
      val appUnderTest: ZIO[RasteringModule, RayTracerError, List[ColoredPixel]] =
        RasteringModule.>.raster(world, camera).flatMap(_.runCollect)

      for {
        (worldModuleExp, cameraModuleExp) <- RasteringModuleMocks.mockExpectations(world, camera)
        res <- appUnderTest.provideManaged(
          worldModuleExp.managedEnv.zipWith(cameraModuleExp.managedEnv) { (wm, cm) =>
            new ChunkRasteringModule {
              override val cameraModule: CameraModule.Service[Any] = cm.cameraModule
              override val worldModule: WorldModule.Service[Any] = wm.worldModule
                }
              }
            )
        } yield assert(res, equalTo(List(
            ColoredPixel(Pixel(0, 0), Color.red),
            ColoredPixel(Pixel(0, 1), Color.green),
            ColoredPixel(Pixel(1, 0), Color.blue),
            ColoredPixel(Pixel(1, 1), Color.white),
            )))
    }
  }
)

object RasteringModuleMocks {
  def mockExpectations(world: World, camera: Camera) = for {
    r1 <- (Pt(1, 1, -1) - Pt.origin).normalized.map(Ray(Pt.origin, _))
    r2 <- (Pt(-1, 1, -1) - Pt.origin).normalized.map(Ray(Pt.origin, _))
    r3 <- (Pt(-1, -1, -1) - Pt.origin).normalized.map(Ray(Pt.origin, _))
    r4 <- (Pt(1, -1, -1) - Pt.origin).normalized.map(Ray(Pt.origin, _))
    cameraModuleExp <- UIO {
      (CameraModule.rayForPixel(equalTo((camera, 0, 0))) returns value(r1)) *>
      (CameraModule.rayForPixel(equalTo((camera, 0, 1))) returns value(r2)) *>
      (CameraModule.rayForPixel(equalTo((camera, 1, 0))) returns value(r3)) *>
      (CameraModule.rayForPixel(equalTo((camera, 1, 1))) returns value(r4))
    }
    worldModuleExp <- UIO {
      (WorldModule.colorForRay(equalTo((world, r1, 5))) returns value(Color.red)) *>
      (WorldModule.colorForRay(equalTo((world, r2, 5))) returns value(Color.green)) *>
      (WorldModule.colorForRay(equalTo((world, r3, 5))) returns value(Color.blue)) *>
      (WorldModule.colorForRay(equalTo((world, r4, 5))) returns value(Color.white))
    }
  } yield (worldModuleExp, cameraModuleExp)
}
