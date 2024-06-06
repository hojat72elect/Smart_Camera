package ca.on.sudbury.hojat.smartcamera.feature_camera

interface CameraUseCase {
    fun sayHello(): String
}

class CameraUseCaseImpl : CameraUseCase {
    override fun sayHello() = "Hello!\nI'm a Camera UseCase."

}