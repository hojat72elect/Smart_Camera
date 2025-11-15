package ca.on.hojat.smart_camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import ca.on.hojat.smart_camera.ui.theme.SmartCameraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCameraTheme {
                Scaffold { innerPadding ->
                    CameraPreview(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}