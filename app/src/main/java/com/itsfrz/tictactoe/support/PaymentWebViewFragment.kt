package com.itsfrz.tictactoe.support

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PaymentWebViewFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        webView = WebView(requireContext())
        return webView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val url = requireArguments()
            .getString("url")
            ?: return
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient =
            object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val currentUrl = request?.url.toString()
                    Log.d("WEBVIEW", currentUrl)
                    when {
                        currentUrl.contains("payment-success", ignoreCase = true) -> {
                            parentFragmentManager.setFragmentResult(
                                "payment_result",
                                bundleOf(
                                    "status" to "success"
                                )
                            )

                            findNavController().popBackStack()
                            return true
                        }
                        currentUrl.contains("payment-failed", ignoreCase = true) -> {
                            parentFragmentManager.setFragmentResult(
                                "payment_result",
                                bundleOf(
                                    "status" to "failure"
                                )
                            )
                            findNavController().popBackStack()
                            return true
                        }
                        currentUrl.contains("payment-cancel", ignoreCase = true) -> {
                            parentFragmentManager.setFragmentResult(
                                "payment_result",
                                bundleOf("status" to "cancelled")
                            )
                            findNavController().popBackStack()
                            return true
                        }
                    }
                    return false
                }
            }
        webView.loadUrl(url)
    }
}