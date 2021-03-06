//package io.tuliplogic.raytracer.ops.programs
//
//import java.nio.file.{Path, Paths}
//
//import io.tuliplogic.raytracer.commons.errors.RayTracerError
//import io.tuliplogic.raytracer.geometry.affine.ATModule
//import io.tuliplogic.raytracer.geometry.matrix.MatrixModule
//import io.tuliplogic.raytracer.geometry.affine.PointVec.Pt
//import io.tuliplogic.raytracer.ops.model.data.Pattern
//import io.tuliplogic.raytracer.ops.model.SceneObject.PointLight
//import io.tuliplogic.raytracer.ops.model.{Canvas, Color, Material, NormalReflectModule, PhongReflectionModule, RayModule}
//import io.tuliplogic.raytracer.ops.rendering.{CanvasRenderer, canvasRendering}
//import zio.blocking.Blocking
//import zio.clock.Clock
//import zio.console.Console
//import zio.{App, UIO, ZIO, clock, console}
//import zio.ZEnv
//
//
//object Chapter6Sphere extends App {
//  val infinitePoint    = Pt(0, 0, -2.5)
//  val pointLight       = PointLight(Pt(-10, 10, -10), Color.white)
//  val canvasHalfWidth  = 7d
//  val canvasHalfHeight = 7d
//  val canvasZCoord     = 10
//  val canvasHRes       = 500
//  val canvasVRes       = 500
//  val canvasFile       = "ppm/chapter-6-shaded-sphere.ppm"
//
//  type RichRayOperations = PhongReflectionModule with NormalReflectModule with RayModule
//
//  val program: ZIO[CanvasRenderer with RichRayOperations with Clock with Console, RayTracerError, Unit] = for {
//    startTime    <- clock.nanoTime
//    canvas       <- Canvas.create(canvasHRes, canvasVRes)
//    idTf         <- ATModule.>.id
//    pat          <- Pattern.uniform(Color(1, 0.2, 1)).provide(idTf)
//    mat          <- UIO(Material(pat, ambient = 0.2, diffuse = 0.9, 0.9, 50d, 0, 0, 1))
//    sampledRect  <- UIO.succeed(SampledRect(canvasHalfWidth, canvasHalfHeight, canvasZCoord, canvasHRes, canvasVRes))
//    scene        <- UIO.succeed(Scene(infinitePoint, pointLight))
//    sphereTransf <- AffineTransformation.id
//    sphere       <- UIO(Sphere(sphereTransf, mat))
//    _ <- sampledRect.pixelsChunkedStream.foreach {
//      case (pt, xn, yn) =>
//        scene.intersectAndComputePhong(pt, sphere).flatMap {
//          case None             => canvas.update(xn, yn, Color.black)
//          case Some(phongComps) => canvas.update(xn, yn, phongComps.toColor)
//        }
//    }
//    calcTime <- clock.nanoTime
//    _        <- console.putStr(s"computation time: ${(calcTime - startTime) / 1000} us")
//    _        <- canvasRendering.render(canvas, 255)
//    endTime  <- clock.nanoTime
//    _        <- console.putStrLn(s"total time taken: ${(endTime - startTime) / 1000} us")
//  } yield ()
//
//  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
//    program
//      .provide {
//        new CanvasRenderer.PPMCanvasRenderer with RichRayOperations.Live with Blocking.Live with MatrixModule.BreezeLive with Console.Live with Clock.Live
//        with AffineTransformationOps.BreezeMatrixOps$ {
//          override def path: Path = Paths.get(canvasFile)
//        }
//      }
//      .foldM(err => console.putStrLn(s"Execution failed with: $err").as(1), _ => UIO.succeed(0))
//
//}
