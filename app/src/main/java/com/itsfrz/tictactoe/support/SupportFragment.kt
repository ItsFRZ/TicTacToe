package com.itsfrz.tictactoe.support

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.itsfrz.tictactoe.R
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.enums.GameMode
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.support.ui.components.SupportScreen
import com.itsfrz.tictactoe.support.viewmodel.SupportViewModel
import kotlinx.coroutines.launch


class SupportFragment : Fragment() {

    var isSupport : Boolean = false
    private lateinit var viewModel: SupportViewModel
    private lateinit var commonViewModel: CommonViewModel

    val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handlePaymentResult(result)
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        isSupport = requireArguments().getBoolean(BundleKey.FULL_SUPPORT,false)
        viewModel = ViewModelProvider(this)[SupportViewModel::class.java]
        commonViewModel = CommonViewModel.getInstance()

        registerWebView()
    }

    private fun registerWebView() {
        parentFragmentManager.setFragmentResultListener(
            "payment_result",
            this
        ) { _, bundle ->
            when(bundle.getString("status")) {
                "success" -> {
                    Log.d("PAYMENT", "Payment Success")
                    val url = bundle.getString("url")
                    Log.d("PAYMENT", "Success URL = $url")
                    sendAcknowledgement("webview_success")
                }

                "failure" -> {
                    Log.d("PAYMENT", "Payment Failed")
                }
                "cancelled" -> {
                    Log.d("PAYMENT", "Payment Cancelled")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                SupportScreen(purchase = isSupport == false){activity, amount ->
                    viewModel.onAmountChanged(amount)
                    lifecycleScope.launch {
                        commonViewModel.updateCashInfo(amount)
                    }
                    if (isUpiAvailable()){
                        viewModel.onPayment(activity = activity, launcher = launcher)
                    }else {
                        openPaymentWebView("https://www.paypal.com/paypalme/orangelabs")
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val amount = commonViewModel.getCashInfo()
            val tokenBaseAmount = 100000
            if (amount == 49){
                commonViewModel.onEvent(CommonUseCase.OnPurchaseTokenUpdate((1*tokenBaseAmount)))
            }else if (amount == 149){
                commonViewModel.onEvent(CommonUseCase.OnPurchaseTokenUpdate((3*tokenBaseAmount)))
            }else if (amount == 499){
                commonViewModel.onEvent(CommonUseCase.OnPurchaseTokenUpdate((10*tokenBaseAmount)))
            }
            commonViewModel.updateCashInfo(0)
        }
    }

    private fun handlePaymentResult(
        result: ActivityResult
    ) {
        if (result.resultCode != Activity.RESULT_OK) {
            Log.d("UPI", "Cancelled")
            return
        }
        val response = result.data?.dataString
        Log.d("UPI", "Response = $response")
        val status = extractStatus(response)
        when(status?.lowercase()) {
            "success" -> {
                Log.d("UPI", "Payment Success")
                val txnId = extractValue(response, "txnId")
                sendAcknowledgement(txnId)
            }
            "failure" -> {
                Log.d("UPI", "Payment Failed")
            }
            else -> {
                Log.d("UPI", "Payment Cancelled")
            }
        }
    }

    private fun extractStatus(
        response: String?
    ): String? {
        return extractValue(response, "Status")
    }

    private fun extractValue(
        response: String?,
        key: String
    ): String? {
        response ?: return null
        response.split("&").forEach { param ->
            val parts = param.split("=")
            if (
                parts.size >= 2 &&
                parts[0].equals(key, true)
            ) {
                return parts[1]
            }
        }
        return null
    }

    private fun sendAcknowledgement(
        txnId: String?
    ) {
        Log.d("UPI", "Acknowledgement received for txnId=$txnId")
        txnId?.let {
            if (txnId.isNotBlank()){
                lifecycleScope.launch {
                    val amount = commonViewModel.getCashInfo()
                    val tokenBaseAmount = 100000
                    if (amount == 49){
                        commonViewModel.onEvent(CommonUseCase.OnPurchaseTokenUpdate((1*tokenBaseAmount)))
                    }else if (amount == 149){
                        commonViewModel.onEvent(CommonUseCase.OnPurchaseTokenUpdate((3*tokenBaseAmount)))
                    }else if (amount == 499){
                        commonViewModel.onEvent(CommonUseCase.OnPurchaseTokenUpdate((10*tokenBaseAmount)))
                    }
                    commonViewModel.updateCashInfo(0)
                }
            }
        }
    }

    private fun openPaymentWebView(
        url: String
    ) {
        findNavController().navigate(
            R.id.paymentWebViewFragment,
            bundleOf(
                "url" to url
            )
        )
    }

    private fun isUpiAvailable(): Boolean {

        val uri = Uri.parse("upi://pay")

        val intent = Intent(
            Intent.ACTION_VIEW,
            uri
        )

        return intent.resolveActivity(
            requireActivity().packageManager
        ) != null
    }
}