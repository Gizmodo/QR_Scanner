package com.example.qr3.ui.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.qr3.R
import com.example.qr3.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.config.BarcodeFormat
import io.github.g00fy2.quickie.config.ScannerConfig
import io.github.g00fy2.quickie.content.QRContent

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var selectedBarcodeFormat = BarcodeFormat.FORMAT_DATA_MATRIX
    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel
    private val scanQrCode = registerForActivityResult(ScanQRCode(), ::showSnackbar)
    private val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::showSnackbar)
    private fun showSnackbar(result: QRResult) {
        val text = when (result) {
            is QRResult.QRSuccess -> result.content.rawValue
            QRResult.QRUserCanceled -> "User canceled"
            QRResult.QRMissingPermission -> "Missing permission"
            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }

        Snackbar.make(binding.root, text, Snackbar.LENGTH_INDEFINITE).apply {
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.run {
                maxLines = 5
                setTextIsSelectable(true)
            }
            if (result is QRResult.QRSuccess && result.content is QRContent.Url) {
                setAction(R.string.open_action) { openUrl(result.content.rawValue) }
            } else {
                setAction(R.string.ok_action) { }
            }
        }.show()
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (ignored: ActivityNotFoundException) {
            // no Activity found to run the given Intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.button.setOnClickListener {
            scanCustomCode.launch(
                ScannerConfig.build {
                    setBarcodeFormats(listOf(selectedBarcodeFormat)) // set interested barcode formats
                    setOverlayStringRes(R.string.scan_barcode) // string resource used for the scanner overlay
                    setOverlayDrawableRes(R.drawable.ic_scan_barcode) // drawable resource used for the scanner overlay
                    setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
                    setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
                    setHorizontalFrameRatio(1f) // set the horizontal overlay ratio (default is 1 / square frame)
                    setUseFrontCamera(false) // use the front camera
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

}
