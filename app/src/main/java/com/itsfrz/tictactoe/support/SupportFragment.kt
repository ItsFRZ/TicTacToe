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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.itsfrz.tictactoe.common.constants.BundleKey
import com.itsfrz.tictactoe.common.usecase.CommonUseCase
import com.itsfrz.tictactoe.common.viewmodel.CommonViewModel
import com.itsfrz.tictactoe.support.ui.components.SupportScreen
import com.itsfrz.tictactoe.support.viewmodel.SupportViewModel
import kotlinx.coroutines.launch


class SupportFragment : Fragment() {

    var isSupport : Boolean = false
    private lateinit var viewModel: SupportViewModel
    private lateinit var commonViewModel: CommonViewModel

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
                    val url = bundle.getString("url")
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
                    openPaymentPage("https://rzp.io/rzp/sCJBATL")
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

    private fun sendAcknowledgement(
        txnId: String?
    ) {
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

    private fun openPaymentPage(url: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        startActivity(intent)
    }
}