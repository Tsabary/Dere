package co.getdere.fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.getdere.viewmodels.SharedViewModelImage

import co.getdere.R
import kotlinx.android.synthetic.main.fragment_web_view.*

class WebViewFragment : Fragment() {

    lateinit var sharedViewModelForImage : SharedViewModelImage


    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            sharedViewModelForImage = ViewModelProviders.of(it).get(SharedViewModelImage::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_web_view, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = webview_view

        sharedViewModelForImage.sharedImageObject.observe(this, Observer {
            it?.let {image->

                val url = image.link

                webView.settings.loadWithOverviewMode = true
                webView.settings.useWideViewPort = true
                webView.settings.builtInZoomControls = true
                webView.settings.pluginState = WebSettings.PluginState.ON
                webView.webViewClient = WebViewClient()
                webView.loadUrl(url)

            }
        }
        )



//        webView.settings.javaScriptEnabled = true


    }


}
