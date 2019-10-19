package io.tuliplogic.raytracer.ops.model

import io.tuliplogic.raytracer.geometry.affine.AffineTransformation
import io.tuliplogic.raytracer.geometry.affine.PointVec.Pt
import io.tuliplogic.raytracer.ops.drawing.Pattern
import zio.UIO

case class Material(
    pattern: Pattern,
    ambient: Double, //TODO refine Double > 0 && < 1
    diffuse: Double, //TODO refine Double > 0 && < 1
    specular: Double, //TODO refine Double > 0 && < 1 specularity of the surface to the light source
    shininess: Double, //TODO refine Double > 10 && < 200 shininess of the surface to the light source
    reflective: Double, //TODO refine Double [0, 1] generic reflectiveness of the surface, of generic rays not only coming from the light sourcex
    transparency: Double, //TODO refine Double [0, 1] how transparent the material is
    refractionIndex: Double //TODO refine Double [0, 1] the material refraction index (for vacuum it's 1)
)

object Material {
  def default: UIO[Material] = Pattern.uniform(Color.white).provideM(AffineTransformation.id).map(Material(_,
    ambient = 0.1, diffuse = 0.9, specular = 0.9, shininess = 200d, reflective = 0, transparency = 0, refractionIndex = 1))
  val glass: UIO[Material] = Pattern.uniform(Color.white).provideM(AffineTransformation.id).map(Material(_,
    ambient = 0.0, diffuse = 0.0, specular = 0.1, shininess = 200d, reflective = 0.4, transparency = 0.95, refractionIndex = 1.5))
  val air: UIO[Material] = Pattern.uniform(Color.white).provideM(AffineTransformation.id).map(Material(_,
    ambient = 0.0, diffuse = 0.0, specular = 0, shininess = 0, reflective = 0, transparency = 1, refractionIndex = 1))

}

sealed trait SpatialEntity
object SpatialEntity {
  sealed trait SceneObject {
    def transformation: AffineTransformation
    def material: Material
  }
  object SceneObject {

    //TODO refine Double > 0
    case class PointLight(position: Pt, intensity: Color)

    /**
      * A unit sphere centered in (0, 0, 0) and a transformation on the sphere that puts it  into final position
      * This can be e.g. a chain of transate and shear
      */
    case class Sphere(transformation: AffineTransformation, material: Material) extends SceneObject
    object Sphere {
      def withTransformAndMaterial(tf: AffineTransformation, material: Material): UIO[Sphere] = UIO(tf).zipWith(UIO(material))(Sphere(_, _))
      def unit: UIO[Sphere] =
        for {
          tf  <- AffineTransformation.id
          mat <- Material.default
          res <- withTransformAndMaterial(tf, mat)
        } yield res

      def unitGlass: UIO[Sphere] =
        for {
          tf  <- AffineTransformation.id
          defMat <- Material.default
          mat <- UIO(defMat.copy(transparency = 1.0, refractionIndex = 1.5))
          res <- withTransformAndMaterial(tf, mat)
        } yield res

    }

    /**
      * Canonical plane {y = 0}
      */
    case class Plane(transformation: AffineTransformation, material: Material) extends SceneObject
    object Plane {
      val horizEpsilon: Double = 1e-4

      def withTransformAndMaterial(tf: AffineTransformation, material: Material): UIO[Plane] = UIO(tf).zipWith(UIO(material))(Plane(_, _))

      def canonical: UIO[Plane] =
        for {
          tf  <- AffineTransformation.id
          mat <- Material.default
          res <- withTransformAndMaterial(tf, mat)
        } yield res
    }
  }

}
