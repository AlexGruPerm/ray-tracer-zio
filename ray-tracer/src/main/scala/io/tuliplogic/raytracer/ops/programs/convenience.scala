package io.tuliplogic.raytracer.ops.programs

import io.tuliplogic.raytracer.geometry.affine.ATModule
import io.tuliplogic.raytracer.geometry.matrix.MatrixModule
import io.tuliplogic.raytracer.ops.model.data.RayModule
import io.tuliplogic.raytracer.ops.model.modules
import io.tuliplogic.raytracer.ops.model.modules.{CameraModule, LightDiffusionModule, LightReflectionModule, NormalReflectModule, PhongReflectionModule, RasteringModule, WorldHitCompsModule, WorldModule, WorldReflectionModule, WorldRefractionModule, WorldTopologyModule}
import zio.blocking.Blocking
import zio.console.Console

/**
 * 
 * ray-tracer-zio - 28/10/2019
 * Created with ♥ in Amsterdam
 */
trait FullModules
  extends NormalReflectModule.Live
  with RayModule.Live
  with ATModule.Live
  with MatrixModule.BreezeMatrixModule
  with WorldModule.Live
  with WorldTopologyModule.Live
  with WorldHitCompsModule.Live
  with CameraModule.Live
  with RasteringModule.ChunkRasteringModule
  with Blocking.Live
  with Console.Live
  with FullRenderingModules

trait FullRenderingModules
  extends PhongReflectionModule.Live
  with LightDiffusionModule.Live
  with LightReflectionModule.Live
  with WorldReflectionModule.Live
  with WorldRefractionModule.Live

trait VerySimpleModules
  extends NormalReflectModule.Live
  with RayModule.Live
  with ATModule.Live
  with MatrixModule.BreezeMatrixModule
  with WorldModule.Live
  with WorldTopologyModule.Live
  with WorldHitCompsModule.Live
  with CameraModule.Live
  with RasteringModule.ChunkRasteringModule
  with Blocking.Live
  with Console.Live
  with PhongReflectionModule.BlackWhite
  with WorldReflectionModule.NoWorldReflectionModule
  with WorldRefractionModule.NoWorldRefractionModule

