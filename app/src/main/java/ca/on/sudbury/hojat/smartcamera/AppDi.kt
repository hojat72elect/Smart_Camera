package ca.on.sudbury.hojat.smartcamera

import ca.on.sudbury.hojat.smartcamera.feature_camera.CameraUseCase
import ca.on.sudbury.hojat.smartcamera.feature_camera.CameraUseCaseImpl
import ca.on.sudbury.hojat.smartcamera.feature_camera.CameraViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppDi {
    val appModule = module {
        //  the singleton of our UseCase
        single<CameraUseCase> { CameraUseCaseImpl() }
        // The ViewModel that consumes the UseCase above
        viewModel { CameraViewModel(get()) }
    }
}