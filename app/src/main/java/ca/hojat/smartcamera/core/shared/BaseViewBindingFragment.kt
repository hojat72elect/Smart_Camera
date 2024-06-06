package ca.hojat.smartcamera.core.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding


abstract class BaseViewBindingFragment<T : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T
) : BaseFragment() {

    private var safeBinding: T? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        with(inflate(inflater, container, false)) {
            safeBinding = this
            return root
        }
    }

    override fun onDestroyView() {
        safeBinding = null
        super.onDestroyView()
    }
}
