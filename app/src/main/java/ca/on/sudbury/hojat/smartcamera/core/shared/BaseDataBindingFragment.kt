package ca.on.sudbury.hojat.smartcamera.core.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding


abstract class BaseDataBindingFragment<T : ViewDataBinding>(private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T) : BaseFragment() {

    private var safeBinding: T? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = with(inflate(inflater, container, false)) {
        lifecycleOwner = this@BaseDataBindingFragment
        safeBinding = this
        onBindData(this)
        return@with root
    }

    abstract fun onBindData(binding: T)

    override fun onDestroyView() {
        safeBinding = null
        super.onDestroyView()
    }
}
