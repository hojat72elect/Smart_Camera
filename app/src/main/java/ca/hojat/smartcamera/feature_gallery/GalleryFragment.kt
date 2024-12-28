package ca.hojat.smartcamera.feature_gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.io.File
import java.util.Locale

val EXTENSION_WHITELIST = arrayOf("JPG")


/**
 * Shows a a gallery of photos taken so far.
 */
class GalleryFragment internal constructor() : Fragment() {

    // navigation arguments
    private val args: GalleryFragmentArgs by navArgs()

    private lateinit var mediaList: MutableList<File>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true

        // Get root directory of media from navigation arguments
        val rootDirectory = File(args.rootDirectory)

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        mediaList = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.uppercase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                GalleryScreen(mediaList, {}, {}, {})
            }
        }
    }
}

// Adapter for each photo in the viewpager of the gallery.
class MediaPagerAdapter(fragmentActivity: FragmentActivity, private val mediaList: List<File>) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = mediaList.size

    override fun createFragment(position: Int): Fragment = PhotoFragment.create(mediaList[position])

}