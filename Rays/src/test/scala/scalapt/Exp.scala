package scalapt

import java.io.File

object Exp extends App {
  val objects : List[Shape] =
    List(
      Plane("left"  , Material.reflective(0.997, 0.998, 0.999), Axis.X, posFacing = true ,   1),
      Plane("right" , Material.reflective(0.997, 0.998, 0.999), Axis.X, posFacing = false,  99),
      Plane("back"  , Material.reflective(0.997, 0.998, 0.999), Axis.Z, posFacing = true ,   0),
      Plane("front" , Material.diffuse   (RGB.black          ), Axis.Z, posFacing = false, 170),
      Plane("bottom", Material.reflective(0.997, 0.998, 0.999), Axis.Y, posFacing = true ,   0),
      Plane("top"   , Material.reflective(0.997, 0.998, 0.999), Axis.Y, posFacing = false,  81.6),

      Sphere("mirror", Material.reflective(RGB.white * 0.999), Point3(27, 16.5, 47), 16.5),
      Sphere("glass" , Material.refractive(RGB.white * 0.999), Point3(73, 16.5, 78), 16.5),

      Sphere("light" , Material.emissive(RGB.white * 12), Point3(50, 681.6 - 0.27, 81.6), 600.0)
    )
  val scene = Scene(
    Camera(
      Ray(
        Point3(50, 52, 295.6),
        Vector3(0, -0.042612, -1)
      ), 0.5135
    ), objects)

  val outF = new File(new File(sys.props("user.home"), "Pictures"), "experiment.png")

  new MainFrame(frameTitle = "Exp", w = 1024, h = 768, inFile = "scenes/cornell2.json", frames = 4,
    outFile = Some(outF), scene = Exp.scene)
}
